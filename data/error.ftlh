<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Ошибка валидации</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container my-5">
        <div class="card border-danger shadow-sm mx-auto" style="max-width: 600px;">
            <div class="card-header bg-danger text-white">
                <h4 class="mb-0">Запрос не может быть выполнен</h4>
            </div>
            <div class="card-body">
                <p class="card-text text-danger fs-5">При обработке вашего запроса возникли ошибки:</p>
                <div class="alert alert-danger" role="alert">
                    <ul class="mb-0">
                        <#list errors as error>
                            <li>${error}</li>
                        </#list>
                    </ul>
                </div>
                <div class="text-center mt-4">
                    <a href="javascript:history.back()" class="btn btn-primary">Вернуться назад</a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>