<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>学生信息管理系统</title>
    <link rel="stylesheet" type="text/css" href="assets/css/login.css">
</head>
<body>
<div class="center-container">
    <h1>学生信息管理系统录</h1>
    <form action="login" method="POST" class="login-form"> <!-- 将 action 修改为 "login" -->
        <% if (request.getAttribute("error") != null) { %>
        <div class="error-message"><%= request.getAttribute("error") %></div>
        <% } %>

        <div class="form-group">
            <label for="username">用户名:</label>
            <input type="text" id="username" name="username" required>
        </div>

        <div class="form-group">
            <label for="password">密&nbsp;&nbsp;&nbsp;码:</label>
            <input type="password" id="password" name="password" required>
        </div>

        <button type="submit" class="login-button">登录</button>
    </form>
</div>

</body>
</html>