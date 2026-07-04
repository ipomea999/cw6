<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Записи на ${date}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">Кабинет врача</a>
            <div class="d-flex">
                <a href="/" class="btn btn-outline-light btn-sm me-2">В календарь</a>
                <a href="/logout" class="btn btn-outline-danger btn-sm">Выйти</a>
            </div>
        </div>
    </nav>

    <div class="container my-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Список пациентов на ${date}</h2>
            <a href="/add?date=${date}" class="btn btn-primary">Записать нового пациента</a>
        </div>

        <#if patients?size == 0>
            <div class="alert alert-warning text-center" role="alert">
                На этот день записей нет.
            </div>
        <#else>
            <div class="table-responsive bg-white rounded shadow-sm p-3">
                <table class="table table-striped align-middle">
                    <thead>
                        <tr>
                            <th>Время</th>
                            <th>ФИО</th>
                            <th>Дата рождения</th>
                            <th>Тип</th>
                            <th>Анамнез (симптомы)</th>
                            <th>Телефон</th>
                            <th>Действия</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list patients as p>
                            <tr>
                                <td><strong>${p.time}</strong></td>
                                <td>${p.name}</td>
                                <td>${p.dob}</td>
                                <td>
                                    <span class="badge <#if p.type == 'Первичный'>bg-success<#else>bg-info</#if>">${p.type}</span>
                                </td>
                                <td>${p.symptoms}</td>
                                <td>${p.extraField!}</td>
                                <td>
                                    <div class="btn-group" role="group">
                                        <a href="/edit?id=${p.id}" class="btn btn-sm btn-outline-secondary">Редактировать</a>
                                        <form action="/delete" method="POST" class="d-inline" onsubmit="return confirm('Вы уверены?');">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <input type="hidden" name="date" value="${date}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger ms-1">Удалить</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </#if>
    </div>

    <footer class="footer mt-auto py-3 bg-white text-center border-top">
        <div class="container">
            <span class="text-muted">Система учета записи пациентов © 2026</span>
        </div>
    </footer>
</body>
</html>