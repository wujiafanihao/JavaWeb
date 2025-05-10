<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学生界面</title>
    <!-- 链接到CSS文件，假设CSS文件名为 student.css -->
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/student.css">
</head>
<body>
    <header>
        <h1>欢迎, ${sessionScope.username}</h1>
        <!-- 这里可以添加导航栏等 -->
    </header>

    <main>
        <h2>学生个人中心</h2>
        <p>这里是学生界面的主要内容区域。</p>
        <!-- 根据需要，这里会放置学生的个人信息、课程信息等 -->
        <ul>
            <li><a href="#">查看个人信息</a></li>
            <li><a href="#">我的课程</a></li>
            <li><a href="#">成绩查询</a></li>
        </ul>
    </main>

    <footer>
        <p>&copy; 2024 学生管理系统</p>
    </footer>

</body>
</html>