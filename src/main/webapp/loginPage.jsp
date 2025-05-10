<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>学生信息管理系统</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/login.css">
    <%-- 步骤 1: 引入 Crypto-JS 库用于哈希计算 --%>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/crypto-js/4.1.1/crypto-js.min.js"></script>
    <style>
        /* 确保提示信息可见 */
        .messagePopup {
            padding: 10px 15px;
            margin-bottom: 20px;
            border: 1px solid transparent;
            border-radius: 4px;
            text-align: center;
            font-weight: bold;
        }
        .successMessage {
            color: #155724;
            background-color: #d4edda;
            border-color: #c3e6cb;
        }
        .errorMessage {
            color: #721c24;
            background-color: #f8d7da;
            border-color: #f5c6cb;
        }
    </style>
</head>
<body>
<%-- 处理登出成功提示 --%>
<%
    String logoutParam = request.getParameter("logout");
    if ("true".equals(logoutParam)) {
%>
<p id="logoutMessage" class="messagePopup successMessage">您已成功退出登录！</p>
<%
    }
%>

<%-- 处理登录错误提示 --%>
<%-- 注意：您这里显示了两次错误信息，一次在这里，一次在表单里。建议只保留一个。--%>
<%-- 我暂时保留这里的，并让JS控制它 --%>
<%
    String error = (String) request.getAttribute("error");
    if (error != null && !error.isEmpty()) {
%>
<p id="errorMessage" class="messagePopup errorMessage"><%= error %></p>
<%
    }
%>

<div class="center-container">
    <h1>学生信息管理系统</h1>
    <%-- 步骤 2: 给 Form 添加 ID --%>
    <form id="loginForm" action="login" method="POST" class="login-form">

        <%-- 这个错误信息显示，建议与上面的合并，或者只保留一个 --%>
        <%-- <% if (request.getAttribute("error") != null) { %>
           <div class="error-message"><%= request.getAttribute("error") %></div>
        <% } %> --%>

        <div class="form-group">
            <label for="usernameInput">用户名:</label>
            <%-- 给输入框添加唯一 ID --%>
            <input type="text" id="usernameInput" name="username" required>
        </div>

        <div class="form-group">
            <label for="passwordInput">密   码:</label>
            <%-- 步骤 3: 修改密码框，使用ID，更改 NAME 属性 --%>
            <input type="password" id="passwordInput" name="passwordOriginal" required>
        </div>

        <%-- 步骤 4: 添加隐藏字段，用于提交哈希后的密码 --%>
        <input type="hidden" id="passwordHashedInput" name="password">

        <button type="submit" class="login-button">登录</button>
    </form>
</div>
<%-- 步骤 5: 确保引入了你的 JS 文件 --%>
<script src="${pageContext.request.contextPath}/assets/js/login.js"></script>
</body>
</html>