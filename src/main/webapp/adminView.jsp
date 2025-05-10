<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body>
<%-- 头部 (与之前代码一致，此处省略) --%>
<header class="admin-header">
    <div class="logo">学生管理系统</div>
    <nav class="main-nav">
        <ul>
            <li><a href="${pageContext.request.contextPath}/admin/dashboard">仪表盘</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/dataStats">数据统计</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/systemLogs">系统日志</a></li>
            <li><a href="${pageContext.request.contextPath}/logout" class="logout">退出登录</a></li>
        </ul>
    </nav>
</header>

<div class="admin-container">
    <!-- 侧边菜单 (与之前代码一致) -->
    <aside class="sidebar">
        <h3>功能导航</h3>
        <ul>
            <li data-module-id="student"><a href="#student">学生管理</a></li>
            <li data-module-id="course"><a href="#course">课程管理</a></li>
            <li data-module-id="leave"><a href="#leave">请假审批</a></li>
            <li data-module-id="credit"><a href="#credit">学分统计</a></li>
        </ul>
    </aside>

    <!-- 主要内容区域 -->
    <main class="admin-main">
        <%-- Flash 消息 (与之前代码一致) --%>
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success">
                <c:out value="${sessionScope.successMessage}" /><c:remove var="successMessage" scope="session" />
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger">
                <c:out value="${sessionScope.errorMessage}" /><c:remove var="errorMessage" scope="session" />
            </div>
        </c:if>
        <c:if test="${not empty requestScope.errorMessage && empty sessionScope.errorMessage}">
            <div class="alert alert-danger"><c:out value="${requestScope.errorMessage}" /></div>
        </c:if>

        <!-- 学生管理模块 (与之前代码一致，此处省略) -->
        <section id="student" class="module">
            <h2>学生管理</h2>
            <%-- ... 学生和班级列表的卡片 ... --%>
            <div class="card-container">
                <div class="card">
                    <div class="card-header">
                        <h3>班级列表</h3>
                        <form method="GET" action="${pageContext.request.contextPath}/admin/dashboard" class="actions">
                            <input type="hidden" name="activeModule" value="student">
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
                            <c:choose>
                                <c:when test="${not empty classList}">
                                    <c:forEach var="classInfo" items="${classList}">
                                        <tr>
                                            <td><c:out value="${classInfo.className}" /></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty classInfo.majorName}"><c:out value="${classInfo.majorName}" /></c:when>
                                                    <c:when test="${not empty classInfo.majorId}">专业ID: <c:out value="${classInfo.majorId}" /> (名称未找到)</c:when>
                                                    <c:otherwise>未指定专业</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty classInfo.collegeName}"><c:out value="${classInfo.collegeName}" /></c:when>
                                                    <c:when test="${not empty classInfo.collegeId}">学院ID: <c:out value="${classInfo.collegeId}" /> (名称未找到)</c:when>
                                                    <c:otherwise>未指定学院</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/deleteClass?id=${classInfo.classId}" class="action-link delete-link" onclick="return confirm('确定要删除该班级吗？此操作可能影响关联学生。');">删除</a>
                                                <a href="${pageContext.request.contextPath}/admin/viewClassInfoDetails?id=${classInfo.classId}" class="action-link">详情</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr><td colspan="4" style="text-align:center;">当前没有班级数据。</td></tr>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="card">
                    <div class="card-header">
                        <h3>学生列表</h3>
                        <form method="GET" action="${pageContext.request.contextPath}/admin/dashboard" class="actions">
                            <input type="hidden" name="activeModule" value="student">
                            <input type="text" name="searchStudentTerm" placeholder="学号/姓名/班级..." class="search-input" id="search-student-input" value="<c:out value='${searchStudentTerm}'/>">
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
                                <th data-sortable data-column-index="2" data-column-type="text" data-filterable-type="gender">性别</th>
                                <th data-sortable data-column-index="3" data-column-type="text">班级</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${not empty studentList}">
                                    <c:forEach var="student" items="${studentList}">
                                        <tr>
                                            <td><c:out value="${student.studentId}" /></td>
                                            <td><c:out value="${student.name}" /></td>
                                            <td><c:out value="${student.gender}" /></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty student.className}"><c:out value="${student.className}" /></c:when>
                                                    <c:when test="${not empty student.classId}">班级ID: <c:out value="${student.classId}" /> (名称未找到)</c:when>
                                                    <c:otherwise>未分配班级</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/editStudent?id=${student.studentId}" class="action-link">编辑</a>
                                                <a href="${pageContext.request.contextPath}/admin/deleteStudent?id=${student.studentId}" class="action-link delete-link" onclick="return confirm('确定要删除该学生吗？');">删除</a>
                                                <a href="${pageContext.request.contextPath}/admin/viewStudentDetails?id=${student.studentId}" class="action-link">详情</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr><td colspan="5" style="text-align:center;">当前没有学生数据。</td></tr>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </section>

        <!-- 课程管理模块 (与之前代码一致，此处省略) -->
        <section id="course" class="module">
            <h2>课程管理</h2>
            <%-- ... 课程列表的卡片 ... --%>
            <div class="card">
                <div class="card-header">
                    <h3>课程列表</h3>
                    <form method="GET" action="${pageContext.request.contextPath}/admin/dashboard" class="actions">
                        <input type="hidden" name="activeModule" value="course">
                        <input type="text" name="searchCourseTerm" placeholder="课程名/教师名..." class="search-input" id="search-course-input" value="<c:out value='${searchCourseTerm}'/>">
                        <button type="submit" class="btn btn-search">搜索课程</button>
                        <a href="${pageContext.request.contextPath}/admin/showAddCourseForm" class="btn btn-add">添加课程</a>
                    </form>
                </div>
                <div class="table-responsive">
                    <table class="data-table" id="course-list-table">
                        <thead>
                        <tr>
                            <th data-sortable data-column-index="0" data-column-type="numeric">课程ID</th>
                            <th data-sortable data-column-index="1" data-column-type="text">课程名称</th>
                            <th data-sortable data-column-index="2" data-column-type="text">授课教师</th>
                            <th data-sortable data-column-index="3" data-column-type="floatNumber">学分</th>
                            <th data-sortable data-column-index="4" data-column-type="text">所属专业</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty courseList}">
                                <c:forEach var="courseItem" items="${courseList}">
                                    <tr>
                                        <td><c:out value="${courseItem.courseId}" /></td>
                                        <td><c:out value="${courseItem.courseName}" /></td>
                                        <td><c:out value="${courseItem.courseTeacher}" /></td>
                                        <td><fmt:formatNumber value="${courseItem.credit}" pattern="#0.0" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty courseItem.majorName}"><c:out value="${courseItem.majorName}" /></c:when>
                                                <c:when test="${not empty courseItem.majorId}">专业ID: <c:out value="${courseItem.majorId}" /> (名称未找到)</c:when>
                                                <c:otherwise>全校选修或未指定</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/editCourse?id=${courseItem.courseId}" class="action-link">编辑</a>
                                            <a href="${pageContext.request.contextPath}/admin/deleteCourse?id=${courseItem.courseId}" class="action-link delete-link" onclick="return confirm('确定要删除该课程吗？');">删除</a>
                                            <a href="${pageContext.request.contextPath}/admin/viewCourseDetails?id=${courseItem.courseId}" class="action-link">详情</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="6" style="text-align:center;">当前没有课程数据。</td></tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <!-- 请假审批模块  -->
        <section id="leave" class="module">
            <h2>请假审批</h2>
            <%-- ... 请假列表的卡片 ... --%>
            <div class="card">
                <div class="card-header">
                    <h3>请假申请列表</h3>
                    <form method="GET" action="${pageContext.request.contextPath}/admin/dashboard" class="actions">
                        <input type="hidden" name="activeModule" value="leave">
                        <input type="text" name="searchLeaveStudentName" placeholder="申请学生姓名..." class="search-input" value="<c:out value='${searchLeaveStudentName}'/>">
                        <select name="searchLeaveStatus" class="search-input" style="min-width: 150px; flex-grow: 0;">
                            <option value="">-- 所有状态 --</option>
                            <option value="待审批" ${searchLeaveStatus eq '待审批' ? 'selected' : ''}>待审批</option>
                            <option value="已批准" ${searchLeaveStatus eq '已批准' ? 'selected' : ''}>已批准</option>
                            <option value="已驳回" ${searchLeaveStatus eq '已驳回' ? 'selected' : ''}>已驳回</option>
                        </select>
                        <button type="submit" class="btn btn-search">搜索请假</button>
                    </form>
                </div>
                <div class="table-responsive">
                    <table class="data-table" id="leave-request-list-table">
                        <thead>
                        <tr>
                            <th data-sortable data-column-index="0" data-column-type="numeric">申请ID</th>
                            <th data-sortable data-column-index="1" data-column-type="text">学生姓名</th>
                            <th data-sortable data-column-index="2" data-column-type="text">请假事由</th>
                            <th data-sortable data-column-index="3" data-column-type="date">开始日期</th>
                            <th data-sortable data-column-index="4" data-column-type="date">结束日期</th>
                            <th data-sortable data-column-index="5" data-column-type="text">状态</th>
                            <th data-sortable data-column-index="6" data-column-type="date">申请日期</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty leaveRequestList}">
                                <c:forEach var="leave" items="${leaveRequestList}">
                                    <tr>
                                        <td><c:out value="${leave.leaveId}" /></td>
                                        <td><c:out value="${leave.studentName}" /></td>
                                        <td><c:out value="${leave.reason}" /></td>
                                        <td><fmt:formatDate value="${leave.startDate}" pattern="yyyy-MM-dd" /></td>
                                        <td><fmt:formatDate value="${leave.endDate}" pattern="yyyy-MM-dd" /></td>
                                        <td>
                                                <span class="status-${leave.status eq '待审批' ? 'pending' : (leave.status eq '已批准' ? 'approved' : 'rejected')}">
                                                    <c:out value="${leave.status}" />
                                                </span>
                                        </td>
                                        <td><fmt:formatDate value="${leave.requestDate}" pattern="yyyy-MM-dd HH:mm" /></td>
                                        <td>
                                            <c:if test="${leave.status eq '待审批'}">
                                                <a href="${pageContext.request.contextPath}/admin/approveLeave?id=${leave.leaveId}" class="action-link btn-approve" onclick="return confirm('确定要批准该请假申请吗？');">批准</a>
                                                <a href="${pageContext.request.contextPath}/admin/rejectLeave?id=${leave.leaveId}" class="action-link btn-reject" onclick="return confirm('确定要驳回该请假申请吗？');">驳回</a>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="8" style="text-align:center;">当前没有请假申请数据。</td></tr>
                            </c:otherwise>
                        </c:choose>
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
                    <h3>学生学分列表</h3>
                    <%-- 学分统计搜索表单 --%>
                    <form method="GET" action="${pageContext.request.contextPath}/admin/dashboard" class="actions">
                        <input type="hidden" name="activeModule" value="credit"> <%-- 保持当前模块激活 --%>
                        <input type="text" name="searchCreditTerm" placeholder="学生姓名或学号..." class="search-input" value="<c:out value='${searchCreditTerm}'/>">
                        <button type="submit" class="btn btn-search">搜索</button>
                        <%-- 通常学分统计是查看，不直接“添加” --%>
                    </form>
                </div>
                <div class="table-responsive">
                    <table class="data-table" id="credit-summary-list-table">
                        <thead>
                        <tr>
                            <th data-sortable data-column-index="0" data-column-type="numeric">学生ID</th>
                            <th data-sortable data-column-index="1" data-column-type="text">学生姓名</th>
                            <th data-sortable data-column-index="2" data-column-type="numeric">已获总学分</th> <%-- 使用 floatNumber 类型 --%>
                            <th>操作</th> <%-- 例如：查看学分详情 --%>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty creditSummaryList}">
                                <c:forEach var="summary" items="${creditSummaryList}">
                                    <tr>
                                        <td><c:out value="${summary.studentId}" /></td>
                                        <td><c:out value="${summary.studentName}" /></td>
                                        <td><fmt:formatNumber value="${summary.totalCredits}" pattern="#0.0" /></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/viewStudentCreditDetails?studentId=${summary.studentId}" class="action-link">查看详情</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="4" style="text-align:center;">当前没有学分统计数据。</td></tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    </main>
</div>

<%-- 页脚 (与之前代码一致) --%>
<footer class="admin-footer">
    <p>© ${java.time.Year.now()} 学生管理系统 | 当前管理员：<c:out value="${sessionScope.username}"/></p>
</footer>
<script src="${pageContext.request.contextPath}/assets/js/admin.js"></script>
</body>
</html>