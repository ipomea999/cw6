package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import model.Patient;
import service.PatientService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Server {
    private final String host;
    private final int port;
    private static Configuration freemarkerConfig;
    private static final PatientService patientService = new PatientService();
    private static final Set<String> activeSessions = new HashSet<>();

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        initFreemarker();
    }

    private void initFreemarker() {
        freemarkerConfig = new Configuration();
        try {
            freemarkerConfig.setDirectoryForTemplateLoading(new File("templates"));
            freemarkerConfig.setDefaultEncoding("UTF-8");
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", new AppHandler());
        server.setExecutor(null);
        server.start();
    }

    private static class AppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                if (path.equals("/login")) {
                    if (method.equals("GET")) {
                        handleGetLogin(exchange);
                    } else if (method.equals("POST")) {
                        handlePostLogin(exchange);
                    }
                    return;
                }

                if (!isAuthenticated(exchange)) {
                    sendRedirect(exchange, "/login");
                    return;
                }

                if (path.equals("/logout")) {
                    handleLogout(exchange);
                } else if (path.equals("/")) {
                    handleGetSchedule(exchange);
                } else if (path.equals("/day")) {
                    handleGetDay(exchange);
                } else if (path.equals("/add")) {
                    if (method.equals("GET")) {
                        handleGetAdd(exchange);
                    } else if (method.equals("POST")) {
                        handlePostAdd(exchange);
                    }
                } else if (path.equals("/delete")) {
                    if (method.equals("POST")) {
                        handlePostDelete(exchange);
                    }
                } else if (path.equals("/edit")) {
                    if (method.equals("GET")) {
                        handleGetEdit(exchange);
                    } else if (method.equals("POST")) {
                        handlePostEdit(exchange);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendErrorPage(exchange, List.of("Внутренняя ошибка сервера: " + e.getMessage()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private boolean isAuthenticated(HttpExchange exchange) {
            String sessionId = getSessionId(exchange);
            return sessionId != null && activeSessions.contains(sessionId);
        }

        private String getSessionId(HttpExchange exchange) {
            List<String> cookies = exchange.getRequestHeaders().get("Cookie");
            if (cookies == null) return null;
            for (String cookie : cookies) {
                String[] pairs = cookie.split(";");
                for (String pair : pairs) {
                    String[] kv = pair.trim().split("=");
                    if (kv.length == 2 && kv[0].equals("sessionId")) {
                        return kv[1];
                    }
                }
            }
            return null;
        }

        private void handleGetLogin(HttpExchange exchange) throws Exception {
            Map<String, Object> data = new HashMap<>();
            String html = renderTemplate("login.ftlh", data);
            sendHtml(exchange, html);
        }

        private void handlePostLogin(HttpExchange exchange) throws Exception {
            Map<String, String> params = parsePostBody(exchange);
            String username = params.get("username");
            String password = params.get("password");

            if ("doctor".equals(username) && "password".equals(password)) {
                String sessionId = UUID.randomUUID().toString();
                activeSessions.add(sessionId);
                exchange.getResponseHeaders().set("Set-Cookie", "sessionId=" + sessionId + "; HttpOnly; Path=/");
                sendRedirect(exchange, "/");
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("error", "Неверный логин или пароль");
                String html = renderTemplate("login.ftlh", data);
                sendHtml(exchange, html);
            }
        }

        private void handleLogout(HttpExchange exchange) throws IOException {
            String sessionId = getSessionId(exchange);
            if (sessionId != null) {
                activeSessions.remove(sessionId);
            }
            exchange.getResponseHeaders().set("Set-Cookie", "sessionId=; HttpOnly; Path=/; Max-Age=0");
            sendRedirect(exchange, "/login");
        }

        private void handleGetSchedule(HttpExchange exchange) throws Exception {
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            int daysInMonth = start.lengthOfMonth();
            List<Map<String, Object>> daysList = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int i = 1; i <= daysInMonth; i++) {
                LocalDate currentDay = start.withDayOfMonth(i);
                Map<String, Object> dMap = new HashMap<>();
                dMap.put("dayNum", i);
                dMap.put("date", currentDay.toString());
                dMap.put("dateString", currentDay.getDayOfMonth() + " " + currentDay.getMonth().toString().toLowerCase());
                dMap.put("isToday", currentDay.equals(today));

                List<Patient> list = patientService.getPatientsForDay(currentDay);
                dMap.put("patients", list);
                dMap.put("count", list.size());

                daysList.add(dMap);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("days", daysList);
            data.put("totalPatients", patientService.getAllPatients().size());
            data.put("currentMonth", today.getMonth().toString() + " " + today.getYear());

            String html = renderTemplate("schedule.ftlh", data);
            sendHtml(exchange, html);
        }

        private void handleGetDay(HttpExchange exchange) throws Exception {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String dateStr = query.get("date");
            if (dateStr == null || dateStr.isEmpty()) {
                sendRedirect(exchange, "/");
                return;
            }

            LocalDate date = LocalDate.parse(dateStr);
            List<Patient> patients = patientService.getPatientsForDay(date);

            Map<String, Object> data = new HashMap<>();
            data.put("date", dateStr);
            data.put("patients", patients);

            String html = renderTemplate("day.ftlh", data);
            sendHtml(exchange, html);
        }

        private void handleGetAdd(HttpExchange exchange) throws Exception {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String dateStr = query.get("date");

            Map<String, Object> data = new HashMap<>();
            data.put("date", dateStr);

            String html = renderTemplate("add.ftlh", data);
            sendHtml(exchange, html);
        }

        private void handlePostAdd(HttpExchange exchange) throws Exception {
            Map<String, String> params = parsePostBody(exchange);
            String dateStr = params.get("date");
            String name = params.get("name");
            String timeStr = params.get("time");
            String dobStr = params.get("dob");
            String type = params.get("type");
            String symptoms = params.get("symptoms");
            String extraField = params.get("extraField");

            List<String> errors = new ArrayList<>();
            if (name == null || name.trim().isEmpty()) errors.add("ФИО не может быть пустым.");
            if (timeStr == null || timeStr.trim().isEmpty()) errors.add("Время приема не может быть пустым.");
            if (dobStr == null || dobStr.trim().isEmpty()) errors.add("Дата рождения не может быть пустой.");
            if (symptoms == null || symptoms.trim().isEmpty()) errors.add("Анамнез не может быть пустым.");

            LocalDate date = null;
            try {
                date = LocalDate.parse(dateStr);
                if (!date.isAfter(LocalDate.now())) {
                    errors.add("Дата приема должна быть строго в будущем.");
                }
            } catch (Exception e) {
                errors.add("Неверный формат даты приема.");
            }

            LocalDate dob = null;
            try {
                dob = LocalDate.parse(dobStr);
            } catch (Exception e) {
                errors.add("Неверный формат даты рождения.");
            }

            LocalTime time = null;
            try {
                time = LocalTime.parse(timeStr);
            } catch (Exception e) {
                errors.add("Неверный формат времени.");
            }

            if (!errors.isEmpty()) {
                sendErrorPage(exchange, errors);
                return;
            }

            Patient patient = new Patient();
            patient.setId(UUID.randomUUID().toString());
            patient.setName(name);
            patient.setDate(date);
            patient.setTime(time);
            patient.setDob(dob);
            patient.setType(type);
            patient.setSymptoms(symptoms);
            patient.setExtraField(extraField);

            patientService.addPatient(patient);

            sendRedirect(exchange, "/day?date=" + dateStr);
        }

        private void handlePostDelete(HttpExchange exchange) throws IOException {
            Map<String, String> params = parsePostBody(exchange);
            String id = params.get("id");
            String date = params.get("date");

            patientService.deletePatient(id);
            sendRedirect(exchange, "/day?date=" + date);
        }

        private void handleGetEdit(HttpExchange exchange) throws Exception {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String id = query.get("id");
            Patient p = patientService.getPatientById(id);

            if (p == null) {
                sendErrorPage(exchange, List.of("Пациент не найден."));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("patient", p);

            String html = renderTemplate("edit.ftlh", data);
            sendHtml(exchange, html);
        }

        private void handlePostEdit(HttpExchange exchange) throws Exception {
            Map<String, String> params = parsePostBody(exchange);
            String id = params.get("id");
            String dateStr = params.get("date");
            String timeStr = params.get("time");
            String dobStr = params.get("dob");
            String type = params.get("type");
            String symptoms = params.get("symptoms");
            String extraField = params.get("extraField");

            Patient p = patientService.getPatientById(id);
            if (p == null) {
                sendErrorPage(exchange, List.of("Пациент не найден."));
                return;
            }

            List<String> errors = new ArrayList<>();
            if (timeStr == null || timeStr.trim().isEmpty()) errors.add("Время приема не может быть пустым.");
            if (dobStr == null || dobStr.trim().isEmpty()) errors.add("Дата рождения не может быть пустой.");
            if (symptoms == null || symptoms.trim().isEmpty()) errors.add("Анамнез не может быть пустым.");

            LocalDate date = null;
            try {
                date = LocalDate.parse(dateStr);
                if (!date.isAfter(LocalDate.now())) {
                    errors.add("Новая дата приема должна быть строго в будущем.");
                }
            } catch (Exception e) {
                errors.add("Неверный формат даты приема.");
            }

            LocalDate dob = null;
            try {
                dob = LocalDate.parse(dobStr);
            } catch (Exception e) {
                errors.add("Неверный формат даты рождения.");
            }

            LocalTime time = null;
            try {
                time = LocalTime.parse(timeStr);
            } catch (Exception e) {
                errors.add("Неверный формат времени.");
            }

            if (!errors.isEmpty()) {
                sendErrorPage(exchange, errors);
                return;
            }

            p.setDate(date);
            p.setTime(time);
            p.setDob(dob);
            p.setType(type);
            p.setSymptoms(symptoms);
            p.setExtraField(extraField);

            sendRedirect(exchange, "/day?date=" + dateStr);
        }

        private void sendErrorPage(HttpExchange exchange, List<String> errors) throws Exception {
            Map<String, Object> data = new HashMap<>();
            data.put("errors", errors);
            String html = renderTemplate("error.ftlh", data);
            sendHtml(exchange, html);
        }

        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> result = new HashMap<>();
            if (query == null || query.isEmpty()) {
                return result;
            }
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(
                            URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(entry[1], StandardCharsets.UTF_8)
                    );
                } else if (entry.length == 1) {
                    result.put(
                            URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                            ""
                    );
                }
            }
            return result;
        }

        private Map<String, String> parsePostBody(HttpExchange exchange) throws IOException {
            java.io.InputStream is = exchange.getRequestBody();
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            String body = bos.toString(StandardCharsets.UTF_8);
            return parseQueryParams(body);
        }

        private String renderTemplate(String templateName, Map<String, Object> data) throws Exception {
            Template template = freemarkerConfig.getTemplate(templateName);
            java.io.StringWriter writer = new java.io.StringWriter();
            template.process(data, writer);
            return writer.toString();
        }

        private void sendHtml(HttpExchange exchange, String html) throws IOException {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void sendRedirect(HttpExchange exchange, String location) throws IOException {
            exchange.getResponseHeaders().set("Location", location);
            exchange.sendResponseHeaders(303, -1);
        }
    }
}