<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>系统操作日志 - 管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <style>
        .log-container {
            max-width: 1200px; /* 可以更宽一些以容纳更多列 */
            margin: 2rem auto;
            padding: 1.5rem;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.08);
        }
        .log-container h2 {
            text-align: center;
            margin-bottom: 1.5rem;
            color: #2c3e50;
        }
        .log-table th, .log-table td {
            font-size: 0.85em; /* 日志内容可能较多，稍微缩小字体 */
            padding: 0.6rem 0.8rem;
        }
        .log-table .description-col {
            min-width: 250px; /* 给描述列一个最小宽度 */
            word-break: break-word; /* 允许长描述换行 */
        }
        .pagination {
            margin-top: 1.5rem;
            text-align: center;
        }
        .pagination a, .pagination span {
            display: inline-block;
            padding: 0.5rem 0.85rem;
            margin: 0 0.2rem;
            border: 1px solid var(--border-color);
            color: var(--primary-color);
            text-decoration: none;
            border-radius: var(--border-radius-small);
        }
        .pagination a:hover {
            background-color: var(--primary-color-darker);
            color: var(--white-color);
            border-color: var(--primary-color-darker);
        }
        .pagination .current {
            background-color: var(--primary-color);
            color: var(--white-color);
            border-color: var(--primary-color);
            font-weight: bold;
        }
        .pagination .disabled {
            color: var(--light-text-color);
            border-color: var(--border-color-light);
            pointer-events: none; /* 不可点击 */
        }
    </style>
</head>
<body>
    <header class="admin-header">
        <div class="logo">学生管理系统 - 系统日志</div>
        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard">返回仪表盘</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="logout">退出登录</a></li>
            </ul>
        </nav>
    </header>

    <div class="log-container">
        <h2>系统操作日志 (共 ${totalLogs} 条)</h2>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <c:if test="${empty logList and empty errorMessage}">
            <div class="alert alert-info">暂无系统操作日志。</div>
        </c:if>

        <c:if test="${not empty logList}">
            <div class="table-responsive">
                <table class="data-table log-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>操作管理员</th>
                            <th>操作类型</th>
                            <th>目标实体</th>
                            <th>实体ID</th>
                            <th class="description-col">详细描述</th>
                            <th>操作时间</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logList}">
                            <tr>
                                <td><c:out value="${log.logId}"/></td>
                                <td><c:out value="${log.adminUsername}"/> (ID: <c:out value="${log.adminId ne null ? log.adminId : 'N/A'}"/>)</td>
                                <td><c:out value="${log.actionType}"/></td>
                                <td><c:out value="${log.targetEntity}"/></td>
                                <td><c:out value="${log.targetEntityId}"/></td>
                                <td class="description-col"><c:out value="${log.actionDescription}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty log.actionTimestampAsDate}">
                                            <fmt:formatDate value="${log.actionTimestampAsDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                        </c:when>
                                        <c:otherwise>
                                            N/A
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <%-- 分页控件 --%>
            <div class="pagination">
                <c:if test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/admin/systemLogs?page=${currentPage - 1}">&laquo; 上一页</a>
                </c:if>
                <c:if test="${currentPage eq 1}">
                     <span class="disabled">&laquo; 上一页</span>
                </c:if>

                <c:set var="startPage" value="${currentPage - 2 < 1 ? 1 : currentPage - 2}"/>
                <c:set var="endPage" value="${currentPage + 2 > totalPages ? totalPages : currentPage + 2}"/>
                <c:if test="${endPage - startPage < 4 && totalPages > 4}">
                    <c:if test="${startPage eq 1}">
                         <c:set var="endPage" value="${startPage + 4 > totalPages ? totalPages : startPage + 4}"/>
                    </c:if>
                    <c:if test="${endPage eq totalPages}">
                         <c:set var="startPage" value="${endPage - 4 < 1 ? 1 : endPage - 4}"/>
                    </c:if>
                </c:if>


                <c:if test="${startPage > 1}">
                    <a href="${pageContext.request.contextPath}/admin/systemLogs?page=1">1</a>
                    <c:if test="${startPage > 2}"><span>...</span></c:if>
                </c:if>

                <c:forEach begin="${startPage}" end="${endPage}" var="i">
                    <c:choose>
                        <c:when test="${i eq currentPage}">
                            <span class="current">${i}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/admin/systemLogs?page=${i}">${i}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:if test="${endPage < totalPages}">
                    <c:if test="${endPage < totalPages - 1}"><span>...</span></c:if>
                    <a href="${pageContext.request.contextPath}/admin/systemLogs?page=${totalPages}">${totalPages}</a>
                </c:if>

                <c:if test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/admin/systemLogs?page=${currentPage + 1}">下一页 &raquo;</a>
                </c:if>
                 <c:if test="${currentPage eq totalPages}">
                     <span class="disabled">下一页 &raquo;</span>
                </c:if>
            </div>
        </c:if>
    </div>

    <footer class="admin-footer">
        <p>© ${java.time.Year.now()} 学生管理系统 | 当前管理员：<c:out value="${sessionScope.username}"/></p>
    </footer>
</body>
</html>