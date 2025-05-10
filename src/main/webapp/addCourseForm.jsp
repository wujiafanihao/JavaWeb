<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %> <%-- 导入 JSTL core 标签库 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>添加新课程 - 管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css"> <%-- 复用 admin.css --%>
    <style>
        /* 针对此表单页面的额外或覆盖样式 */
        .form-container {
            max-width: 600px; /* 表单容器最大宽度 */
            margin: 2rem auto; /* 上下间距2rem，左右自动居中 */
            padding: 2rem; /* 内边距 */
            background: #fff; /* 白色背景 */
            border-radius: 8px; /* 圆角 */
            box-shadow: 0 2px 10px rgba(0,0,0,0.1); /* 阴影效果 */
        }
        .form-container h2 {
            text-align: center; /* 标题居中 */
            margin-bottom: 1.5rem; /* 标题与表单内容间距 */
            color: #333; /* 标题颜色 */
        }
        .form-group {
            margin-bottom: 1rem; /* 表单组之间的间距 */
        }
        .form-group label {
            display: block; /* 标签独占一行 */
            margin-bottom: 0.5rem; /* 标签与输入框间距 */
            font-weight: bold; /* 字体加粗 */
            color: #555; /* 标签颜色 */
        }
        .form-group input[type="text"],
        .form-group input[type="number"], /* 为学分输入框添加样式 */
        .form-group select { /* 为 select 添加统一样式 */
            width: 100%; /* 输入框/选择框宽度占满容器 */
            padding: 0.75rem; /* 内边距 */
            border: 1px solid #ccc; /* 边框 */
            border-radius: 4px; /* 圆角 */
            font-size: 1rem; /* 字体大小 */
            background-color: white; /* 背景色 */
        }
        .form-actions {
            margin-top: 1.5rem; /* 操作按钮区域与上方元素间距 */
            text-align: right; /* 按钮右对齐 */
        }
        .btn-submit {
            background-color: #28a745; /* 绿色提交按钮 (与addClassForm一致) */
            color: white;
        }
        .btn-submit:hover {
            background-color: #218838; /* 鼠标悬停时颜色变深 */
        }
        .btn-cancel {
            background-color: #6c757d; /* 灰色取消按钮 (与addClassForm一致) */
            color: white;
            margin-right: 0.5rem; /* 与提交按钮的间距 */
        }
        .btn-cancel:hover {
            background-color: #5a6268; /* 鼠标悬停时颜色变深 */
        }
        /* 错误和成功消息的样式 */
        .message-text {
            margin-bottom: 1rem;
            padding: 0.75rem;
            border-radius: 4px;
            text-align: center;
        }
        .error-text {
            color: #721c24;
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
        }
        .success-text {
            color: #155724;
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
        }
    </style>
</head>
<body>
<%-- 页面头部 --%>
<header class="admin-header">
    <div class="logo">学生管理系统 - 添加课程</div>
    <nav class="main-nav">
        <ul>
            <li><a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=course">返回课程管理</a></li>
            <li><a href="${pageContext.request.contextPath}/logout" class="logout">退出登录</a></li>
        </ul>
    </nav>
</header>

<%-- 表单容器 --%>
<div class="form-container">
    <h2>添加新课程</h2>
    <form method="POST" action="${pageContext.request.contextPath}/admin/saveCourse">
        <%-- 课程名称 --%>
        <div class="form-group">
            <label for="courseName">课程名称:</label>
            <input type="text" id="courseName" name="courseName" value="<c:out value='${param.courseName}'/>" required>
        </div>

        <%-- 授课教师 --%>
        <div class="form-group">
            <label for="courseTeacher">授课教师:</label>
            <input type="text" id="courseTeacher" name="courseTeacher" value="<c:out value='${param.courseTeacher}'/>" required>
        </div>

        <%-- 学分 --%>
        <div class="form-group">
            <label for="credit">学分:</label>
            <input type="number" id="credit" name="credit" step="0.1" min="0" max="20" value="<c:out value='${param.credit}'/>" required>
            <small>请输入0到20之间的数字，可包含一位小数。</small>
        </div>

        <%-- 所属专业 (下拉选择) --%>
        <div class="form-group">
            <label for="majorName">所属专业:</label> <%-- 注意：这里我们让用户选择专业名称，然后在后端根据名称查找ID --%>
            <select id="majorName" name="majorName">
                <option value="">-- 请选择专业 (可选, 留空表示全校选修) --</option>
                <c:if test="${not empty majorListFromServlet}"> <%-- 使用不同的变量名以区分 --%>
                    <c:forEach var="major" items="${majorListFromServlet}">
                        <option value="${major.majorName}" ${param.majorName eq major.majorName ? 'selected' : ''}>
                            <c:out value="${major.majorName}" />
                        </option>
                    </c:forEach>
                </c:if>
                <c:if test="${empty majorListFromServlet && not empty errorMessageForMajorList}">
                    <option value="" disabled><c:out value="${errorMessageForMajorList}"/></option>
                </c:if>
                <c:if test="${empty majorListFromServlet && empty errorMessageForMajorList}">
                    <option value="" disabled>没有可用的专业信息</option>
                </c:if>
            </select>
            <small>如果留空，课程将不与特定专业关联（如全校公共选修课）。</small>
        </div>

        <%-- 显示可能的错误消息 (通常是表单提交验证失败后由Servlet设置) --%>
        <c:if test="${not empty requestScope.errorMessage}">
            <p class="message-text error-text"><c:out value="${requestScope.errorMessage}" /></p>
        </c:if>
        <%-- 显示可能的成功消息 (通常是操作成功后由Servlet通过会话设置，然后重定向前请求转发回来显示一次) --%>
        <%-- 或者像SaveClassServlet那样，通过sessionScope在重定向后显示 --%>
        <c:if test="${not empty requestScope.successMessage}">
            <p class="message-text success-text"><c:out value="${requestScope.successMessage}" /></p>
        </c:if>

        <%-- 操作按钮 --%>
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=course" class="btn btn-cancel">取消</a>
            <button type="submit" class="btn btn-submit">保存课程</button>
        </div>
    </form>
</div>

<%-- 页面页脚 --%>
<footer class="admin-footer">
    <p>© ${java.time.Year.now()} 学生管理系统 | 当前管理员：<c:out value="${sessionScope.username}"/></p>
</footer>
</body>
</html>