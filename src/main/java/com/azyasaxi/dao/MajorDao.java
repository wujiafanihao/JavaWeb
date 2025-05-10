package com.azyasaxi.dao;

import com.azyasaxi.model.Major;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * MajorDao 类 (Data Access Object)
 * 负责与数据库中的 Major 表进行交互。
 */
@Repository
public class MajorDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MajorDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 从数据库中检索所有专业的信息。
     *
     * @return 包含所有 Major 对象的列表。
     */
    public List<Major> getAllMajors() {
        String sql = "SELECT major_id, major_name, college_id FROM Major ORDER BY major_name";
        try {
            return jdbcTemplate.query(sql, new MajorRowMapper());
        } catch (Exception e) {
            System.err.println("查询所有专业信息失败: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // 返回空列表表示查询失败或没有数据
        }
    }

    private static class MajorRowMapper implements RowMapper<Major> {
        @Override
        public Major mapRow(ResultSet rs, int rowNum) throws SQLException {
            Major major = new Major();
            major.setMajorId(rs.getInt("major_id"));
            major.setMajorName(rs.getString("major_name"));
            // college_id 可能为 NULL
            int collegeId = rs.getInt("college_id");
            if (rs.wasNull()) {
                major.setCollegeId(null);
            } else {
                major.setCollegeId(collegeId);
            }
            return major;
        }
    }

    // 未来可以添加其他方法，如 findById, addMajor, updateMajor, deleteMajor等
}