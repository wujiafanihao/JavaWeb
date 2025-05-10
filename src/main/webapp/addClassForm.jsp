<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>添加新班级 - 管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css"> <%-- 复用 admin.css --%>
    <style>
        /* 针对此表单页面的额外或覆盖样式 */
        .form-container {
            max-width: 600px;
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
        .form-group input[type="number"] { /* 如果专业ID用number类型 */
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 1rem;
        }
        .form-actions {
            margin-top: 1.5rem;
            text-align: right;
        }
        .btn-submit {
            background-color: #28a745; /* 绿色提交按钮 */
            color: white;
        }
        .btn-submit:hover {
            background-color: #218838;
        }
        .btn-cancel {
            background-color: #6c757d; /*灰色取消按钮 */
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
        <div class="logo">学生管理系统 - 添加班级</div>
        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard">返回仪表盘</a></li>
                <li><a href="#" class="logout">退出登录</a></li>
            </ul>
        </nav>
    </header>

    <div class="form-container">
        <h2>添加新班级</h2>
        <form method="POST" action="${pageContext.request.contextPath}/admin/saveClass">
            <div class="form-group">
                <label for="className">班级名称:</label>
                <input type="text" id="className" name="className" required>
            </div>

            <div class="form-group">
                <label for="majorId">所属专业ID:</label>
                <%-- TODO: 将来这里应该是一个从数据库加载的专业下拉列表 --%>
                <%-- 目前暂时使用文本框输入专业ID --%>
                <input type="number" id="majorId" name="majorId" placeholder="输入专业ID (可选)">
                <small>如果留空，则班级不关联特定专业。</small>
            </div>
            
            <%-- 显示可能的错误消息 --%>
            <c:if test="${not empty errorMessage}">
                <p style="color: red; margin-bottom: 1rem;"><c:out value="${errorMessage}" /></p>
            </c:if>
            <%-- 显示可能的成功消息 --%>
            <c:if test="${not empty successMessage}">
                <p style="color: green; margin-bottom: 1rem;"><c:out value="${successMessage}" /></p>
            </c:if>

            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-cancel">取消</a>
                <button type="submit" class="btn btn-submit">保存班级</button>
            </div>
        </form>
    </div>

    <footer class="admin-footer">
        <p>&copy; 2025 学生管理系统 | 当前管理员：${sessionScope.username}</p>
    </footer>
</body>
</html>