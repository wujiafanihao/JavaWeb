<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学生个人中心 - <c:out value="${studentDetails.name}"/></title>
    <%-- 可以创建一个新的 student.css 或者复用/扩展 admin.css --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/student.css">
    <%-- 如果 student.css 不存在，可以先链接 admin.css 作为基础 --%>
    <%-- <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css"> --%>
    <style>
        /* studentView.jsp 特定内联样式 (如果需要快速调整) */
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f7f6;
            color: #333;
            margin: 0;
            padding: 0;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        .student-header {
            background: #0056b3; /* 学生界面的主色调可以不同 */
            color: white;
            padding: 1rem 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .student-header .logo { font-size: 1.5em; font-weight: bold; }
        .student-header .user-info { font-size: 0.9em; }
        .student-header a.logout { color: #ffdddd; text-decoration: none; margin-left: 15px;}
        .student-header a.logout:hover { color: #fff; text-decoration: underline;}

        .student-container {
            max-width: 1100px;
            margin: 2rem auto;
            padding: 1.5rem;
            display: flex;
            flex-direction: column;
            gap: 2rem; /* 各个功能区之间的间距 */
        }
        .content-card {
            background: white;
            padding: 1.5rem 2rem;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.07);
        }
        .content-card h2 {
            color: #0056b3;
            margin-top: 0;
            margin-bottom: 1.5rem;
            padding-bottom: 0.75rem;
            border-bottom: 1px solid #e0e0e0;
        }
        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem; margin-bottom:1rem;}
        .info-grid p, .info-list li { margin-bottom: 0.6rem; line-height: 1.6; }
        .info-grid strong, .info-list strong { color: #333; min-width: 100px; display: inline-block; }
        
        .table-responsive { overflow-x: auto; }
        .data-table { width: 100%; border-collapse: collapse; font-size: 0.9em; }
        .data-table th, .data-table td { padding: 0.8rem 1rem; text-align: left; border-bottom: 1px solid #eee; }
        .data-table thead th { background-color: #f0f5fa; color: #0056b3; font-weight: 600; }
        .data-table tbody tr:hover { background-color: #f9f9f9; }

        .form-group { margin-bottom: 1rem; }
        .form-group label { display: block; margin-bottom: 0.3rem; font-weight: 500; }
        .form-control {
            width: 100%;
            padding: 0.6rem 0.8rem;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }
        textarea.form-control { min-height: 80px; }
        .btn {
            padding: 0.6rem 1.2rem;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            font-size: 0.9rem;
            background-color: #007bff; color: white;
        }
        .btn:hover { background-color: #0056b3; }
        .btn-action { font-size: 0.85em; padding: 0.3rem 0.6rem; }

        .status-待审批 { color: #ffa500; font-weight: bold; } /* 橙色 */
        .status-已批准 { color: #28a745; font-weight: bold; } /* 绿色 */
        .status-已驳回 { color: #dc3545; font-weight: bold; } /* 红色 */

        .student-footer {
            text-align: center;
            padding: 1.5rem;
            background: #343a40;
            color: white;
            margin-top: auto; /* 固定到底部 */
        }
    </style>
</head>
<body>
    <header class="student-header">
        <div class="logo">学生个人中心</div>
        <div class="user-info">
            欢迎您，<c:out value="${studentDetails.name}"/> (<c:out value="${sessionScope.username}"/>)
            <a href="${pageContext.request.contextPath}/logout" class="logout">退出登录</a>
        </div>
    </header>

    <div class="student-container">
        <%-- Flash Messages --%>
        <c:if test="${not empty successMessage}">
            <div style="padding: 10px; margin-bottom: 15px; border: 1px solid green; background-color: #e6ffe6; color: green;">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div style="padding: 10px; margin-bottom: 15px; border: 1px solid red; background-color: #ffe6e6; color: red;">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>
         <c:if test="${not empty pageError}">
            <div style="padding: 10px; margin-bottom: 15px; border: 1px solid red; background-color: #ffe6e6; color: red;">
                <c:out value="${pageError}"/>
            </div>
        </c:if>

        <%-- 个人信息展示 --%>
        <section id="personal-info" class="content-card">
            <h2>我的信息</h2>
            <c:if test="${not empty studentDetails}">
                <div class="info-grid">
                    <p><strong>学号:</strong> <c:out value="${studentDetails.studentId}"/></p>
                    <p><strong>姓名:</strong> <c:out value="${studentDetails.name}"/></p>
                    <p><strong>性别:</strong> <c:out value="${studentDetails.gender}"/></p>
                    <p><strong>登录用户名:</strong> <c:out value="${studentDetails.userName}"/></p>
                    <p><strong>班级:</strong> <c:out value="${not empty studentDetails.className ? studentDetails.className : '未分配'}"/></p>
                    <p><strong>专业:</strong> <c:out value="${not empty studentDetails.majorName ? studentDetails.majorName : '未分配'}"/></p>
                    <p><strong>学院:</strong> <c:out value="${not empty studentDetails.collegeName ? studentDetails.collegeName : '未分配'}"/></p>
                    <p><strong>已获总学分:</strong> <fmt:formatNumber value="${studentDetails.totalEarnedCredits ne null ? studentDetails.totalEarnedCredits : 0}" pattern="#0.0"/></p>
                </div>

                <h3>已选课程及成绩</h3>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>课程名称</th>
                                <th>授课教师</th>
                                <th>学分</th>
                                <th>我的成绩</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty studentDetails.enrollments}">
                                    <c:forEach var="enrollment" items="${studentDetails.enrollments}">
                                        <tr>
                                            <td><c:out value="${enrollment.course.courseName}"/></td>
                                            <td><c:out value="${enrollment.course.courseTeacher}"/></td>
                                            <td><fmt:formatNumber value="${enrollment.course.credit}" pattern="#0.0"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${enrollment.grade ne null}">
                                                        <fmt:formatNumber value="${enrollment.grade}" pattern="#0.00"/>
                                                    </c:when>
                                                    <c:otherwise>未录入</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr><td colspan="4" style="text-align: center;">暂无已选课程记录。</td></tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:if>
            <c:if test="${empty studentDetails && empty pageError}">
                 <p>无法加载您的个人信息。</p>
            </c:if>
        </section>

        <%-- 请假功能区 --%>
        <section id="leave-management" class="content-card">
            <h2>我的请假</h2>
            <%-- 申请请假表单 --%>
            <h3>提交新的请假申请</h3>
            <form action="${pageContext.request.contextPath}/student/submitLeave" method="POST">
                <div class="form-group">
                    <label for="leaveReason">请假事由:</label>
                    <textarea id="leaveReason" name="reason" class="form-control" rows="3" required></textarea>
                </div>
                <div style="display: flex; gap: 1rem;">
                    <div class="form-group" style="flex: 1;">
                        <label for="leaveStartDate">开始日期:</label>
                        <input type="date" id="leaveStartDate" name="startDate" class="form-control" required>
                    </div>
                    <div class="form-group" style="flex: 1;">
                        <label for="leaveEndDate">结束日期:</label>
                        <input type="date" id="leaveEndDate" name="endDate" class="form-control" required>
                    </div>
                </div>
                <button type="submit" class="btn">提交申请</button>
            </form>

            <h3 style="margin-top: 2rem;">历史请假记录</h3>
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>申请ID</th>
                            <th>事由</th>
                            <th>开始日期</th>
                            <th>结束日期</th>
                            <th>状态</th>
                            <th>申请日期</th>
                            <th>审批日期</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty leaveRequestList}">
                                <c:forEach var="leave" items="${leaveRequestList}">
                                    <tr>
                                        <td><c:out value="${leave.leaveId}"/></td>
                                        <td><c:out value="${leave.reason}"/></td>
                                        <td><fmt:formatDate value="${leave.startDate}" pattern="yyyy-MM-dd"/></td>
                                        <td><fmt:formatDate value="${leave.endDate}" pattern="yyyy-MM-dd"/></td>
                                        <td><span class="status-${leave.status eq '待审批' ? '待审批' : (leave.status eq '已批准' ? '已批准' : '已驳回')}"><c:out value="${leave.status}"/></span></td>
                                        <td><fmt:formatDate value="${leave.requestDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td>
                                            <c:if test="${not empty leave.approvalDate}">
                                                <fmt:formatDate value="${leave.approvalDate}" pattern="yyyy-MM-dd HH:mm"/>
                                            </c:if>
                                            <c:if test="${empty leave.approvalDate && leave.status ne '待审批'}">已处理</c:if>
                                            <c:if test="${empty leave.approvalDate && leave.status eq '待审批'}"> - </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="7" style="text-align: center;">暂无历史请假记录。</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <%-- 选课功能区 --%>
        <section id="course-selection" class="content-card">
            <h2>在线选课</h2>
            <h3>可选课程列表</h3>
            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>课程ID</th>
                            <th>课程名称</th>
                            <th>授课教师</th>
                            <th>学分</th>
                            <th>所属专业</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty availableCoursesList}">
                                <c:forEach var="course" items="${availableCoursesList}">
                                    <tr>
                                        <td><c:out value="${course.courseId}"/></td>
                                        <td><c:out value="${course.courseName}"/></td>
                                        <td><c:out value="${course.courseTeacher}"/></td>
                                        <td><fmt:formatNumber value="${course.credit}" pattern="#0.0"/></td>
                                        <td><c:out value="${not empty course.majorName ? course.majorName : '全校选修'}"/></td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/student/enrollCourse" method="POST" style="display:inline;">
                                                <input type="hidden" name="courseId" value="${course.courseId}">
                                                <button type="submit" class="btn btn-action">选修此课程</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="6" style="text-align: center;">暂无可选择的课程或所有课程均已选修。</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

    </div>

    <footer class="student-footer">
        <p>© ${java.time.Year.now()} 学生管理系统</p>
    </footer>
    <script>
        // 简单的日期校验，确保结束日期不早于开始日期
        const startDateInput = document.getElementById('leaveStartDate');
        const endDateInput = document.getElementById('leaveEndDate');

        if(startDateInput && endDateInput) {
            endDateInput.addEventListener('change', function() {
                if (startDateInput.value && this.value && new Date(this.value) < new Date(startDateInput.value)) {
                    alert('结束日期不能早于开始日期！');
                    this.value = ''; // 清空结束日期
                }
            });
            startDateInput.addEventListener('change', function() {
                 if (endDateInput.value && this.value && new Date(endDateInput.value) < new Date(this.value)) {
                    alert('开始日期不能晚于结束日期！');
                    this.value = ''; // 清空开始日期
                }
            });
        }
    </script>
</body>
</html>