<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>详情信息 - 管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <style>
        .detail-container {
            max-width: 800px;
            margin: 2rem auto;
            padding: 2rem;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .detail-container h2 {
            text-align: center;
            margin-bottom: 1.5rem;
            color: #2c3e50;
            border-bottom: 1px solid #eee;
            padding-bottom: 1rem;
        }
        .detail-section {
            margin-bottom: 1.5rem;
        }
        .detail-section h3 {
            color: #34495e;
            margin-bottom: 0.75rem;
            font-size: 1.2em;
        }
        .detail-section p, .detail-section li {
            margin-bottom: 0.5rem;
            line-height: 1.6;
        }
        .detail-section strong {
            color: #2c3e50;
            min-width: 120px;
            display: inline-block;
        }
        .detail-actions {
            margin-top: 2rem;
            text-align: center;
        }
        .student-enrollments-table th, .student-enrollments-table td {
            padding: 0.5rem;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <header class="admin-header">
        <div class="logo">学生管理系统 - 详细信息</div>
        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=${not empty activeModule ? activeModule : 'student'}#${not empty activeModule ? activeModule : 'student'}">返回仪表盘</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="logout">退出登录</a></li>
            </ul>
        </nav>
    </header>

    <div class="detail-container">
        <c:choose>
            <%-- 班级详情显示 --%>
            <c:when test="${viewMode eq 'classDetails' and not empty detailsData}">
                <h2>班级详情: <c:out value="${detailsData.className}"/></h2>
                <div class="detail-section">
                    <p><strong>班级ID:</strong> <c:out value="${detailsData.classId}"/></p>
                    <p><strong>班级名称:</strong> <c:out value="${detailsData.className}"/></p>
                    <p><strong>所属专业:</strong> <c:out value="${not empty detailsData.majorName ? detailsData.majorName : '未指定'}"/></p>
                    <p><strong>所属学院:</strong> <c:out value="${not empty detailsData.collegeName ? detailsData.collegeName : '未指定'}"/></p>
                </div>
                <div class="detail-section">
                    <h3>班级学生列表:</h3>
                    <c:choose>
                        <c:when test="${not empty detailsData.studentsInClass}">
                            <div class="table-responsive">
                                <table class="data-table student-enrollments-table">
                                    <thead>
                                        <tr>
                                            <th>学号</th>
                                            <th>姓名</th>
                                            <th>性别</th>
                                            <th>已获总学分</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="std" items="${detailsData.studentsInClass}">
                                            <tr>
                                                <td><c:out value="${std.studentId}"/></td>
                                                <td><c:out value="${std.name}"/></td>
                                                <td><c:out value="${std.gender}"/></td>
                                                <td><fmt:formatNumber value="${std.totalEarnedCredits ne null ? std.totalEarnedCredits : 0}" pattern="#0.0"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p>该班级暂无学生。</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:when>

            <%-- 学生详情显示 --%>
            <c:when test="${viewMode eq 'studentDetails' and not empty detailsData}">
                <h2>学生详情: <c:out value="${detailsData.name}"/></h2>
                <div class="detail-section">
                    <h3>基本信息</h3>
                    <p><strong>学号:</strong> <c:out value="${detailsData.studentId}"/></p>
                    <p><strong>姓名:</strong> <c:out value="${detailsData.name}"/></p>
                    <p><strong>性别:</strong> <c:out value="${detailsData.gender}"/></p>
                    <p><strong>登录用户名:</strong> <c:out value="${detailsData.userName}"/></p>
                </div>
                <div class="detail-section">
                    <h3>班级与专业信息</h3>
                    <p><strong>班级名称:</strong> <c:out value="${not empty detailsData.className ? detailsData.className : '未分配班级'}"/></p>
                    <p><strong>专业名称:</strong> <c:out value="${not empty detailsData.majorName ? detailsData.majorName : '未指定'}"/></p>
                    <p><strong>所属学院:</strong> <c:out value="${not empty detailsData.collegeName ? detailsData.collegeName : '未指定'}"/></p>
                </div>
                <div class="detail-section">
                    <h3>选课记录与成绩</h3>
                    <c:choose>
                        <c:when test="${not empty detailsData.enrollments}">
                            <div class="table-responsive">
                                <table class="data-table student-enrollments-table">
                                    <thead>
                                        <tr>
                                            <th>课程ID</th>
                                            <th>课程名称</th>
                                            <th>任课教师</th>
                                            <th>课程学分</th>
                                            <th>所得成绩</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="enrollment" items="${detailsData.enrollments}">
                                            <tr>
                                                <td><c:out value="${enrollment.course.courseId}"/></td>
                                                <td><c:out value="${enrollment.course.courseName}"/></td>
                                                <td><c:out value="${enrollment.course.courseTeacher}"/></td>
                                                <td><fmt:formatNumber value="${enrollment.course.credit}" pattern="#0.0"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${enrollment.grade ne null}">
                                                            <fmt:formatNumber value="${enrollment.grade}" pattern="#0.00"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            未录入
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p>该学生暂无选课记录。</p>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="detail-section">
                    <h3>学分统计</h3>
                    <p><strong>已获得总学分:</strong>
                        <c:choose>
                            <c:when test="${detailsData.totalEarnedCredits ne null}">
                                <fmt:formatNumber value="${detailsData.totalEarnedCredits}" pattern="#0.0"/>
                            </c:when>
                            <c:otherwise>
                                0.0
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:when>

            <%-- 学生编辑表单 --%>
            <c:when test="${viewMode eq 'editStudent' and not empty editData}">
                <h2>编辑学生信息: <c:out value="${editData.name}"/></h2>
                <form method="POST" action="${pageContext.request.contextPath}/admin/saveStudent">
                    <input type="hidden" name="studentId" value="${editData.studentId}">
                    <input type="hidden" name="activeModule" value="student"> <%-- 用于保存后正确返回 --%>

                    <div class="form-group">
                        <label for="editStudentName">姓名:</label>
                        <input type="text" id="editStudentName" name="name" value="<c:out value="${editData.name}"/>" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label for="editStudentGender">性别:</label>
                        <select id="editStudentGender" name="gender" required class="form-control">
                            <option value="男" ${editData.gender eq '男' ? 'selected' : ''}>男</option>
                            <option value="女" ${editData.gender eq '女' ? 'selected' : ''}>女</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="editStudentClassId">班级:</label>
                        <select id="editStudentClassId" name="classId" class="form-control">
                            <option value="">-- 不分配/更改班级 --</option>
                            <c:if test="${not empty classList}">
                                <c:forEach var="classInfo" items="${classList}">
                                    <option value="${classInfo.classId}" ${editData.classId eq classInfo.classId ? 'selected' : ''}>
                                        <c:out value="${classInfo.className}"/>
                                    </option>
                                </c:forEach>
                            </c:if>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="editStudentUserName">登录用户名:</label>
                        <input type="text" id="editStudentUserName" name="userName" value="<c:out value="${editData.userName}"/>" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label for="editStudentPassword">新密码 (选填):</label>
                        <input type="password" id="editStudentPassword" name="password" placeholder="留空则不修改密码" class="form-control">
                        <small>如果需要修改密码，请在此输入新密码。</small>
                    </div>

                    <div class="form-actions" style="text-align: left; margin-top: 1.5rem;">
                        <button type="submit" class="btn btn-submit">保存更改</button>
                        <a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=student#student" class="btn btn-cancel">取消</a>
                    </div>
                </form>
            </c:when>

            <%-- 课程详情显示 (后续添加) --%>
            <c:when test="${viewMode eq 'courseDetails' and not empty detailsData}">
                <h2>课程详情: <c:out value="${detailsData.courseName}"/></h2>
                <div class="detail-section">
                    <h3>基本信息</h3>
                    <p><strong>课程ID:</strong> <c:out value="${detailsData.courseId}"/></p>
                    <p><strong>课程名称:</strong> <c:out value="${detailsData.courseName}"/></p>
                    <p><strong>授课教师:</strong> <c:out value="${detailsData.courseTeacher}"/></p>
                    <p><strong>课程学分:</strong> <fmt:formatNumber value="${detailsData.credit}" pattern="#0.0"/></p>
                    <p><strong>所属专业:</strong> <c:out value="${not empty detailsData.majorName ? detailsData.majorName : '全校选修或未指定'}"/></p>
                </div>
                <div class="detail-section">
                    <h3>选修学生列表 (<c:out value="${detailsData.enrollments.size()}"/>人):</h3>
                    <c:choose>
                        <c:when test="${not empty detailsData.enrollments}">
                            <div class="table-responsive">
                                <table class="data-table student-enrollments-table">
                                    <thead>
                                        <tr>
                                            <th>学号</th>
                                            <th>学生姓名</th>
                                            <th>班级</th>
                                            <th>所得成绩</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="enrollment" items="${detailsData.enrollments}">
                                            <tr>
                                                <td><c:out value="${enrollment.student.studentId}"/></td>
                                                <td><c:out value="${enrollment.student.name}"/></td>
                                                <td><c:out value="${not empty enrollment.student.className ? enrollment.student.className : '未分配'}"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${enrollment.grade ne null}">
                                                            <fmt:formatNumber value="${enrollment.grade}" pattern="#0.00"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            未录入
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p>暂无学生选修此课程。</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:when>
<%-- 课程编辑表单 --%>
            <c:when test="${viewMode eq 'editCourse' and not empty editData}">
                <h2>编辑课程信息: <c:out value="${editData.courseName}"/></h2>
                <form method="POST" action="${pageContext.request.contextPath}/admin/saveCourse">
                    <input type="hidden" name="courseId" value="${editData.courseId}">
                    <input type="hidden" name="activeModule" value="course"> <%-- 用于保存后正确返回 --%>

                    <div class="form-group">
                        <label for="editCourseNameActual">课程名称:</label> <%-- Changed id to avoid conflict with other potential elements --%>
                        <input type="text" id="editCourseNameActual" name="courseName" value="<c:out value="${editData.courseName}"/>" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label for="editCourseTeacherActual">授课教师:</label>
                        <input type="text" id="editCourseTeacherActual" name="courseTeacher" value="<c:out value="${editData.courseTeacher}"/>" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label for="editCourseCreditActual">学分:</label>
                        <input type="number" id="editCourseCreditActual" name="credit" value="${editData.credit}" step="0.1" min="0.5" max="10" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label for="editCourseMajorNameActual">所属专业 (可选):</label>
                        <select id="editCourseMajorNameActual" name="majorName" class="form-control">
                            <option value="">-- 全校选修/不更改专业 --</option>
                            <c:if test="${not empty majorList}">
                                <c:forEach var="major" items="${majorList}">
                                    <option value="${major.majorName}" ${editData.majorId eq major.majorId ? 'selected' : ''}>
                                        <c:out value="${major.majorName}"/>
                                    </option>
                                </c:forEach>
                            </c:if>
                        </select>
                        <small>选择一个专业，或留空以设为全校选修或不更改当前专业设置。</small>
                    </div>

                    <div class="form-actions" style="text-align: left; margin-top: 1.5rem;">
                        <button type="submit" class="btn btn-submit">保存更改</button>
                        <a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=course#course" class="btn btn-cancel">取消</a>
                    </div>
                </form>
            </c:when>

            <c:otherwise>
                <h2>信息</h2>
                <p><c:out value="${requestScope.errorMessage ne null ? requestScope.errorMessage : '没有可显示的详情信息或模式不匹配。'}"/></p>
            </c:otherwise>
        </c:choose>

        <div class="detail-actions">
            <a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=${not empty activeModule ? activeModule : 'student'}#${not empty activeModule ? activeModule : 'student'}" class="btn">返回列表</a>
        </div>
    </div>

    <footer class="admin-footer">
        <p>© ${java.time.Year.now()} 学生管理系统 | 当前管理员：<c:out value="${sessionScope.username}"/></p>
    </footer>
</body>
</html>