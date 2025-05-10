<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>添加新学生 - 管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <style>
        .form-container {
            max-width: 700px;
            margin: 2rem auto;
            padding: 2rem;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .form-container h2 {
            text-align: center;
            margin-bottom: 1.5rem;
            color: #333;
        }
        .form-group {
            margin-bottom: 1rem;
        }
        .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: bold;
            color: #555;
        }
        .form-group input[type="text"],
        .form-group input[type="password"],
        .form-group select {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 1rem;
        }
        .form-group .radio-group label {
            font-weight: normal;
            margin-right: 1rem;
        }
        .form-group .radio-group input[type="radio"] {
            margin-right: 0.3rem;
        }
        .form-actions {
            margin-top: 1.5rem;
            text-align: right;
        }
        .btn-submit {
            background-color: #28a745;
            color: white;
        }
        .btn-submit:hover {
            background-color: #218838;
        }
        .btn-cancel {
            background-color: #6c757d;
            color: white;
            margin-right: 0.5rem;
        }
        .btn-cancel:hover {
            background-color: #5a6268;
        }
    </style>
</head>
<body>
    <header class="admin-header">
        <div class="logo">学生管理系统 - 添加学生</div>
        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard#student">返回学生管理</a></li>
                <li><a href="#" class="logout">退出登录</a></li>
            </ul>
        </nav>
    </header>

    <div class="form-container">
        <h2>添加新学生</h2>
        <form method="POST" action="${pageContext.request.contextPath}/admin/saveStudent">
            <div class="form-group">
                <label for="name">学生姓名:</label>
                <input type="text" id="name" name="name" value="<c:out value='${param.name}'/>" required>
            </div>

            <div class="form-group">
                <label>性别:</label>
                <div class="radio-group">
                    <label><input type="radio" name="gender" value="男" ${param.gender == '男' ? 'checked' : ''} required> 男</label>
                    <label><input type="radio" name="gender" value="女" ${param.gender == '女' ? 'checked' : ''}> 女</label>
                </div>
            </div>

            <div class="form-group">
                <label for="userName">登录用户名:</label>
                <input type="text" id="userName" name="userName" value="<c:out value='${param.userName}'/>" required>
            </div>

            <div class="form-group">
                <label for="password">登录密码:</label>
                <input type="password" id="password" name="password" required>
            </div>

            <div class="form-group">
                <label for="classId">所属班级:</label>
                <select id="classId" name="classId">
                    <option value="">-- 请选择班级 (可选) --</option>
                    <c:if test="${not empty classList}">
                        <c:forEach var="classInfo" items="${classList}">
                            <option value="${classInfo.classId}" ${param.classId == classInfo.classId ? 'selected' : ''}>
                                <c:out value="${classInfo.className}" />
                                <c:if test="${not empty classInfo.majorName}"> (<c:out value="${classInfo.majorName}" />)</c:if>
                            </option>
                        </c:forEach>
                    </c:if>
                    <c:if test="${empty classList && empty errorMessage}">
                        <option value="" disabled>没有可用的班级信息</option>
                    </c:if>
                </select>
                 <small>如果不选择，则学生不分配到具体班级。</small>
            </div>
            
            <c:if test="${not empty requestScope.errorMessage}">
                <p style="color: red; margin-bottom: 1rem;"><c:out value="${requestScope.errorMessage}" /></p>
            </c:if>
            <c:if test="${not empty requestScope.successMessage}"> <%-- 通常成功消息在重定向后通过 sessionScope 显示 --%>
                <p style="color: green; margin-bottom: 1rem;"><c:out value="${requestScope.successMessage}" /></p>
            </c:if>


            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/admin/dashboard#student" class="btn btn-cancel">取消</a>
                <button type="submit" class="btn btn-submit">保存学生</button>
            </div>
        </form>
    </div>

    <footer class="admin-footer">
        <p>&copy; 2025 学生管理系统 | 当前管理员：${sessionScope.username}</p>
    </footer>
</body>
</html>