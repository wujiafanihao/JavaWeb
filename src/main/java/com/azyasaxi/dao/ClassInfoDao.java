package com.azyasaxi.dao;

import com.azyasaxi.model.ClassInfo;
import com.azyasaxi.model.Major;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper; // 用于将 ResultSet 行映射到对象
import org.springframework.stereotype.Repository; // 声明这是一个 DAO 组件

import java.sql.ResultSet;
import java.util.ArrayList; // 导入 ArrayList
import java.sql.SQLException;
import java.util.List;

/**
 * ClassInfoDao 类 (Data Access Object)
 * 负责与数据库中的 ClassInfo 表进行交互，使用 Spring JdbcTemplate 执行班级数据的增删改查操作。
 */
@Repository // 将此类标记为 Spring 管理的 DAO 组件
public class ClassInfoDao {

    private final JdbcTemplate jdbcTemplate; // Spring JdbcTemplate 实例

    /**
     * 构造函数，通过 Spring 依赖注入 JdbcTemplate。
     *
     * @param jdbcTemplate 由 Spring 容器提供的 JdbcTemplate 实例。
     */
    @Autowired // 自动注入 JdbcTemplate bean
    public ClassInfoDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 从数据库中检索所有班级的信息，或者根据提供的搜索词进行过滤。
     * 搜索基于班级名称、专业名称或学院名称的相似匹配。
     *
     * @param searchTerm 可选的搜索词。如果为 null 或空，则返回所有班级。
     * @return 包含匹配 ClassInfo 对象的列表；如果未找到或发生错误，则返回空列表。
     */
    public List<ClassInfo> searchClassInfos(String searchTerm) {
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT c.class_id, c.class_name, c.major_id, " +
            "m.major_name AS class_majorName, m.college_id, col.college_name " +
            "FROM ClassInfo c " +
            "LEFT JOIN Major m ON c.major_id = m.major_id " +
            "LEFT JOIN College col ON m.college_id = col.college_id"
        );
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likeTerm = "%" + searchTerm.trim() + "%";
            sqlBuilder.append(" WHERE (c.class_name LIKE ? OR m.major_name LIKE ? OR col.college_name LIKE ?)");
            params.add(likeTerm);
            params.add(likeTerm);
            params.add(likeTerm);
        }
        sqlBuilder.append(" ORDER BY c.class_id"); // 添加排序

        try {
            return jdbcTemplate.query(sqlBuilder.toString(), params.toArray(), new ClassInfoRowMapper());
        } catch (Exception e) {
            System.err.println("搜索班级信息失败 (searchTerm: " + searchTerm + "): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // 保留一个获取所有班级信息的方法，它现在可以调用新的搜索方法并传入null
    public List<ClassInfo> getAllClassInfo() {
        return searchClassInfos(null);
    }

    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 ClassInfo 对象。
     */
    private static class ClassInfoRowMapper implements RowMapper<ClassInfo> {
        @Override
        public ClassInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClassInfo classInfo = new ClassInfo();

            classInfo.setClassId(rs.getInt("class_id"));
            classInfo.setClassName(rs.getString("class_name"));

            // 处理 major_id (可能为 NULL)
            int majorId = rs.getInt("major_id");
            if (rs.wasNull()) {
                classInfo.setMajorId(null);
            } else {
                classInfo.setMajorId(majorId);
            }

            // 处理 college_id (可能为 NULL)
            int collegeId = rs.getInt("college_id");
            if (rs.wasNull()) {
                classInfo.setCollegeId(null);
            } else {
                classInfo.setCollegeId(collegeId);
            }

            // 设置专业名称 (从 JOIN 查询中获取，别名为 class_majorName)
            classInfo.setMajorName(rs.getString("class_majorName")); // 正确设置 majorName
            classInfo.setCollegeName(rs.getString("college_name")); // 正确设置 collegeName

            return classInfo;
        }
    }

    /**
     * 向数据库中添加一个新的班级信息。
     *
     * @param classInfo 包含新班级信息的 ClassInfo 对象。
     *                  主要使用 className 和 majorId 字段。
     * @return 如果插入成功，返回影响的行数 (通常为 1)；如果插入失败，返回 0。
     */
    public int addClassInfo(ClassInfo classInfo) {
        String sql = "INSERT INTO ClassInfo (class_name, major_id) VALUES (?, ?)";
        try {
            // classInfo.getMajorId() 可能返回 null，需要正确处理 PreparedStatement 的 setNull
            Integer majorId = classInfo.getMajorId();
            if (majorId == null) {
                return jdbcTemplate.update(sql, classInfo.getClassName(), null);
            } else {
                return jdbcTemplate.update(sql, classInfo.getClassName(), majorId);
            }
        } catch (Exception e) {
            System.err.println("添加班级信息失败: " + classInfo.getClassName() + " - " + e.getMessage());
            e.printStackTrace();
            return 0; // 表示失败
        }
    }

    // 未来可以根据需求添加其他班级相关的数据库操作方法，例如：
    // public ClassInfo getClassInfoById(int classId) { ... }
    // public int updateClassInfo(ClassInfo classInfo) { ... }
    // public int deleteClassInfo(int classId) { ... }
}
