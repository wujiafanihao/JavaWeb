<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %> <%-- 导入 JSTL core 标签库 --%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <%-- 移除了 Chart.js 的 script 引入 --%>
    <%-- 移除了自定义的 JavaScript 块 (showModule 等) --%>
</head>
<body>
<header class="admin-header">
    <div class="logo">学生管理系统</div>
    <nav class="main-nav">
        <ul>
            <li><a href="#">仪表盘</a></li>
            <li><a href="#">数据统计</a></li>
            <li><a href="#">系统日志</a></li>
            <li><a href="#" class="logout">退出登录</a></li>
        </ul>
    </nav>
</header>

<div class="admin-container">
    <!-- 侧边菜单 -->
    <aside class="sidebar">
        <h3>功能导航</h3>
        <ul>
            <%-- 侧边栏链接现在是简单的锚点链接 --%>
            <li class="active"><a href="#student">学生管理</a></li>
            <li><a href="#course">课程管理</a></li>
            <li><a href="#leave">请假审批</a></li>
            <li><a href="#credit">学分统计</a></li>
        </ul>
    </aside>

    <!-- 主要内容区域 -->
    <main class="admin-main">
        <%-- 显示会话中的成功消息 (Flash Message) --%>
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success">
                <c:out value="${sessionScope.successMessage}" />
                <c:remove var="successMessage" scope="session" /> <%-- 显示后从会话中移除 --%>
            </div>
        </c:if>

        <%-- 显示会话中的错误消息 (Flash Message) --%>
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger">
                <c:out value="${sessionScope.errorMessage}" />
                <c:remove var="errorMessage" scope="session" /> <%-- 显示后从会话中移除 --%>
            </div>
        </c:if>

        <!-- 学生管理模块 -->
        <section id="student" class="module active"> <%-- 默认学生模块为 active (可见) --%>
            <h2>学生管理</h2>
            <div class="card-container">
                <div class="card">
                    <div class="card-header">
                        <h3>班级列表</h3>
                        <form method="GET" action="${pageContext.request.contextPath}/admin/dashboard" class="actions">
                            <input type="text" name="searchClassTerm" placeholder="班级/专业/学院名..." class="search-input" id="search-class-input" value="<c:out value='${searchClassTerm}'/>">
                            <button type="submit" class="btn btn-search">搜索班级</button>
                            <a href="${pageContext.request.contextPath}/admin/showAddClassForm" class="btn btn-add">添加班级</a>
                        </form>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table" id="class-list-table">
                            <thead>
                            <tr>
                                <th data-sortable data-column-index="0" data-column-type="text">班级名称</th>
                                <th data-sortable data-column-index="1" data-column-type="text">专业名称</th>
                                <th data-sortable data-column-index="2" data-column-type="text">所属学院</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                                <c:if test="${empty classList and empty errorMessage}">
                                    <tr>
                                        <td colspan="4" style="text-align:center;">当前没有班级数据。</td>
                                    </tr>
                                </c:if>
                                <c:if test="${not empty classList}">
                                    <c:forEach var="classInfo" items="${classList}">
                                        <tr>
                                            <td><c:out value="${classInfo.className}" /></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty classInfo.majorName}">
                                                        <c:out value="${classInfo.majorName}" />
                                                    </c:when>
                                                    <c:when test="${not empty classInfo.majorId}">
                                                        专业ID: <c:out value="${classInfo.majorId}" /> (名称未找到)
                                                    </c:when>
                                                    <c:otherwise>
                                                        未指定专业
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty classInfo.collegeName}">
                                                        <c:out value="${classInfo.collegeName}" />
                                                    </c:when>
                                                    <c:when test="${not empty classInfo.collegeId}">
                                                        学院ID: <c:out value="${classInfo.collegeId}" /> (名称未找到)
                                                    </c:when>
                                                    <c:otherwise>
                                                        未指定学院
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/editClass?id=${classInfo.classId}" class="action-link">编辑</a>
                                                <a href="${pageContext.request.contextPath}/admin/deleteClass?id=${classInfo.classId}" class="action-link delete-link" onclick="return confirm('确定要删除该班级吗？');">删除</a>
                                                <a href="${pageContext.request.contextPath}/admin/viewCLassDetails?id=${classInfo.classId}" class="action-link">详情</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:if>
                                <%-- 如果 Servlet 传递了针对班级列表的特定错误消息，可以在这里显示 --%>
                                <%-- 或者统一使用页面底部的 errorMessage --%>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="card">
                     <div class="card-header">
                        <h3>学生列表</h3>
                        <form method="GET" action="${pageContext.request.contextPath}/admin/dashboard" class="actions">
                            <input type="text" name="searchStudentTerm" placeholder="学生姓名/班级名..." class="search-input" id="search-student-input" value="<c:out value='${searchStudentTerm}'/>">
                            <button type="submit" class="btn btn-search">搜索学生</button>
                            <a href="${pageContext.request.contextPath}/admin/showAddStudentForm" class="btn btn-add">添加学生</a>
                        </form>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table" id="student-list-table">
                            <thead>
                            <tr>
                                <th data-sortable data-column-index="0" data-column-type="numeric">学号</th>
                                <th data-sortable data-column-index="1" data-column-type="text">姓名</th>
                                <th data-sortable data-column-index="2" data-column-type="text" data-filterable-type="gender">性别</th> <%-- 先按文本排序，后续可加特殊筛选 --%>
                                <th data-sortable data-column-index="3" data-column-type="text">班级</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                                <%-- 使用 JSTL 遍历由 Servlet 在请求作用域中设置的 studentList --%>
                                <c:if test="${empty studentList and empty errorMessage}">
                                    <tr>
                                        <td colspan="5" style="text-align:center;">当前没有学生数据。</td>
                                    </tr>
                                </c:if>
                                <c:if test="${not empty studentList}">
                                    <c:forEach var="student" items="${studentList}">
                                        <tr>
                                            <td><c:out value="${student.studentId}" /></td>
                                            <td><c:out value="${student.name}" /></td>
                                            <td><c:out value="${student.gender}" /></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty student.className}">
                                                        <c:out value="${student.className}" />
                                                    </c:when>
                                                    <c:when test="${not empty student.classId}">
                                                        班级ID: <c:out value="${student.classId}" /> (名称未找到)
                                                    </c:when>
                                                    <c:otherwise>
                                                        未分配班级
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/editStudent?id=${student.studentId}" class="action-link">编辑</a>
                                                <a href="${pageContext.request.contextPath}/admin/deleteStudent?id=${student.studentId}" class="action-link delete-link" onclick="return confirm('确定要删除该学生吗？');">删除</a>
                                                <a href="${pageContext.request.contextPath}/admin/viewStudentDetails?id=${student.studentId}" class="action-link">详情</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:if>
                                <c:if test="${not empty errorMessage}">
                                    <tr>
                                        <td colspan="5" style="text-align:center; color: red;"><c:out value="${errorMessage}" /></td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </section>

        <!-- 课程管理模块 -->
        <section id="course" class="module">  <%-- 默认其他模块可以是非 active (如果CSS中有对应隐藏规则) --%>
            <h2>课程管理</h2>
            <div class="card">
                 <div class="card-header">
                    <h3>课程列表</h3>
                    <div class="actions">
                        <input type="text" placeholder="搜索课程..." class="search-input" id="search-course-input">
                        <button class="btn btn-search" onclick="searchCourses()">搜索</button>
                        <button class="btn btn-add" onclick="addCourse()">添加课程</button>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>课程ID</th><th>课程名称</th><th>学分</th><th>授课教师</th><th>所属专业ID</th><th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                         <tr><td colspan="6" style="text-align:center;">当前没有课程数据。</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <!-- 请假审批模块 -->
        <section id="leave" class="module">
            <h2>请假审批</h2>
            <div class="card">
                <div class="card-header">
                    <h3>待审批列表</h3>
                     <div class="actions">
                        <input type="text" placeholder="搜索请假记录..." class="search-input" id="search-leave-input">
                        <button class="btn btn-search" onclick="searchLeaveRequests()">搜索</button>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>申请编号</th><th>学生姓名</th><th>请假事由</th><th>开始日期</th><th>结束日期</th><th>状态</th><th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr><td colspan="7" style="text-align:center;">当前没有请假数据。</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <!-- 学分统计模块 -->
        <section id="credit" class="module">
            <h2>学分统计</h2>
            <div class="card">
                 <div class="card-header">
                    <h3>学分概览</h3>
                     <div class="actions">
                        <input type="text" placeholder="搜索学生学分..." class="search-input" id="search-credit-input">
                        <button class="btn btn-search" onclick="searchCredits()">搜索</button>
                    </div>
                </div>
                <%-- canvas for chart.js was here --%>
                <p style="text-align:center; padding:20px;">学分图表区域</p>
            </div>
        </section>
    </main>
</div>

<footer class="admin-footer">
    <p>&copy; 2025 学生管理系统 | 当前管理员：${sessionScope.username}</p>
</footer>
<script src="${pageContext.request.contextPath}/assets/js/admin.js"></script>
</body>
</html>