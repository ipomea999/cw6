<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Редактирование записи</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">Кабинет врача</a>
            <div class="d-flex">
                <a href="/day?date=${patient.date}" class="btn btn-outline-light btn-sm me-2">Назад к списку</a>
                <a href="/logout" class="btn btn-outline-danger btn-sm">Выйти</a>
            </div>
        </div>
    </nav>

    <div class="container my-4">
        <div class="card shadow-sm mx-auto" style="max-width: 600px;">
            <div class="card-header bg-warning text-dark">
                <h4 class="mb-0">Редактирование данных пациента</h4>
            </div>
            <div class="card-body">
                <form action="/edit" method="POST">
                    <input type="hidden" name="id" value="${patient.id}">

                    <div class="mb-3">
                        <label class="form-label">ФИО пациента (не подлежит изменению)</label>
                        <input type="text" class="form-control bg-light" value="${patient.name}" readonly>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="date" class="form-label">Дата приема <span class="text-danger">*</span></label>
                            <input type="date" class="form-control" id="date" name="date" value="${patient.date}" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="time" class="form-label">Время приема <span class="text-danger">*</span></label>
                            <input type="time" class="form-control" id="time" name="time" value="${patient.time}" required>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12 mb-3">
                            <label for="dob" class="form-label">Дата рождения <span class="text-danger">*</span></label>
                            <input type="date" class="form-control" id="dob" name="dob" value="${patient.dob}" required>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Тип пациента <span class="text-danger">*</span></label>
                        <div>
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="type" id="type1" value="Первичный" <#if patient.type == 'Первичный'>checked</#if>>
                                <label class="form-check-label" for="type1">Первичный</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="type" id="type2" value="Вторичный" <#if patient.type == 'Вторичный'>checked</#if>>
                                <label class="form-check-label" for="type2">Вторичный</label>
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="symptoms" class="form-label">Анамнез (симптомы) <span class="text-danger">*</span></label>
                        <textarea class="form-control" id="symptoms" name="symptoms" rows="3" required>${patient.symptoms}</textarea>
                    </div>

                    <div class="mb-3">
                        <label for="extraField" class="form-label">Контактный телефон</label>
                        <input type="text" class="form-control" id="extraField" name="extraField" value="${patient.extraField!}">
                    </div>

                    <button type="submit" class="btn btn-warning w-100 text-dark fw-bold">Сохранить изменения</button>
                </form>
            </div>
        </div>
    </div>

    <footer class="footer mt-auto py-3 bg-white text-center border-top">
        <div class="container">
            <span class="text-muted">Система учета записи пациентов © 2026</span>
        </div>
    </footer>
</body>
</html>