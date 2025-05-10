<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学分数据统计 - 管理员控制台</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <%-- 引入 Chart.js --%>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js" integrity="sha512-CQBWl4fJHWbryGE+Pc7UAxWMUMNMWzWxF4SQo9CgkJIN1kx6djDQZjh3Y8SZ1d+6I+1zze6Z7kHXO7q3UyZAWw==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dataSats.css">
</head>
<body>
    <header class="admin-header">
        <div class="logo">学生管理系统 - 数据统计</div>
        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/admin/dashboard">返回仪表盘</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="logout">退出登录</a></li>
            </ul>
        </nav>
    </header>

    <div class="stats-container">
        <h2>学分数据统计分析</h2>

        <c:if test="${statsDataLoaded eq false}">
            <div class="alert alert-danger">
                <p>错误：<c:out value="${statsErrorMessage ne null ? statsErrorMessage : '无法加载统计数据。'}"/></p>
            </div>
        </c:if>

        <c:if test="${statsDataLoaded eq true}">
            <div class="charts-grid">
                <div class="chart-card">
                    <h3>各学院学生人数</h3>
                    <canvas id="collegeStudentCountChart"></canvas>
                </div>
                <div class="chart-card">
                    <h3>各学院平均获得学分</h3>
                    <canvas id="collegeAvgCreditsChart"></canvas>
                </div>
            </div>
            <div class="charts-grid" style="margin-top: 2rem;">
                 <div class="chart-card">
                    <h3>学分区间学生人数分布</h3>
                    <canvas id="creditRangeStudentCountChart"></canvas>
                </div>
                 <div class="chart-card">
                    <h3>学分区间学生人数分布 (饼图)</h3>
                    <canvas id="creditRangeStudentCountPieChart"></canvas>
                </div>
            </div>
        </c:if>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // 将 EL 表达式的输出显式作为字符串，然后在 JS 中转换为布尔值
            const statsDataLoadedStr = '${statsDataLoaded}';
            const isStatsDataLoaded = (statsDataLoadedStr === 'true');

            if (!isStatsDataLoaded) {
                console.warn('统计数据未加载，图表无法渲染。');
                return;
            }

            // Chart.js 辅助函数：生成随机颜色
            function getRandomColor() {
                const r = Math.floor(Math.random() * 200);
                const g = Math.floor(Math.random() * 200);
                const b = Math.floor(Math.random() * 200);
                return `rgba(${r}, ${g}, ${b}, 0.7)`;
            }
            function getRandomBorderColor() {
                const r = Math.floor(Math.random() * 200);
                const g = Math.floor(Math.random() * 200);
                const b = Math.floor(Math.random() * 200);
                return `rgba(${r}, ${g}, ${b}, 1)`;
            }

            try {
                // 1. 各学院学生人数图表 (条形图)
                const collegeNames = JSON.parse('${collegeNamesJson}');
                const studentCountsPerCollege = JSON.parse('${studentCountsPerCollegeJson}');
                const collegeStudentCountCtx = document.getElementById('collegeStudentCountChart')?.getContext('2d');
                if (collegeStudentCountCtx && collegeNames.length > 0) {
                    new Chart(collegeStudentCountCtx, {
                        type: 'bar',
                        data: {
                            labels: collegeNames,
                            datasets: [{
                                label: '学生人数',
                                data: studentCountsPerCollege,
                                backgroundColor: collegeNames.map(() => getRandomColor()),
                                borderColor: collegeNames.map(() => getRandomBorderColor()),
                                borderWidth: 1
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: true,
                            scales: {
                                y: { beginAtZero: true, ticks: { precision: 0 } }
                            }
                        }
                    });
                }

                // 2. 各学院平均获得学分图表 (条形图)
                const avgCreditsPerCollege = JSON.parse('${avgCreditsPerCollegeJson}');
                const collegeAvgCreditsCtx = document.getElementById('collegeAvgCreditsChart')?.getContext('2d');
                if (collegeAvgCreditsCtx && collegeNames.length > 0) {
                     new Chart(collegeAvgCreditsCtx, {
                        type: 'bar',
                        data: {
                            labels: collegeNames,
                            datasets: [{
                                label: '平均获得学分',
                                data: avgCreditsPerCollege,
                                backgroundColor: collegeNames.map(() => getRandomColor()),
                                borderColor: collegeNames.map(() => getRandomBorderColor()),
                                borderWidth: 1
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: true,
                            scales: {
                                y: { beginAtZero: true, ticks: { precision: 1 } }
                            }
                        }
                    });
                }

                // 3. 学分区间学生人数分布 (条形图)
                const creditRangeLabels = JSON.parse('${creditRangeLabelsJson}');
                const studentCountsPerRange = JSON.parse('${studentCountsPerRangeJson}');
                const creditRangeStudentCountCtx = document.getElementById('creditRangeStudentCountChart')?.getContext('2d');
                if (creditRangeStudentCountCtx && creditRangeLabels.length > 0) {
                    new Chart(creditRangeStudentCountCtx, {
                        type: 'bar',
                        data: {
                            labels: creditRangeLabels,
                            datasets: [{
                                label: '学生人数',
                                data: studentCountsPerRange,
                                backgroundColor: creditRangeLabels.map(() => getRandomColor()),
                                borderColor: creditRangeLabels.map(() => getRandomBorderColor()),
                                borderWidth: 1
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: true,
                            scales: {
                                y: { beginAtZero: true, ticks: { precision: 0 } }
                            }
                        }
                    });
                }
                
                // 4. 学分区间学生人数分布 (饼图)
                const creditRangeStudentCountPieCtx = document.getElementById('creditRangeStudentCountPieChart')?.getContext('2d');
                if (creditRangeStudentCountPieCtx && creditRangeLabels.length > 0) {
                    new Chart(creditRangeStudentCountPieCtx, {
                        type: 'pie',
                        data: {
                            labels: creditRangeLabels,
                            datasets: [{
                                label: '学生人数',
                                data: studentCountsPerRange,
                                backgroundColor: creditRangeLabels.map(() => getRandomColor()),
                                hoverOffset: 4
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: true
                        }
                    });
                }

            } catch (e) {
                console.error("渲染图表时出错:", e);
                const errorDiv = document.createElement('div');
                errorDiv.className = 'alert alert-danger';
                errorDiv.textContent = '渲染图表时发生客户端错误，请检查控制台。';
                document.querySelector('.stats-container').insertBefore(errorDiv, document.querySelector('.charts-grid'));
            }
        });
    </script>

    <footer class="admin-footer">
        <p>© ${java.time.Year.now()} 学生管理系统 | 当前管理员：<c:out value="${sessionScope.username}"/></p>
    </footer>
</body>
</html>