package com.azyasaxi.dao;

import com.azyasaxi.model.Enrollment;
import com.azyasaxi.model.Student;
import com.azyasaxi.model.Course; // 虽然主要用于courseId，但RowMapper中可能需要
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // 导入 BigDecimal
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * EnrollmentDao 类 (Data Access Object)
 * 负责与数据库中的 Enrollment 表进行交互。
 */
@Repository
public class EnrollmentDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EnrollmentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 Enrollment 对象。
     * 此 RowMapper 会填充 Enrollment 对象及其关联的 Student 对象（包含学生姓名和班级名）。
     */
    private static class EnrollmentWithStudentDetailsRowMapper implements RowMapper<Enrollment> {
        @Override
        public Enrollment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrollmentId(rs.getInt("e_enrollment_id"));
            enrollment.setStudentId(rs.getInt("e_student_id"));
            enrollment.setCourseId(rs.getInt("e_course_id"));
            enrollment.setGrade(rs.getBigDecimal("e_grade"));

            Student student = new Student();
            student.setStudentId(rs.getInt("s_student_id"));
            student.setName(rs.getString("s_name"));
            student.setClassName(rs.getString("ci_class_name")); // 从ClassInfo表获取班级名称
            // 如果需要更多学生信息，可以在此填充，并确保SQL查询包含这些列

            enrollment.setStudent(student);

            // 如果还需要填充Enrollment中的Course对象的基本信息（如果Course对象不仅仅是ID）
            // Course course = new Course();
            // course.setCourseId(rs.getInt("e_course_id"));
            // // course.setCourseName(rs.getString("c_course_name")); // 如果也JOIN了Course表获取名称
            // enrollment.setCourse(course);

            return enrollment;
        }
    }

    /**
     * 根据课程ID获取所有选课记录，并包含选课学生的详细信息（学号、姓名、班级名）和成绩。
     *
     * @param courseId 课程的ID。
     * @return 包含 Enrollment 对象的列表，每个 Enrollment 对象都填充了学生信息和成绩。
     */
    public List<Enrollment> getEnrollmentsByCourseIdWithStudentDetails(int courseId) {
        String sql = "SELECT " +
                     "e.enrollment_id AS e_enrollment_id, e.student_id AS e_student_id, e.course_id AS e_course_id, e.grade AS e_grade, " +
                     "s.student_id AS s_student_id, s.name AS s_name, " +
                     "ci.class_name AS ci_class_name " +
                     "FROM Enrollment e " +
                     "JOIN Student s ON e.student_id = s.student_id " +
                     "LEFT JOIN ClassInfo ci ON s.class_id = ci.class_id " + // LEFT JOIN以防学生没有班级
                     "WHERE e.course_id = ? " +
                     "ORDER BY s.student_id ASC"; // 按学号排序
        try {
            return jdbcTemplate.query(sql, new EnrollmentWithStudentDetailsRowMapper(), courseId);
        } catch (Exception e) {
            System.err.println("根据课程ID获取选课学生列表失败 (courseId: " + courseId + "): " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 更新特定学生特定课程的成绩。
     *
     * @param studentId 学生的ID。
     * @param courseId  课程的ID。
     * @param newGrade  新的成绩。如果为 null，则数据库中的成绩也会被设置为 NULL。
     * @return 如果更新成功，返回影响的行数 (通常为 1)；如果未找到匹配的选课记录或更新失败，返回 0。
     */
    public int updateGradeByStudentAndCourse(int studentId, int courseId, BigDecimal newGrade) {
        String sql = "UPDATE Enrollment SET grade = ? WHERE student_id = ? AND course_id = ?";
        try {
            return jdbcTemplate.update(sql, newGrade, studentId, courseId);
        } catch (Exception e) {
            System.err.println("更新成绩失败 (studentId: " + studentId + ", courseId: " + courseId + "): " + e.getMessage());
            e.printStackTrace();
            return 0; // 表示更新失败
        }
    }

    // 未来可以添加其他 Enrollment 相关的方法，例如：
    // public int addEnrollment(Enrollment enrollment) { ... }
    // public int deleteEnrollment(int enrollmentId) { ... }
}