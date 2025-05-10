package com.azyasaxi.dao;

import com.azyasaxi.model.Student;    // 导入 Student 模型类
import com.azyasaxi.model.Course;    // 导入 Course 模型
import com.azyasaxi.model.Enrollment; // 导入 Enrollment 模型
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StudentDao 类 (Data Access Object)
 * 负责与数据库中的 Student 表进行交互，以及获取学生相关的详细聚合信息。
 */
@Repository
public class StudentDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StudentDao(JdbcTemplate jdbcTemplateArg) {
        this.jdbcTemplate = jdbcTemplateArg;
    }

    // --- StudentRowMapper (用于学生列表，包含班级名) ---
    private static class BasicStudentRowMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setStudentId(rs.getInt("s_student_id"));
            student.setName(rs.getString("s_name"));
            student.setGender(rs.getString("s_gender"));
            int classId = rs.getInt("s_class_id");
            if (rs.wasNull()) {
                student.setClassId(null);
            } else {
                student.setClassId(classId);
            }
            student.setClassName(rs.getString("ci_class_name"));
            student.setUserName(rs.getString("s_username"));
            return student;
        }
    }

    // --- StudentWithDetailsRowMapper (用于获取学生详细信息，包含学院专业等) ---
    private static class StudentWithDetailsRowMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student(); // 创建Student对象以填充详细信息
            student.setStudentId(rs.getInt("s_student_id"));
            student.setName(rs.getString("s_name"));
            student.setGender(rs.getString("s_gender"));
            student.setUserName(rs.getString("s_username")); // 用户名也加载

            // 填充班级信息
            int classId = rs.getInt("s_class_id");
            if (!rs.wasNull()) {
                student.setClassId(classId);
                student.setClassName(rs.getString("ci_class_name"));
            }

            // 填充专业信息
            // int majorId = rs.getInt("m_major_id"); // 如果需要专业ID本身
            // if (!rs.wasNull()) { student.setMajorId(majorId); } // 假设Student模型有majorId字段
            student.setMajorName(rs.getString("m_major_name"));

            // 填充学院信息
            // int collegeId = rs.getInt("co_college_id"); // 如果需要学院ID本身
            // if (!rs.wasNull()) { student.setCollegeId(collegeId); } // 假设Student模型有collegeId字段
            student.setCollegeName(rs.getString("co_college_name"));

            // enrollments 和 totalEarnedCredits 将在Service层单独获取并设置
            return student;
        }
    }

    // --- EnrollmentWithCourseDetailsRowMapper (用于获取带课程详情的选课记录) ---
    // (这个 RowMapper 与之前的版本相同，确保 Enrollment 模型中有 Course course 字段)
    private static class EnrollmentWithCourseDetailsRowMapper implements RowMapper<Enrollment> {
        @Override
        public Enrollment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrollmentId(rs.getInt("e_enrollment_id"));
            enrollment.setStudentId(rs.getInt("e_student_id"));
            enrollment.setCourseId(rs.getInt("e_course_id"));
            java.math.BigDecimal grade = rs.getBigDecimal("e_grade");
            if (rs.wasNull()) {
                enrollment.setGrade(null);
            } else {
                enrollment.setGrade(grade);
            }

            Course course = new Course();
            course.setCourseId(rs.getInt("c_course_id"));
            course.setCourseName(rs.getString("c_course_name"));
            course.setCourseTeacher(rs.getString("c_course_teacher")); // 获取任课老师
            course.setCredit(rs.getBigDecimal("c_credit"));
            enrollment.setCourse(course);

            return enrollment;
        }
    }


    // --- 查询方法 ---
    public List<Student> searchStudents(String searchTerm) {
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT s.student_id AS s_student_id, s.name AS s_name, s.gender AS s_gender, " +
                        "s.class_id AS s_class_id, ci.class_name AS ci_class_name, s.username AS s_username " +
                        "FROM Student s " +
                        "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id"
        );
        List<Object> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likeTerm = "%" + searchTerm.trim() + "%";
            sqlBuilder.append(" WHERE (s.name LIKE ? OR CAST(s.student_id AS CHAR) LIKE ? OR ci.class_name LIKE ? OR s.username LIKE ?)");
            params.add(likeTerm);
            params.add(likeTerm);
            params.add(likeTerm);
            params.add(likeTerm);
        }
        sqlBuilder.append(" ORDER BY s.student_id ASC");
        try {
            return jdbcTemplate.query(sqlBuilder.toString(), params.toArray(), new BasicStudentRowMapper()); // 使用 BasicStudentRowMapper
        } catch (Exception e) {
            System.err.println("搜索学生信息失败 (searchTerm: " + searchTerm + "): " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Student> getAllStudents() {
        return searchStudents(null);
    }

    /**
     * 根据学生ID获取学生的基本信息 (用于编辑等场景)。
     *
     * @param studentId 学生的ID。
     * @return Student 对象 (只包含基本信息和班级名)，如果未找到则返回 null。
     */
    public Student getStudentByIdForEdit(int studentId) {
        String sql = "SELECT s.student_id AS s_student_id, s.name AS s_name, s.gender AS s_gender, " +
                "s.class_id AS s_class_id, ci.class_name AS ci_class_name, s.username AS s_username " +
                "FROM Student s " +
                "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id " +
                "WHERE s.student_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BasicStudentRowMapper(), studentId);
        } catch (EmptyResultDataAccessException e) {
            return null; // 未找到学生
        } catch (Exception e) {
            System.err.println("根据ID获取学生基本信息失败 (studentId: " + studentId + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 在 StudentDao.java 中
    /**
     * 根据班级ID获取该班级下的所有学生基本信息。
     *
     * @param classId 班级的ID。
     * @return 包含指定班级所有 Student 对象的列表；如果出错或无学生则返回空列表。
     */
    public List<Student> getStudentsByClassId(int classId) {
        // SQL 查询语句，选择学生基本信息，并按学号排序
        String sql = "SELECT s.student_id AS s_student_id, s.name AS s_name, s.gender AS s_gender, " +
                "s.class_id AS s_class_id, ci.class_name AS ci_class_name, s.username AS s_username " +
                "FROM Student s " +
                "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id " + // 虽然已有classId，但JOIN可以获取className
                "WHERE s.class_id = ? " +
                "ORDER BY s.student_id ASC";
        try {
            return jdbcTemplate.query(sql, new BasicStudentRowMapper(), classId); // 使用 BasicStudentRowMapper
        } catch (Exception e) {
            System.err.println("根据班级ID获取学生列表失败 (classId: " + classId + "): " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // 出错时返回空列表
        }
    }

    /**
     * 根据学生用户名获取学生信息。
     *
     * @param username 学生的用户名。
     * @return 包含学生信息的 Student 对象，如果未找到则返回 null。
     */

    public Student getStudentByUsername(String username) { // 用于认证，需要密码
        String sql = "SELECT s.student_id, s.name, s.gender, s.class_id, ci.class_name AS student_className, s.username, s.password " +
                "FROM Student s " +
                "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id " +
                "WHERE s.username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Student student = new Student();
                student.setStudentId(rs.getInt("student_id"));
                student.setName(rs.getString("name"));
                student.setGender(rs.getString("gender"));
                int classId = rs.getInt("class_id");
                if (rs.wasNull()) {
                    student.setClassId(null);
                } else {
                    student.setClassId(classId);
                }
                student.setClassName(rs.getString("student_className")); // 注意这里别名
                student.setUserName(rs.getString("username"));
                student.setPassword(rs.getString("password"));
                return student;
            }, username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            System.err.println("根据用户名获取学生信息失败 (username: " + username + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- 新增：获取学生完整详细信息的方法 ---
    /**
     * 根据学生ID获取学生的详细基本信息，包括班级、专业和学院名称。
     * 这个方法返回一个 Student 对象，填充了这些关联信息。
     * 不包含课程列表和总学分，这些将在Service层组合。
     *
     * @param studentId 学生的ID。
     * @return Student 对象，包含学生详细信息；如果未找到学生，则返回 null。
     */
    public Student getStudentWithHierarchyDetailsById(int studentId) {
        // SQL 查询语句，通过多表JOIN获取所需信息
        String sql = "SELECT " +
                "s.student_id AS s_student_id, s.name AS s_name, s.gender AS s_gender, s.username AS s_username, " +
                "s.class_id AS s_class_id, ci.class_name AS ci_class_name, " +
                "m.major_name AS m_major_name, " + // 获取专业名称
                "co.college_name AS co_college_name " + // 获取学院名称
                "FROM Student s " +
                "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id " +       // 学生 -> 班级
                "LEFT JOIN Major m ON ci.major_id = m.major_id " +        // 班级 -> 专业
                "LEFT JOIN College co ON m.college_id = co.college_id " + // 专业 -> 学院
                "WHERE s.student_id = ?";
        try {
            // 使用 StudentWithDetailsRowMapper 进行映射
            return jdbcTemplate.queryForObject(sql, new StudentWithDetailsRowMapper(), studentId);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("未找到ID为 " + studentId + " 的学生（用于详细信息查询）。");
            return null;
        } catch (Exception e) {
            System.err.println("根据ID获取学生层级详细信息时发生错误 (studentId: " + studentId + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据学生ID获取该学生所有选课记录（包含课程详情、任课老师和成绩）。
     * (此方法与之前 StudentDao 中的版本相同)
     * @param studentId 学生的ID。
     * @return 包含 Enrollment 对象的列表。
     */
    public List<Enrollment> getEnrollmentsWithCourseDetailsByStudentId(int studentId) {
        String sql = "SELECT " +
                "e.enrollment_id AS e_enrollment_id, e.student_id AS e_student_id, e.course_id AS e_course_id, e.grade AS e_grade, " +
                "c.course_id AS c_course_id, c.course_name AS c_course_name, c.course_teacher AS c_course_teacher, c.credit AS c_credit " +
                "FROM Enrollment e " +
                "JOIN Course c ON e.course_id = c.course_id " +
                "WHERE e.student_id = ? " +
                "ORDER BY c.course_name ASC";
        try {
            return jdbcTemplate.query(sql, new EnrollmentWithCourseDetailsRowMapper(), studentId);
        } catch (Exception e) {
            System.err.println("根据学生ID获取选课及课程详情列表失败 (studentId: " + studentId + "): " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    public int addStudent(Student student) {
        String sql = "INSERT INTO Student (name, gender, class_id, username, password) VALUES (?, ?, ?, ?, ?)";
        try {
            Integer classId = student.getClassId();
            Object classIdParam = (classId == null) ? null : classId;
            return jdbcTemplate.update(sql,
                    student.getName(),
                    student.getGender(),
                    classIdParam,
                    student.getUserName(),
                    student.getPassword());
        } catch (Exception e) {
            System.err.println("添加学生信息失败: " + student.getName() + " (用户名: " + student.getUserName() + ") - " + e.getMessage());
            return 0;
        }
    }

    public int updateStudent(Student student) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE Student SET name = ?, gender = ?, class_id = ?, username = ?");
        List<Object> params = new ArrayList<>();
        params.add(student.getName());
        params.add(student.getGender());
        params.add(student.getClassId()); // classId 可以为 null，JDBC 会处理
        params.add(student.getUserName());

        // 检查是否需要更新密码
        // 假设 Service 层已经处理了密码的哈希 (如果需要)
        // 或者约定如果 password 字段非空，则表示需要更新
        if (student.getPassword() != null && !student.getPassword().isEmpty()) {
            sqlBuilder.append(", password = ?");
            params.add(student.getPassword()); // 直接使用传入的密码 (可能是哈希过的)
        }

        sqlBuilder.append(" WHERE student_id = ?");
        params.add(student.getStudentId());

        try {
            return jdbcTemplate.update(sqlBuilder.toString(), params.toArray());
        } catch (Exception e) {
            System.err.println("更新学生信息失败 (ID: " + student.getStudentId() + "): " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteStudent(int studentId) {
        String sql = "DELETE FROM Student WHERE student_id = ?";
        try {
            return jdbcTemplate.update(sql, studentId);
        } catch (Exception e) {
            System.err.println("删除学生信息失败 (studentId: " + studentId + "): " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // --- 数据统计相关查询 ---

    /**
     * 获取每个学院的学生人数和平均总获得学分。
     * @return List of Maps, 每个 Map 包含 "college_name", "student_count", "average_total_credits".
     *         如果学院信息缺失或学生未获得学分，相应字段可能为null或0。
     */
    public List<java.util.Map<String, Object>> getStudentStatsByCollege() {
        String sql = "SELECT " +
                     "COALESCE(col.college_name, '未分配学院') AS college_name, " +
                     "COUNT(DISTINCT s.student_id) AS student_count, " +
                     "AVG(cs.total_credits) AS average_total_credits " +
                     "FROM Student s " +
                     "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id " +
                     "LEFT JOIN Major m ON ci.major_id = m.major_id " +
                     "LEFT JOIN College col ON m.college_id = col.college_id " +
                     "LEFT JOIN CreditSummary cs ON s.student_id = cs.student_id " +
                     "GROUP BY col.college_name " +
                     "ORDER BY college_name";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("获取各学院学生统计数据失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 获取不同学分区间的学生人数统计。
     * 学分区间定义为: 0-29, 30-59, 60-89, 90+
     * @return List of Maps, 每个 Map 包含 "credit_range" 和 "student_count".
     */
    public List<java.util.Map<String, Object>> getStudentCountByCreditRanges() {
        // IFNULL(cs.total_credits, 0) 用于处理没有学分记录的学生，将其视为0学分
        String sql = "SELECT " +
                     "  CASE " +
                     "    WHEN IFNULL(cs.total_credits, 0) BETWEEN 0 AND 29.99 THEN '0-29 学分' " +
                     "    WHEN IFNULL(cs.total_credits, 0) BETWEEN 30 AND 59.99 THEN '30-59 学分' " +
                     "    WHEN IFNULL(cs.total_credits, 0) BETWEEN 60 AND 89.99 THEN '60-89 学分' " +
                     "    WHEN IFNULL(cs.total_credits, 0) >= 90 THEN '90+ 学分' " +
                     "    ELSE '0-29 学分' " + // 确保所有学生都被统计
                     "  END AS credit_range, " +
                     "  COUNT(s.student_id) AS student_count " +
                     "FROM Student s " +
                     "LEFT JOIN CreditSummary cs ON s.student_id = cs.student_id " +
                     "GROUP BY credit_range " +
                     "ORDER BY FIELD(credit_range, '0-29 学分', '30-59 学分', '60-89 学分', '90+ 学分')";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("获取学分区间学生统计数据失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}