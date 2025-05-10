package com.azyasaxi.dao;

import com.azyasaxi.model.Student; // 导入 Student 模型类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper; // 用于将 ResultSet 行映射到对象
import org.springframework.stereotype.Repository; // 声明这是一个 DAO 组件

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; // 导入 ArrayList
import java.util.List;

/**
 * StudentDao 类 (Data Access Object)
 * 负责与数据库中的 Student 表进行交互，使用 Spring JdbcTemplate 执行学生数据的增删改查操作。
 */
@Repository // 将此类标记为 Spring 管理的 DAO 组件
public class StudentDao {

    private final JdbcTemplate jdbcTemplate; // Spring JdbcTemplate 实例

    /**
     * 构造函数，通过 Spring 依赖注入 JdbcTemplate。
     *
     * @param jdbcTemplate 由 Spring 容器提供的 JdbcTemplate 实例。
     */
    @Autowired // 自动注入 JdbcTemplate bean
    public StudentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 从数据库中检索所有学生的信息，或者根据提供的搜索词进行过滤。
     * 搜索基于学生姓名或班级名称的相似匹配。
     *
     * @param searchTerm 可选的搜索词。如果为 null 或空，则返回所有学生。
     * @return 包含匹配 Student 对象的列表；如果未找到或发生错误，则返回空列表。
     */
    public List<Student> searchStudents(String searchTerm) {
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT s.student_id, s.name, s.gender, s.class_id, ci.class_name AS student_className, s.username, s.password " +
            "FROM Student s " +
            "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id"
        );
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likeTerm = "%" + searchTerm.trim() + "%";
            // 根据学生姓名 (s.name) 或班级名称 (ci.class_name) 进行搜索
            sqlBuilder.append(" WHERE (s.name LIKE ? OR ci.class_name LIKE ?)");
            params.add(likeTerm); // 学生姓名的参数
            params.add(likeTerm); // 班级名称的参数
        }
        sqlBuilder.append(" ORDER BY s.student_id"); // 添加排序

        try {
            return jdbcTemplate.query(sqlBuilder.toString(), params.toArray(), new StudentRowMapper());
        } catch (Exception e) {
            System.err.println("搜索学生信息失败 (searchTerm: " + searchTerm + "): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 获取所有学生的信息。此方法现在调用 searchStudents 并传入null作为搜索词。
     * @return 包含所有 Student 对象的列表。
     */
    public List<Student> getAllStudents() {
        return searchStudents(null);
    }

    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 Student 对象。
     */
    private static class StudentRowMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setStudentId(rs.getInt("student_id"));
            student.setName(rs.getString("name"));
            student.setGender(rs.getString("gender"));

            // 处理 class_id (可能为 NULL)
            int classId = rs.getInt("class_id");
            if (rs.wasNull()) {
                student.setClassId(null);
            } else {
                student.setClassId(classId);
            }

            // 设置班级名称 (从 JOIN 查询中获取，别名为 student_className)
            // 如果 class_id 为 NULL，则 student_className 也应该为 NULL
            student.setClassName(rs.getString("student_className"));

            student.setUserName(rs.getString("username"));
            student.setPassword(rs.getString("password")); // 注意：通常不建议将密码直接加载到列表视图中
            return student;
        }
    }

    /**
     * 向数据库中添加一个新的学生信息。
     *
     * @param student 包含新学生信息的 Student 对象。
     * @return 如果插入成功，返回影响的行数 (通常为 1)；如果插入失败（例如用户名已存在），返回 0。
     */
    public int addStudent(Student student) {
        String sql = "INSERT INTO Student (name, gender, class_id, username, password) VALUES (?, ?, ?, ?, ?)";
        try {
            // student.getClassId() 可能返回 null，需要正确处理 PreparedStatement 的 setNull
            Integer classId = student.getClassId();
            Object classIdParam = (classId == null) ? null : classId;

            return jdbcTemplate.update(sql,
                    student.getName(),
                    student.getGender(),
                    classIdParam,
                    student.getUserName(),
                    student.getPassword()); // 注意：密码应该在Service层或更高层进行加密处理后再传入DAO
        } catch (Exception e) {
            // 例如，如果 username 不是唯一的，会抛出 DataIntegrityViolationException
            System.err.println("添加学生信息失败: " + student.getName() + " (Username: " + student.getUserName() + ") - " + e.getMessage());
            // e.printStackTrace(); // 可以取消注释以进行更详细的调试
            return 0; // 表示失败
        }
    }

    // 未来可以根据需求添加其他学生相关的数据库操作方法，例如：
    // public Student getStudentById(int studentId) { ... }
    // public int updateStudent(Student student) { ... }
    // public int deleteStudent(int studentId) { ... }
}
