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
        .form-group input[type="number"],
        .form-group select { /* 为 select 添加统一样式 */
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 1rem;
            background-color: white; /* 确保背景色与其他输入框一致 */
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
                <label for="majorId">所属专业:</label> <%-- 修改标签文本 --%>
                <select id="majorId" name="majorId">
                    <option value="">-- 请选择专业 (可选) --</option> <%-- 修改默认选项文本 --%>
                    <c:if test="${not empty majorList}"> <%-- 判断 majorList 是否为空 --%>
                        <c:forEach var="major" items="${majorList}"> <%-- 迭代 majorList --%>
                            <option value="${major.majorId}" ${param.majorId == major.majorId ? 'selected' : ''}>
                                <c:out value="${major.majorName}" /> <%-- 显示专业名称 --%>
                            </option>
                        </c:forEach>
                    </c:if>
                    <c:if test="${empty majorList && empty errorMessageFromServlet}"> <%-- 判断 majorList 是否为空，并使用不同的错误变量名以区分 --%>
                        <%-- errorMessageFromServlet 是假设 Servlet 在加载专业列表失败时设置的特定错误消息 --%>
                        <%-- 如果只是单纯没有专业数据，则显示下面的 "没有可用的专业信息" --%>
                        <option value="" disabled>没有可用的专业信息</option>
                    </c:if>
                </select>
                <small>如果留空，则班级不关联特定专业。</small>
            </div>
            
            <%-- 显示可能的错误消息 (这个 errorMessage 通常是针对表单提交验证的) --%>
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