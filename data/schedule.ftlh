<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Расписание врача</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">Кабинет врача</a>
            <div class="d-flex">
                <span class="navbar-text me-3 text-white">Врач</span>
                <a href="/logout" class="btn btn-outline-danger btn-sm">Выйти</a>
            </div>
        </div>
    </nav>

    <div class="container my-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1>Расписание на ${currentMonth}</h1>
            <span class="badge bg-secondary fs-5">Всего записей: ${totalPatients}</span>
        </div>

        <div class="row row-cols-1 row-cols-md-3 row-cols-lg-4 g-4">
            <#list days as d>
                <div class="col">
                    <div class="card h-100 <#if d.isToday>border-primary bg-primary bg-opacity-10<#else>border-secondary</#if>">
                        <div class="card-header <#if d.isToday>bg-primary text-white<#else>bg-light</#if>">
                            <h5 class="card-title mb-0">День ${d.dayNum}</h5>
                            <small class="text-muted"><#if d.isToday>Сегодня<#else>${d.dateString}</#if></small>
                        </div>
                        <div class="card-body">
                            <p class="card-text">Записано: <strong>${d.count}</strong></p>
                            <ul class="list-unstyled">
                                <#list d.patients as p>
                                    <li class="mb-1 text-truncate">
                                        <span class="badge <#if p.type == 'Первичный'>bg-success<#else>bg-info</#if>">${p.type}</span>
                                        <strong>${p.time}</strong> - ${p.name}
                                    </li>
                                </#list>
                            </ul>
                        </div>
                        <div class="card-footer bg-transparent border-0 mt-auto">
                            <a href="/day?date=${d.date}" class="btn btn-sm btn-outline-primary w-100">Просмотр</a>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </div>

    <footer class="footer mt-auto py-3 bg-white text-center border-top">
        <div class="container">
            <span class="text-muted">Система учета записи пациентов © 2026</span>
        </div>
    </footer>
</body>
</html>