package com.azyasaxi.dao;

import com.azyasaxi.model.Course; // 导入 Course 模型类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseDao 类 (Data Access Object)
 * 负责与数据库中的 Course 表进行交互，使用 Spring JdbcTemplate 执行课程数据的增删改查操作。
 */
@Repository // 将此类标记为 Spring 管理的 DAO 组件
public class CourseDao {

    private final JdbcTemplate jdbcTemplate; // Spring JdbcTemplate 实例

    /**
     * 构造函数，通过 Spring 依赖注入 JdbcTemplate。
     *
     * @param jdbcTemplate 由 Spring 容器提供的 JdbcTemplate 实例。
     */
    @Autowired // 自动注入 JdbcTemplate bean
    public CourseDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 Course 对象。
     * 会连接 Major 表以获取专业名称。
     */
    private static class CourseRowMapper implements RowMapper<Course> {
        @Override
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
            Course course = new Course();
            course.setCourseId(rs.getInt("course_id"));
            course.setCourseName(rs.getString("course_name"));
            course.setCourseTeacher(rs.getString("course_teacher"));
            course.setCredit(rs.getBigDecimal("credit"));

            // 处理 major_id (可能为 NULL, 尽管在您的表结构中 major_id 是 Course 表的一个外键，
            // 但根据您的需求 "课程所属专业，用于区分专业课和选修课的判断"，它可能允许为空来表示全校选修课)
            // 如果业务逻辑要求major_id不能为空，那么这里的处理可以简化。
            int majorId = rs.getInt("major_id");
            if (rs.wasNull()) {
                course.setMajorId(null);
                course.setMajorName(null); // 如果major_id为null，专业名称也应为null或特定值
            } else {
                course.setMajorId(majorId);
                course.setMajorName(rs.getString("major_name")); // 从 JOIN 查询中获取专业名称
            }
            return course;
        }
    }

    /**
     * 从数据库中检索所有课程的信息，或者根据提供的搜索词进行过滤。
     * 搜索基于课程名称或课程教师的相似匹配。
     *
     * @param searchTerm 可选的搜索词。如果为 null 或空，则返回所有课程。
     * @return 包含匹配 Course 对象的列表；如果未找到或发生错误，则返回空列表。
     */
    public List<Course> searchCourses(String searchTerm) {
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT c.course_id, c.course_name, c.course_teacher, c.credit, c.major_id, " +
                        "m.major_name " + // 获取专业名称
                        "FROM Course c " +
                        "LEFT JOIN Major m ON c.major_id = m.major_id" // 左连接以包含没有关联专业的课程（如果允许）
        );
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likeTerm = "%" + searchTerm.trim() + "%";
            // 根据课程名称或课程老师进行搜索
            sqlBuilder.append(" WHERE (c.course_name LIKE ? OR c.course_teacher LIKE ?)");
            params.add(likeTerm);
            params.add(likeTerm);
        }
        sqlBuilder.append(" ORDER BY c.course_id"); // 添加排序

        try {
            return jdbcTemplate.query(sqlBuilder.toString(), params.toArray(), new CourseRowMapper());
        } catch (Exception e) {
            System.err.println("搜索课程信息失败 (searchTerm: " + searchTerm + "): " + e.getMessage());
            e.printStackTrace();
            return List.of(); // 返回空列表表示错误或未找到
        }
    }

    /**
     * 获取所有课程信息。
     *
     * @return 包含所有 Course 对象的列表。
     */
    public List<Course> getAllCourses() {
        return searchCourses(null); // 调用搜索方法，传入null以获取所有
    }

    /**
     * 向数据库中添加一个新的课程信息。
     * 注意：这里我们假设通过专业名称来查找 major_id。如果专业不存在，则添加失败。
     * 或者，您可以选择先创建专业（如果不存在）或让调用者确保专业已存在并传递 major_id。
     * 为了简化，这里假设调用者会提供一个有效的 major_id (或者允许 major_id 为 null)。
     *
     * 通过课程名称，任课老师，学分和专业名称进行添加"，
     * 我们需要先根据专业名称查询到 major_id。
     *
     * @param course 包含新课程信息的 Course 对象。
     *               需要 courseName, courseTeacher, credit。
     *               majorName 用于查找 majorId，如果 majorName 为 null 或空，则 majorId 也为 null。
     * @return 如果插入成功，返回影响的行数 (通常为 1)；如果插入失败（例如专业名找不到），返回 0 或负数。
     */
    public int addCourse(Course course, String majorName) {
        String sql = "INSERT INTO Course (course_name, course_teacher, credit, major_id) VALUES (?, ?, ?, ?)";
        Integer majorIdToInsert = null;

        // 如果提供了专业名称，则尝试根据专业名称获取 major_id
        if (majorName != null && !majorName.trim().isEmpty()) {
            String findMajorIdSql = "SELECT major_id FROM Major WHERE major_name = ?";
            try {
                // queryForObject 期望返回单行单列，如果找不到会抛出 EmptyResultDataAccessException
                majorIdToInsert = jdbcTemplate.queryForObject(findMajorIdSql, Integer.class, majorName.trim());
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                System.err.println("添加课程失败：找不到专业名称 '" + majorName + "' 对应的专业ID。");
                return -1; // 表示专业名未找到
            } catch (Exception e) {
                System.err.println("查询专业ID时出错: " + e.getMessage());
                e.printStackTrace();
                return -2; // 表示查询专业ID时发生其他错误
            }
        } else {
            // 如果没有提供专业名称，或者您允许课程没有专业关联，则 majorIdToInsert 保持为 null
            // 确保您的 Course 表的 major_id 字段允许 NULL 值，如果这是您的业务需求。
            // 根据您的表结构，major_id 是外键，所以它要么是有效的Major表ID，要么如果允许NULL，则为NULL。
            System.out.println("添加课程时未提供专业名称。");
        }

        try {
            // 执行插入课程的操作
            // 如果 majorIdToInsert 是 null，我们需要使用 PreparedStatement.setNull
            if (majorIdToInsert == null) {
                return jdbcTemplate.update(sql, course.getCourseName(), course.getCourseTeacher(), course.getCredit(), null);
            } else {
                return jdbcTemplate.update(sql, course.getCourseName(), course.getCourseTeacher(), course.getCredit(), majorIdToInsert);
            }
        } catch (Exception e) {
            System.err.println("添加课程信息失败: " + course.getCourseName() + " - " + e.getMessage());
            // 检查是否是唯一性约束冲突
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique constraint") || e.getMessage().toLowerCase().contains("duplicate entry")) {
                System.err.println("错误：课程 '" + course.getCourseName() + "' 在该专业下已存在。");
                return -3; // 表示唯一性约束冲突
            }
            e.printStackTrace();
            return 0; // 表示其他插入失败
        }
    }


    // 根据需求，未来可以添加更多方法：
    /**
     * 根据课程ID获取课程信息。
     *
     * @param courseId 课程ID。
     * @return Course 对象，如果未找到则返回 null。
     */
    public Course getCourseById(int courseId) {
        String sql = "SELECT c.course_id, c.course_name, c.course_teacher, c.credit, c.major_id, m.major_name " +
                "FROM Course c " +
                "LEFT JOIN Major m ON c.major_id = m.major_id " +
                "WHERE c.course_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new CourseRowMapper(), courseId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null; // 未找到课程
        } catch (Exception e) {
            System.err.println("根据ID获取课程信息失败 (courseId: " + courseId + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新现有课程信息。
     *
     * @param course 要更新的 Course 对象，其 courseId 必须有效。
     * @param majorName (可选) 新的专业名称，用于更新 major_id。如果为 null，则不更新专业。
     * @return 影响的行数。
     */
    public int updateCourse(Course course, String majorName) {
        String sql = "UPDATE Course SET course_name = ?, course_teacher = ?, credit = ?, major_id = ? WHERE course_id = ?";
        Integer majorIdToUpdate = course.getMajorId(); // 默认使用课程对象中已有的majorId

        if (majorName != null && !majorName.trim().isEmpty()) {
            String findMajorIdSql = "SELECT major_id FROM Major WHERE major_name = ?";
            try {
                majorIdToUpdate = jdbcTemplate.queryForObject(findMajorIdSql, Integer.class, majorName.trim());
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                System.err.println("更新课程失败：找不到专业名称 '" + majorName + "' 对应的专业ID。");
                return -1;
            } catch (Exception e) {
                System.err.println("查询专业ID时出错 (更新课程): " + e.getMessage());
                e.printStackTrace();
                return -2;
            }
        } else if (majorName != null && majorName.trim().isEmpty()) { // 如果传入空字符串，表示希望清除专业关联
            majorIdToUpdate = null;
        }
        // 如果 majorName 是 null (不是空字符串)，则保持 course 对象中原有的 majorId 不变

        try {
            if (majorIdToUpdate == null) {
                return jdbcTemplate.update(sql, course.getCourseName(), course.getCourseTeacher(), course.getCredit(),
                        null, // 使用 Types.INTEGER 来显式设置 NULL
                        course.getCourseId());
            } else {
                return jdbcTemplate.update(sql, course.getCourseName(), course.getCourseTeacher(), course.getCredit(),
                        majorIdToUpdate, course.getCourseId());
            }
        } catch (Exception e) {
            System.err.println("更新课程信息失败 (courseId: " + course.getCourseId() + "): " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据课程ID删除课程。
     *
     * @param courseId 要删除的课程ID。
     * @return 影响的行数。
     */
    public int deleteCourse(int courseId) {
        String sql = "DELETE FROM Course WHERE course_id = ?";
        try {
            return jdbcTemplate.update(sql, courseId);
        } catch (Exception e) {
            System.err.println("删除课程信息失败 (courseId: " + courseId + "): " + e.getMessage());
            // 需要考虑外键约束，Enrollment表引用了Course。
            // 如果有学生选了这门课，直接删除可能会失败（除非设置了级联删除或SET NULL，这里是Enrollment级联删除Course）
            e.printStackTrace();
            return 0;
        }
    }

}