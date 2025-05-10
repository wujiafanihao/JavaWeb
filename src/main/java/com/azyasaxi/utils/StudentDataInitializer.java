package com.azyasaxi.utils;

import com.azyasaxi.dao.AdminLogDao; // 新增：导入 AdminLogDao
import com.azyasaxi.model.AdminLog;  // 新增：导入 AdminLog
import org.springframework.jdbc.core.JdbcTemplate; // 新增：导入 JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource; // 新增：导入 SingleConnectionDataSource

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudentDataInitializer {

    private Connection conn;
    private Random random;
    private AdminLogDao adminLogDao; // 新增：AdminLogDao 实例

    // 中文姓氏
    private static final String[] LAST_NAMES_CH = {"李", "王", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴",
            "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"};
    // 对应的姓氏拼音 (简化)
    private static final String[] LAST_NAMES_PY = {"li", "wang", "zhang", "liu", "chen", "yang", "zhao", "huang", "zhou", "wu",
            "xu", "sun", "hu", "zhu", "gao", "lin", "he", "guo", "ma", "luo"};

    // 中文名字（单字）
    private static final String[] FIRST_NAMES_CH_SINGLE = {"伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋",
            "勇", "艳", "杰", "娟", "涛", "明", "超", "平", "刚", "毅", "浩", "宇"};
    // 对应的名字拼音 (简化)
    private static final String[] FIRST_NAMES_PY_SINGLE = {"wei", "fang", "na", "min", "jing", "li", "qiang", "lei", "jun", "yang",
            "yong", "yan", "jie", "juan", "tao", "ming", "chao", "ping", "gang", "yi", "hao", "yu"};

    // 用于构成双字名时的第二个字 (也可以从 FIRST_NAMES_CH_SINGLE 中取)
    private static final String[] FIRST_NAMES_CH_SECOND = {"英", "华", "兰", "霞", "萍", "国", "龙", "丹", "梅", "雪"};
    private static final String[] FIRST_NAMES_PY_SECOND = {"ying", "hua", "lan", "xia", "ping", "guo", "long", "dan", "mei", "xue"};


    public StudentDataInitializer(Connection conn, Random random) {
        this.conn = conn;
        this.random = random;
        // 使用传入的 Connection 创建 JdbcTemplate 和 AdminLogDao
        // 注意：SingleConnectionDataSource(conn, false) 表示不关闭外部传入的连接
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(conn, false));
        this.adminLogDao = new AdminLogDao(jdbcTemplate);
    }

    /**
     * 初始化所有学生相关数据：基本信息、课程表、请假记录
     * @param studentCount 要创建的学生数量
     */
    public void initializeAllStudentData(int studentCount) throws SQLException {
        System.out.println("开始初始化 " + studentCount + " 个学生数据...");
        List<Integer> classIds = getClassIds();
        List<CourseInfo> allCourses = getAllCourses(); // 获取所有课程信息

        if (classIds.isEmpty()) {
            System.err.println("没有可用的班级信息，无法创建学生。");
            return;
        }
        if (allCourses.isEmpty()) {
            System.err.println("没有可用的课程信息，无法为学生创建课程表。");
            // 仍然可以创建学生，只是课程表会是空的
        }

        String studentSql = "INSERT IGNORE INTO Student (name, gender, class_id, username, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmtStudent = conn.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < studentCount; i++) {
                NameWithPinyin nameInfo = generateRandomNameWithPinyin(); // 调用新方法获取姓名和拼音
                String name = nameInfo.chineseName; // 获取中文名
                String gender = random.nextBoolean() ? "男" : "女";
                int classId = classIds.get(random.nextInt(classIds.size()));
                // 调用新方法，使用拼音名和序号生成用户名
                String username = generateUsernameFromPinyin(nameInfo.pinyinName, i + 1);
                String password = "123456"; // 默认密码，实际应用应加密

                pstmtStudent.setString(1, name);
                pstmtStudent.setString(2, gender);
                pstmtStudent.setInt(3, classId);
                pstmtStudent.setString(4, username);
                pstmtStudent.setString(5, password);

                int affectedRows = pstmtStudent.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmtStudent.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int studentId = generatedKeys.getInt(1);
                            // 为该学生创建一些请假历史
                            addLeaveRequestsForStudent(studentId);
                        }
                    }
                } else {
                    System.out.println("学生 " + username + " 已存在或创建失败，跳过其关联数据生成。");
                }
            }
        }
        System.out.println("已尝试添加 " + studentCount + " 个学生及其关联数据。");
        
        // 添加模拟管理员日志
        addSampleAdminLogs();
    }

    private void addSampleAdminLogs() {
        System.out.println("开始添加模拟管理员操作日志...");
        if (this.adminLogDao == null) {
            System.err.println("AdminLogDao 未初始化，无法添加模拟日志。");
            return;
        }
        try {
            // 假设管理员ID为1，用户名为"admin" (与DataBase.java中addInitialAdmin一致)
            Integer adminId = 1;
            String adminUsername = "admin";

            // 模拟日志1: 新增学生
            adminLogDao.addLog(new AdminLog(adminId, adminUsername, "新增学生", "学生", "zhangsan001", "管理员 admin 添加了新学生: 张三 (用户名: zhangsan001)"));
            
            // 模拟日志2: 修改课程信息
            // 假设课程ID为5存在 (需要根据实际初始化的课程ID调整，或使用课程名)
            List<CourseInfo> courses = getAllCourses(); // 复用现有方法获取课程
            if (!courses.isEmpty()) {
                int sampleCourseId = courses.get(random.nextInt(courses.size())).courseId;
                 adminLogDao.addLog(new AdminLog(adminId, adminUsername, "修改课程信息", "课程", String.valueOf(sampleCourseId), "管理员 admin 修改了课程 (ID: "+sampleCourseId+") 的学分。"));
            } else {
                 adminLogDao.addLog(new AdminLog(adminId, adminUsername, "修改课程信息", "课程", "示例课程名", "管理员 admin 修改了课程 '示例课程名' 的学分。"));
            }


            // 模拟日志3: 批准请假
            // 假设请假申请ID为2存在 (需要根据实际初始化的请假ID调整)
            // 为了简单，这里直接构造描述，实际应基于已存在的请假申请
            adminLogDao.addLog(new AdminLog(adminId, adminUsername, "批准请假申请", "请假申请", "2", "管理员 admin 批准了请假申请 (ID: 2)"));
            
            // 模拟日志4: 修改学生成绩
            List<Integer> studentIdsForLog = getStudentIdsForLog(3); // 获取最多3个学生ID用于日志
            if (!studentIdsForLog.isEmpty() && !courses.isEmpty()) {
                int studentIdForGradeLog = studentIdsForLog.get(random.nextInt(studentIdsForLog.size()));
                int courseIdForGradeLog = courses.get(random.nextInt(courses.size())).courseId;
                 adminLogDao.addLog(new AdminLog(adminId, adminUsername, "修改学生成绩", "学生选课记录", "S:" + studentIdForGradeLog + "_C:" + courseIdForGradeLog, "管理员 admin 修改了学生 (ID: "+studentIdForGradeLog+") 的课程 (ID: "+courseIdForGradeLog+") 成绩。"));
            }


            System.out.println("模拟管理员操作日志添加完毕。");
        } catch (Exception e) {
            System.err.println("添加模拟管理员日志时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<Integer> getStudentIdsForLog(int count) throws SQLException {
        List<Integer> studentIds = new ArrayList<>();
        String sql = "SELECT student_id FROM Student LIMIT ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    studentIds.add(rs.getInt("student_id"));
                }
            }
        }
        return studentIds;
    }


    private List<Integer> getClassIds() throws SQLException {
        List<Integer> classIds = new ArrayList<>();
        String sql = "SELECT class_id FROM ClassInfo";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                classIds.add(rs.getInt("class_id"));
            }
        }
        return classIds;
    }

    private int getMajorIdForClass(int classId) throws SQLException {
        String sql = "SELECT major_id FROM ClassInfo WHERE class_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("major_id");
                }
            }
        }
        throw new SQLException("无法找到班级ID " + classId + " 对应的专业ID。");
    }


    private static class CourseInfo {
        int courseId;
        int majorId; // 课程所属的专业ID

        CourseInfo(int courseId, int majorId) {
            this.courseId = courseId;
            this.majorId = majorId;
        }
    }

    private List<CourseInfo> getAllCourses() throws SQLException {
        List<CourseInfo> courses = new ArrayList<>();
        String sql = "SELECT course_id, major_id FROM Course";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(new CourseInfo(rs.getInt("course_id"), rs.getInt("major_id")));
            }
        }
        return courses;
    }

    /**
     * 为指定学生创建一些模拟的请假历史记录
     */
    private void addLeaveRequestsForStudent(int studentId) throws SQLException {
        String sql = "INSERT IGNORE INTO LeaveRequest (student_id, reason, start_date, end_date, status, request_date, approval_date, approved_by_admin_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String[] reasons = {"参加家庭婚礼", "身体不适就医", "重要个人事务", "参加学术会议预演", "重感冒需休养"};
        String[] statuses = {"待审批", "已批准", "已驳回"};

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int leaveRequestCount = random.nextInt(3); // 为每个学生生成0-2条请假记录
            for (int i = 0; i < leaveRequestCount; i++) {
                String reason = reasons[random.nextInt(reasons.length)];

                LocalDate startDate = LocalDate.now().minusDays(random.nextInt(60) + 1); // 过去60天内的某天开始
                LocalDate endDate = startDate.plusDays(random.nextInt(5) + 1); // 请假1-5天

                String status = statuses[random.nextInt(statuses.length)];
                LocalDateTime requestDate = LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(), 9, 0).minusDays(random.nextInt(5)+1); // 申请日期在开始日期前1-5天
                LocalDateTime approvalDate = null;
                Integer approvedByAdminId = null;

                if (!status.equals("待审批")) {
                    approvalDate = LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(),10,0).minusDays(random.nextInt(2)); // 审批日期在申请日期之后，开始日期之前或当天
                    if (approvalDate.isBefore(requestDate)) approvalDate = requestDate.plusHours(random.nextInt(24)+1);
                    approvedByAdminId = 1; // 假设由ID为1的管理员审批
                }

                pstmt.setInt(1, studentId);
                pstmt.setString(2, reason);
                pstmt.setDate(3, Date.valueOf(startDate));
                pstmt.setDate(4, Date.valueOf(endDate));
                pstmt.setString(5, status);
                pstmt.setTimestamp(6, Timestamp.valueOf(requestDate));
                if (approvalDate != null) {
                    pstmt.setTimestamp(7, Timestamp.valueOf(approvalDate));
                } else {
                    pstmt.setNull(7, Types.TIMESTAMP);
                }
                if (approvedByAdminId != null) {
                    pstmt.setInt(8, approvedByAdminId);
                } else {
                    pstmt.setNull(8, Types.INTEGER);
                }
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * 生成随机的中文姓名，并同时返回其对应的简化拼音形式。
     * @return 包含中文名和对应拼音的对象。
     */
    private NameWithPinyin generateRandomNameWithPinyin() {
        int lastNameIndex = random.nextInt(LAST_NAMES_CH.length);
        String chineseLastName = LAST_NAMES_CH[lastNameIndex];
        String pinyinLastName = LAST_NAMES_PY[lastNameIndex];

        int firstNameIndex1 = random.nextInt(FIRST_NAMES_CH_SINGLE.length);
        String chineseFirstName1 = FIRST_NAMES_CH_SINGLE[firstNameIndex1];
        String pinyinFirstName1 = FIRST_NAMES_PY_SINGLE[firstNameIndex1];

        String chineseFullName = chineseLastName + chineseFirstName1;
        String pinyinFullName = pinyinLastName + pinyinFirstName1;

        if (random.nextDouble() < 0.4) { // 40%的概率是双字名
            int firstNameIndex2 = random.nextInt(FIRST_NAMES_CH_SECOND.length);
            chineseFullName += FIRST_NAMES_CH_SECOND[firstNameIndex2];
            pinyinFullName += FIRST_NAMES_PY_SECOND[firstNameIndex2];
        }
        return new NameWithPinyin(chineseFullName, pinyinFullName);
    }

    // 辅助内部类，用于同时存储中文名和其拼音
    private static class NameWithPinyin {
        String chineseName;
        String pinyinName;
        NameWithPinyin(String chineseName, String pinyinName) {
            this.chineseName = chineseName;
            this.pinyinName = pinyinName;
        }
    }


    /**
     * 根据生成的拼音名和序号生成学生用户名。
     * @param pinyinName 学生的拼音名 (例如 "zhangsan", "liwei")。
     * @param suffixNumber 用于确保唯一性的数字后缀。
     * @return 生成的学生用户名 (例如 "zhangsan001", "liwei002")。
     */
    private String generateUsernameFromPinyin(String pinyinName, int suffixNumber) {
        if (pinyinName == null || pinyinName.isEmpty()) {
            pinyinName = "user"; // 默认基础名
        }
        // 将拼音转换为小写，并附加格式化的数字后缀
        return pinyinName.toLowerCase() + String.format("%03d", suffixNumber);
    }

}