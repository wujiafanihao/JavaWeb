package com.azyasaxi.dao;

import com.azyasaxi.model.ClassInfo; // 导入 ClassInfo 模型类
// Major 和 College 的导入保持不变，因为 RowMapper 中会用到
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
 * ClassInfoDao 类 (Data Access Object)
 * 负责与数据库中的 ClassInfo 表进行交互，执行班级数据的增删改查操作。
 */
@Repository
public class ClassInfoDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ClassInfoDao(JdbcTemplate jdbcTemplateArg) {
        this.jdbcTemplate = jdbcTemplateArg;
    }

    // --- RowMapper ---
    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 ClassInfo 对象。
     * 此 RowMapper 会尝试从 JOIN 的结果中填充 majorName 和 collegeName。
     */
    private static class ClassInfoWithHierarchyRowMapper implements RowMapper<ClassInfo> {
        @Override
        public ClassInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setClassId(rs.getInt("ci_class_id"));         // 使用别名
            classInfo.setClassName(rs.getString("ci_class_name"));

            int majorId = rs.getInt("ci_major_id");
            if (rs.wasNull()) {
                classInfo.setMajorId(null);
            } else {
                classInfo.setMajorId(majorId);
            }
            classInfo.setMajorName(rs.getString("m_major_name")); // 从 Major 表获取

            // collegeId 和 collegeName 是通过 Major 表间接关联的
            int collegeId = rs.getInt("m_college_id"); // Major 表中的 college_id
            if (rs.wasNull()) {
                classInfo.setCollegeId(null); // ClassInfo 对象中的 collegeId
            } else {
                classInfo.setCollegeId(collegeId);
            }
            classInfo.setCollegeName(rs.getString("co_college_name")); // 从 College 表获取

            return classInfo;
        }
    }

    // --- 查询方法 ---
    public List<ClassInfo> searchClassInfos(String searchTerm) {
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT ci.class_id AS ci_class_id, ci.class_name AS ci_class_name, ci.major_id AS ci_major_id, " +
                        "m.major_name AS m_major_name, m.college_id AS m_college_id, col.college_name AS co_college_name " +
                        "FROM ClassInfo ci " +
                        "LEFT JOIN Major m ON ci.major_id = m.major_id " +
                        "LEFT JOIN College col ON m.college_id = col.college_id"
        );
        List<Object> params = new ArrayList<>();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likeTerm = "%" + searchTerm.trim() + "%";
            sqlBuilder.append(" WHERE (ci.class_name LIKE ? OR m.major_name LIKE ? OR col.college_name LIKE ?)");
            params.add(likeTerm);
            params.add(likeTerm);
            params.add(likeTerm);
        }
        sqlBuilder.append(" ORDER BY ci.class_id ASC");
        try {
            return jdbcTemplate.query(sqlBuilder.toString(), params.toArray(), new ClassInfoWithHierarchyRowMapper());
        } catch (Exception e) {
            System.err.println("搜索班级信息失败 (searchTerm: " + searchTerm + "): " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<ClassInfo> getAllClassInfo() {
        return searchClassInfos(null);
    }

    /**
     * 根据班级ID获取班级的详细信息，包括其所属专业和学院的名称。
     * 此方法不包含班级下的学生列表，学生列表需单独查询。
     *
     * @param classId 班级的ID。
     * @return ClassInfo 对象，包含班级、专业和学院信息；如果未找到班级，则返回 null。
     */
    public ClassInfo getClassInfoWithHierarchyById(int classId) {
        String sql = "SELECT ci.class_id AS ci_class_id, ci.class_name AS ci_class_name, ci.major_id AS ci_major_id, " +
                "m.major_name AS m_major_name, m.college_id AS m_college_id, col.college_name AS co_college_name " +
                "FROM ClassInfo ci " +
                "LEFT JOIN Major m ON ci.major_id = m.major_id " +
                "LEFT JOIN College col ON m.college_id = col.college_id " +
                "WHERE ci.class_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new ClassInfoWithHierarchyRowMapper(), classId);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("未找到ID为 " + classId + " 的班级信息。");
            return null; // 未找到班级
        } catch (Exception e) {
            System.err.println("根据ID获取班级详细信息失败 (classId: " + classId + "): " + e.getMessage());
            e.printStackTrace();
            return null; // 发生其他错误
        }
    }

    // --- 修改方法 (保持原有) ---
    public int addClassInfo(ClassInfo classInfo) {
        // (与您之前提供的代码一致)
        String sql = "INSERT INTO ClassInfo (class_name, major_id) VALUES (?, ?)";
        try {
            Integer majorId = classInfo.getMajorId();
            Object majorIdParam = (majorId == null) ? null : majorId;
            return jdbcTemplate.update(sql, classInfo.getClassName(), majorIdParam);
        } catch (Exception e) {
            System.err.println("添加班级信息失败: " + classInfo.getClassName() + " - " + e.getMessage());
            return 0;
        }
    }

    public int deleteClassInfo(int classId) {
        // (与您之前提供的代码一致)
        String sql = "DELETE FROM ClassInfo WHERE class_id = ?";
        try {
            return jdbcTemplate.update(sql, classId);
        } catch (Exception e) {
            System.err.println("删除班级信息失败 (classId: " + classId + "): " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // updateClassInfo 方法可以后续根据需求添加
}