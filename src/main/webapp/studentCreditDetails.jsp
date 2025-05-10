<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学生学分详情 - <c:out value="${studentDetails.name}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <style>
        .detail-container {
            max-width: 900px;
            margin: 2rem auto;
            padding: 2rem;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .detail-container h2, .detail-container h3 {
            color: #2c3e50;
            border-bottom: 1px solid #eee;
            padding-bottom: 0.75rem;
            margin-bottom: 1rem;
        }
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1rem;
            margin-bottom: 1.5rem;
        }
        .info-grid p {
            margin-bottom: 0.5rem;
        }
        .info-grid strong {
            color: #34495e;
            min-width: 80px;
            display: inline-block;
        }
        .grade-input {
            width: 70px;
            padding: 0.3rem 0.5rem;
            border: 1px solid #ccc;
            border-radius: 4px;
            text-align: right;
        }
        .update-grade-btn {
            padding: 0.3rem 0.7rem;
            font-size: 0.85em;
            margin-left: 0.5rem;
        }
        .total-credits {
            font-size: 1.2em;
            font-weight: bold;
            color: var(--primary-color);
            margin-top: 1rem;
        }
    </style>
</head>
<body>
    <header class="admin-header">
        <div class="logo">学生管理系统 - 学分详情</div>
        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=credit#credit">返回仪表盘</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="logout">退出登录</a></li>
            </ul>
        </nav>
    </header>

    <div class="detail-container">
        <c:if test="${not empty studentDetails}">
            <h2>学生学分详情: <c:out value="${studentDetails.name}"/></h2>

            <div class="info-grid">
                <p><strong>学号:</strong> <c:out value="${studentDetails.studentId}"/></p>
                <p><strong>姓名:</strong> <c:out value="${studentDetails.name}"/></p>
                <p><strong>班级:</strong> <c:out value="${not empty studentDetails.className ? studentDetails.className : '未分配班级'}"/></p>
            </div>

            <h3>选课及成绩列表:</h3>
            <c:choose>
                <c:when test="${not empty studentDetails.enrollments}">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>课程ID</th>
                                    <th>课程名称</th>
                                    <th>任课教师</th>
                                    <th>课程学分</th>
                                    <th>所得成绩</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="enrollment" items="${studentDetails.enrollments}">
                                    <tr>
                                        <td><c:out value="${enrollment.course.courseId}"/></td>
                                        <td><c:out value="${enrollment.course.courseName}"/></td>
                                        <td><c:out value="${enrollment.course.courseTeacher}"/></td>
                                        <td><fmt:formatNumber value="${enrollment.course.credit}" pattern="#0.0"/></td>
                                        <form method="POST" action="${pageContext.request.contextPath}/admin/updateStudentGrade" style="display:contents;">
                                            <input type="hidden" name="studentId" value="${studentDetails.studentId}">
                                            <input type="hidden" name="courseId" value="${enrollment.course.courseId}">
                                            <td>
                                                <input type="number" name="newGrade" class="grade-input" step="0.01" min="0" max="100" 
                                                       value="<c:if test='${enrollment.grade ne null}'><fmt:formatNumber value='${enrollment.grade}' pattern='#0.00' groupingUsed='false'/></c:if>">
                                            </td>
                                            <td>
                                                <button type="submit" class="btn btn-search update-grade-btn">更新成绩</button>
                                            </td>
                                        </form>
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

            <div class="total-credits">
                <strong>已获得总学分:</strong> 
                <fmt:formatNumber value="${studentDetails.totalEarnedCredits ne null ? studentDetails.totalEarnedCredits : 0}" pattern="#0.0"/>
            </div>

        </c:if>
        <c:if test="${empty studentDetails}">
            <p><c:out value="${requestScope.errorMessage ne null ? requestScope.errorMessage : '无法加载学生学分详情。'}"/></p>
        </c:if>
        
        <div style="text-align: center; margin-top: 2rem;">
             <a href="${pageContext.request.contextPath}/admin/dashboard?activeModule=credit#credit" class="btn">返回学分列表</a>
        </div>

    </div>

    <footer class="admin-footer">
        <p>© ${java.time.Year.now()} 学生管理系统 | 当前管理员：<c:out value="${sessionScope.username}"/></p>
    </footer>
</body>
</html>