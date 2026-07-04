package service;

import generator.Generator;
import model.Patient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class PatientService {
    private final List<Patient> patients = new ArrayList<>();

    public PatientService() {
        generateDummyData();
    }

    private void generateDummyData() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        int days = start.lengthOfMonth();
        Random r = new Random();
        for (int i = 1; i <= days; i++) {
            LocalDate day = start.withDayOfMonth(i);
            if (r.nextDouble() < 0.4) {
                int count = r.nextInt(3) + 1;
                for (int j = 0; j < count; j++) {
                    Patient p = new Patient();
                    p.setId(UUID.randomUUID().toString());
                    p.setName(Generator.makeName());
                    p.setSymptoms(Generator.makeDescription());
                    p.setDob(LocalDate.now().minusYears(15 + r.nextInt(50)));
                    p.setType(r.nextBoolean() ? "Первичный" : "Вторичный");
                    p.setExtraField("+79" + (100000000 + r.nextInt(900000000)));
                    p.setDate(day);
                    p.setTime(LocalTime.of(8 + r.nextInt(10), r.nextBoolean() ? 0 : 30));
                    patients.add(p);
                }
            }
        }
    }

    public List<Patient> getPatientsForDay(LocalDate date) {
        return patients.stream()
                .filter(p -> p.getDate().equals(date))
                .sorted(Comparator.comparing(Patient::getTime))
                .collect(Collectors.toList());
    }

    public List<Patient> getAllPatients() {
        return patients;
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    public boolean deletePatient(String id) {
        return patients.removeIf(p -> p.getId().equals(id));
    }

    public Patient getPatientById(String id) {
        return patients.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}