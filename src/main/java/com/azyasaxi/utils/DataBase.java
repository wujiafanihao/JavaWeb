package com.azyasaxi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

public class DataBase {

    // 随机生成器
    private static final Random random = new Random();

    // 学院和专业数据 (这些可以保留在DataBase类中，因为它们是更通用的基础数据)
    protected static final String[] COLLEGES = {
            "计算机学院", "艺术学院", "工程学院", "商学院", "医学院"
    };

    protected static final String[][] MAJORS_BY_COLLEGE = {
            // 计算机学院
            {"软件工程", "计算机科学与技术", "人工智能", "数据科学", "网络安全"},
            // 艺术学院
            {"音乐表演", "舞蹈", "美术", "戏剧影视", "设计"},
            // 工程学院
            {"机械工程", "电子工程", "土木工程", "材料科学", "自动化"},
            // 商学院
            {"金融", "会计", "市场营销", "工商管理", "国际贸易"},
            // 医学院
            {"临床医学", "护理", "药学", "公共卫生", "生物医学"}
    };

    // 课程数据: 每个专业3-5门课程
    protected static final String[][] COURSES_BY_MAJOR_TYPE = {
            // 计算机类课程
            {"Java程序设计", "Python编程", "数据结构与算法", "数据库系统", "计算机网络", "操作系统", "软件工程", "人工智能基础"},
            // 艺术类课程
            {"音乐理论", "声乐训练", "舞蹈基础", "现代舞", "素描", "油画", "表演艺术", "导演基础"},
            // 工程类课程
            {"机械制图", "电子电路", "材料力学", "自动控制原理", "信号与系统", "土木工程概论", "热力学"},
            // 商科类课程
            {"微观经济学", "宏观经济学", "会计学原理", "市场营销", "金融学", "管理学", "国际贸易实务"},
            // 医学类课程
            {"解剖学", "生理学", "病理学", "药理学", "内科学", "外科学", "护理学基础", "公共卫生学"}
    };

    // 预定义的教师姓氏和名字，用于随机生成教师姓名
    private static final String[] TEACHER_SURNAMES = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "吴", "周"};
    private static final String[] TEACHER_GIVEN_NAMES = {"伟", "芳", "娜", "敏", "静", "强", "磊", "洋", "勇", "艳", "涛", "明", "秀英", "文华"};


    public static void main(String[] args) {
        // 加载配置文件
        Properties props = loadProperties();

        if (props == null) {
            System.err.println("无法加载application.properties文件");
            return;
        }

        // 注意：这里的url通常不包含数据库名，因为第一步是创建数据库
        String baseUrl = props.getProperty("db.url"); // e.g., jdbc:mysql://localhost:3306/
        String dbName = "StudentManagement";
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        // SQL脚本
        String[] sqlScripts = {
                // 1. 创建数据库
                "CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
                // 2. 使用数据库
                "USE " + dbName + ";",
                // 3. 创建Admin表 (管理员信息)
                "CREATE TABLE IF NOT EXISTS Admin (" +
                        "admin_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "username VARCHAR(50) UNIQUE NOT NULL," +
                        "password VARCHAR(255) NOT NULL" +
                        ");",
                // 4. 创建College表 (学院信息)
                "CREATE TABLE IF NOT EXISTS College (" +
                        "college_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "college_name VARCHAR(100) NOT NULL UNIQUE" + // 学院名唯一
                        ");",
                // 5. 创建Major表 (专业信息)
                "CREATE TABLE IF NOT EXISTS Major (" +
                        "major_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "major_name VARCHAR(100) NOT NULL," +
                        "college_id INT," +
                        "FOREIGN KEY (college_id) REFERENCES College(college_id) ON DELETE CASCADE," + // 级联删除
                        "UNIQUE (major_name, college_id)" + // 同一学院下专业名唯一
                        ");",
                // 6. 创建ClassInfo表 (班级信息)
                "CREATE TABLE IF NOT EXISTS ClassInfo (" +
                        "class_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "class_name VARCHAR(100) NOT NULL," +
                        "major_id INT," +
                        "FOREIGN KEY (major_id) REFERENCES Major(major_id) ON DELETE CASCADE," +
                        "UNIQUE (class_name, major_id)" + // 同一专业下班级名唯一
                        ");",
                // 7. 创建Student表 (学生基本信息)
                "CREATE TABLE IF NOT EXISTS Student (" +
                        "student_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(100) NOT NULL," +
                        "gender ENUM('男', '女') NOT NULL," +
                        "class_id INT," +
                        "username VARCHAR(50) UNIQUE NOT NULL," + // 学生登录用户名
                        "password VARCHAR(255) NOT NULL," +      // 学生登录密码
                        "FOREIGN KEY (class_id) REFERENCES ClassInfo(class_id) ON DELETE SET NULL" + // 班级删除时学生仍在，但class_id为NULL
                        ");",
                // 8. 创建Course表 (课程信息)
                "CREATE TABLE IF NOT EXISTS Course (" +
                        "course_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "course_name VARCHAR(100) NOT NULL," +
                        "course_teacher VARCHAR(100) NOT NULL," + // 课程教师
                        "credit DECIMAL(3,1) NOT NULL," +
                        "major_id INT," + // 课程所属专业，用于区分专业课和选修课的判断
                        "FOREIGN KEY (major_id) REFERENCES Major(major_id) ON DELETE CASCADE," +
                        "UNIQUE (course_name, major_id)" + // 同一专业下课程名唯一
                        ");",
                // 9. 创建Enrollment表 (修读学分信息，即选课记录和成绩)
                "CREATE TABLE IF NOT EXISTS Enrollment (" +
                        "enrollment_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "student_id INT," +
                        "course_id INT," +
                        "grade DECIMAL(5,2)," + // 成绩，允许为空表示未出分
                        "FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE," +
                        "FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE," +
                        "UNIQUE (student_id, course_id)" + // 一个学生同一门课只能有一条选课记录
                        ");",
                // 10. 创建LeaveRequest表 (请假信息)
                "CREATE TABLE IF NOT EXISTS LeaveRequest (" +
                        "leave_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "student_id INT," +
                        "reason TEXT NOT NULL," +
                        "start_date DATE NOT NULL," +
                        "end_date DATE NOT NULL," +
                        "status ENUM('待审批', '已批准', '已驳回') NOT NULL DEFAULT '待审批'," +
                        "request_date DATETIME DEFAULT CURRENT_TIMESTAMP," + // 申请提交时间
                        "approval_date DATETIME NULL," + // 审批时间
                        "approved_by_admin_id INT NULL," + // 审批管理员ID
                        "FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE," +
                        "FOREIGN KEY (approved_by_admin_id) REFERENCES Admin(admin_id) ON DELETE SET NULL" +
                        ");",
                // 11. 创建CreditSummary视图 (修读学分统计视图)
                "CREATE OR REPLACE VIEW CreditSummary AS " +
                        "SELECT e.student_id, s.name AS student_name, SUM(c.credit) AS total_credits " +
                        "FROM Enrollment e " +
                        "JOIN Course c ON e.course_id = c.course_id " +
                        "JOIN Student s ON e.student_id = s.student_id " +
                        "WHERE e.grade >= 60.0 " + // 假设60分及以上获得学分
                        "GROUP BY e.student_id, s.name;",
                // 12. 创建AdminLog表 (管理员操作日志)
                "CREATE TABLE IF NOT EXISTS AdminLog (" +
                        "log_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "admin_id INT," +
                        "admin_username VARCHAR(50)," +
                        "action_type VARCHAR(50) NOT NULL," +
                        "target_entity VARCHAR(100)," +
                        "target_entity_id VARCHAR(50)," +
                        "action_description TEXT," +
                        "action_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (admin_id) REFERENCES Admin(admin_id) ON DELETE SET NULL" +
                        ");"
        };

        // 先连接到MySQL服务器，不指定数据库名，用于创建数据库
        try (Connection connInit = DriverManager.getConnection(baseUrl, username, password);
             Statement stmtInit = connInit.createStatement()) {
            System.out.println("成功连接到MySQL服务器，开始初始化数据库...");
            stmtInit.execute(sqlScripts[0]); // CREATE DATABASE
            System.out.println("执行成功: " + sqlScripts[0].split("\n")[0] + "...");
        } catch (SQLException e) {
            // 如果数据库已存在，可能会报SQLException，但可以继续尝试连接并执行后续脚本
            if (!e.getMessage().contains("database exists")) {
                System.err.println("创建数据库失败: " + e.getMessage());
                return;
            }
            System.out.println("数据库 " + dbName + " 已存在，继续操作...");
        }


        // 使用包含数据库名的URL连接
        String fullUrl = baseUrl + dbName + "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(fullUrl, username, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("成功连接到数据库 " + dbName + "，开始初始化表结构和数据...");

            // 执行剩余的SQL脚本 (从USE DATABASE开始)
            for (int i = 1; i < sqlScripts.length; i++) {
                try {
                    // 对于CREATE TABLE语句，可以分多行，这里简单取第一行作为日志输出
                    String logSql = sqlScripts[i].split("\n")[0];
                    if (logSql.length() > 70) logSql = logSql.substring(0, 70) + "...";
                    stmt.execute(sqlScripts[i]);
                    System.out.println("执行成功: " + logSql);
                } catch (SQLException e) {
                    String logSql = sqlScripts[i].split("\n")[0];
                    if (logSql.length() > 70) logSql = logSql.substring(0, 70) + "...";
                    System.err.println("执行失败: " + e.getMessage() + " (SQL: " + logSql + ")");
                }
            }

            System.out.println("数据库表结构初始化完成！");

            // 添加初始管理员账号 (确保在Admin表创建后)
            addInitialAdmin(conn);

            // 添加学院和专业数据
            addCollegesAndMajors(conn);

            // 添加班级数据
            addInitialClassData(conn);

            // 添加课程数据 (按专业分类)
            addInitialCourseData(conn); // 确保课程数据先于学生课程表和选课记录生成

            // 初始化学生数据（包括学生基本信息、课程表、请假历史）
            StudentDataInitializer studentDataInitializer = new StudentDataInitializer(conn, random);
            studentDataInitializer.initializeAllStudentData(50); // 创建50个学生及其关联数据

            // 添加选课数据 (Enrollment表，带有成绩)
            addInitialEnrollmentData(conn);

            System.out.println("数据库所有数据初始化完成！");

        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Properties loadProperties() {
        try (InputStream input = DataBase.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                System.err.println("找不到application.properties文件");
                return null;
            }

            Properties props = new Properties();
            props.load(input);
            return props;

        } catch (IOException e) {
            System.err.println("读取配置文件出错: " + e.getMessage());
            return null;
        }
    }

    private static void addInitialAdmin(Connection conn) {
        // SQL语句使用INSERT IGNORE避免主键或唯一键冲突时报错，而是静默忽略
        String sql = "INSERT IGNORE INTO Admin (admin_id, username, password) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 1); // 指定管理员ID为1，方便后续引用
            pstmt.setString(2, "admin");
            pstmt.setString(3, "admin123"); // 注意：实际应用中密码应该加密存储
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("已创建默认管理员账号: admin/admin123 (ID: 1)");
            } else {
                System.out.println("管理员账号(ID:1, Username:admin)已存在或创建失败，未重复创建");
            }
        } catch (SQLException e) {
            System.err.println("创建管理员账号失败: " + e.getMessage());
        }
    }

    private static void addCollegesAndMajors(Connection conn) {
        String collegeSql = "INSERT IGNORE INTO College (college_name) VALUES (?)";
        String majorSql = "INSERT IGNORE INTO Major (major_name, college_id) VALUES (?, ?)";

        try (PreparedStatement collegeStmt = conn.prepareStatement(collegeSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement majorStmt = conn.prepareStatement(majorSql)) {

            for (int i = 0; i < COLLEGES.length; i++) {
                collegeStmt.setString(1, COLLEGES[i]);
                collegeStmt.executeUpdate();

                try (var rs = collegeStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int collegeId = rs.getInt(1);
                        String[] majors = MAJORS_BY_COLLEGE[i];
                        for (String major : majors) {
                            majorStmt.setString(1, major);
                            majorStmt.setInt(2, collegeId);
                            majorStmt.executeUpdate();
                        }
                    } else {
                        // 如果学院已存在，通过学院名查询ID
                        try (PreparedStatement queryCollegeId = conn.prepareStatement("SELECT college_id FROM College WHERE college_name = ?")) {
                            queryCollegeId.setString(1, COLLEGES[i]);
                            var rsExisting = queryCollegeId.executeQuery();
                            if (rsExisting.next()) {
                                int collegeId = rsExisting.getInt(1);
                                String[] majors = MAJORS_BY_COLLEGE[i];
                                for (String major : majors) {
                                    majorStmt.setString(1, major);
                                    majorStmt.setInt(2, collegeId);
                                    majorStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("已添加学院和专业数据（或已存在）");
        } catch (SQLException e) {
            System.err.println("添加学院和专业数据失败: " + e.getMessage());
        }
    }

    private static void addInitialClassData(Connection conn) {
        String sql = "INSERT IGNORE INTO ClassInfo (class_name, major_id) VALUES (?, ?)";
        String queryMajorSql = "SELECT major_id, major_name FROM Major"; // 同时获取专业名称用于班级命名

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(queryMajorSql)) {

            while (rs.next()) {
                int majorId = rs.getInt("major_id");
                String majorName = rs.getString("major_name");
                int classCount = 1 + random.nextInt(2); // 每个专业1-2个班级

                for (int i = 1; i <= classCount; i++) {
                    // 使用专业名和序号组合班级名，更具辨识度
                    String className = majorName + String.format("%02d", i) + "班"; // 例如：软件工程01班
                    pstmt.setString(1, className);
                    pstmt.setInt(2, majorId);
                    pstmt.executeUpdate();
                }
            }
            System.out.println("已添加班级数据（或已存在）");
        } catch (SQLException e) {
            System.err.println("添加班级数据失败: " + e.getMessage());
        }
    }

    private static void addInitialCourseData(Connection conn) {
        // SQL 语句现在包含 course_teacher 列
        String sql = "INSERT IGNORE INTO Course (course_name, credit, major_id, course_teacher) VALUES (?, ?, ?, ?)";
        String queryMajorSql = "SELECT major_id FROM Major ORDER BY major_id"; // 专业ID用于关联课程

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(queryMajorSql)) {

            int majorTypeIndex = 0; // 用于循环 COURSES_BY_MAJOR_TYPE
            while (rs.next()) {
                int majorId = rs.getInt("major_id");
                String[] coursesForThisMajorType = COURSES_BY_MAJOR_TYPE[majorTypeIndex % COURSES_BY_MAJOR_TYPE.length];

                int coursesToCreateCount = 3 + random.nextInt(3); // 每个专业创建3-5门课程
                for (int i = 0; i < coursesToCreateCount && i < coursesForThisMajorType.length; i++) {
                    String courseName = coursesForThisMajorType[i];
                    // 随机添加 "基础" 或 "进阶" 后缀，或无后缀
                    int suffixType = random.nextInt(3);
                    if (suffixType == 1) courseName += "(基础)";
                    else if (suffixType == 2) courseName += "(进阶)";

                    double credit = 1.0 + random.nextInt(4) + (random.nextInt(2) * 0.5); // 1.0 到 4.5 学分, 间隔0.5

                    // 生成随机教师姓名
                    String teacherSurname = TEACHER_SURNAMES[random.nextInt(TEACHER_SURNAMES.length)];
                    String teacherGivenName = TEACHER_GIVEN_NAMES[random.nextInt(TEACHER_GIVEN_NAMES.length)];
                    String courseTeacher = teacherSurname + teacherGivenName;

                    pstmt.setString(1, courseName);
                    pstmt.setDouble(2, credit);
                    pstmt.setInt(3, majorId);
                    pstmt.setString(4, courseTeacher); // 设置 course_teacher 参数
                    pstmt.executeUpdate();
                }
                majorTypeIndex++;
            }
            System.out.println("已添加课程数据（包含教师，或已存在）");
        } catch (SQLException e) {
            System.err.println("添加课程数据失败: " + e.getMessage());
        }
    }


    private static void addInitialEnrollmentData(Connection conn) {
        String sql = "INSERT IGNORE INTO Enrollment (student_id, course_id, grade) VALUES (?, ?, ?)";
        String studentSql = "SELECT student_id FROM Student";
        String courseSql = "SELECT course_id FROM Course";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement()) {

            // 获取所有学生ID
            var studentRs = stmt.executeQuery(studentSql);
            java.util.List<Integer> studentIds = new java.util.ArrayList<>();
            while (studentRs.next()) {
                studentIds.add(studentRs.getInt("student_id"));
            }
            studentRs.close();

            // 获取所有课程ID
            var courseRs = stmt.executeQuery(courseSql);
            java.util.List<Integer> courseIds = new java.util.ArrayList<>();
            while (courseRs.next()) {
                courseIds.add(courseRs.getInt("course_id"));
            }
            courseRs.close();

            if (studentIds.isEmpty() || courseIds.isEmpty()) {
                System.out.println("没有学生或课程数据，无法添加选课记录 (Enrollment)。");
                return;
            }

            for (int studentId : studentIds) {
                int enrollmentCount = 3 + random.nextInt(4); // 每个学生选3-6门课程
                java.util.Set<Integer> selectedCoursesForStudent = new java.util.HashSet<>(); //确保一个学生不重复选同一门课（在本次生成中）

                for (int i = 0; i < enrollmentCount && selectedCoursesForStudent.size() < courseIds.size(); i++) {
                    int courseId = courseIds.get(random.nextInt(courseIds.size()));
                    if (selectedCoursesForStudent.contains(courseId)) { //如果随机选到了已选的，则跳过，尝试下一次
                        i--; //重新尝试本次选课
                        continue;
                    }
                    selectedCoursesForStudent.add(courseId);

                    double grade = 40 + random.nextInt(61); // 40-100之间的随机成绩
                    if(random.nextDouble() < 0.1) { // 10%的几率不及格
                        grade = 30 + random.nextInt(30);
                    }
                    grade = Math.round(grade * 10.0) / 10.0; // 保留一位小数

                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, courseId);
                    pstmt.setDouble(3, grade);
                    try {
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        // INSERT IGNORE 会处理唯一约束冲突，这里捕获其他可能的错误
                        if (!e.getMessage().toLowerCase().contains("duplicate entry")) {
                            System.err.println("添加选课记录 (Enrollment) 失败 for student " + studentId + ", course " + courseId + ": " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("已添加选课数据 (Enrollment)（或已存在）");
        } catch (SQLException e) {
            System.err.println("准备添加选课数据 (Enrollment) 失败: " + e.getMessage());
        }
    }
}