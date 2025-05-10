package com.azyasaxi.dao;

import com.azyasaxi.model.CreditSummary; // 导入 CreditSummary 模型类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections; // 用于返回空列表
import java.util.List;

/**
 * CreditSummaryDao 类 (Data Access Object)
 * 负责从数据库中的 CreditSummary 视图查询学生已获得的学分统计信息。
 * 使用 Spring JdbcTemplate 执行查询操作。
 */
@Repository // 将此类标记为 Spring 管理的 DAO 组件
public class CreditSummaryDao {

    private final JdbcTemplate jdbcTemplate; // Spring JdbcTemplate 实例

    /**
     * 构造函数，通过 Spring 依赖注入 JdbcTemplate。
     *
     * @param jdbcTemplateArg 由 Spring 容器提供的 JdbcTemplate 实例。
     */
    @Autowired // 自动注入 JdbcTemplate bean
    public CreditSummaryDao(JdbcTemplate jdbcTemplateArg) {
        this.jdbcTemplate = jdbcTemplateArg;
    }

    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 CreditSummary 对象。
     */
    private static class CreditSummaryRowMapper implements RowMapper<CreditSummary> {
        @Override
        public CreditSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            CreditSummary summary = new CreditSummary();
            summary.setStudentId(rs.getInt("student_id"));       // 从视图获取 student_id
            summary.setStudentName(rs.getString("student_name")); // 从视图获取 student_name
            summary.setTotalCredits(rs.getBigDecimal("total_credits")); // 从视图获取 total_credits
            return summary;
        }
    }

    /**
     * 从 CreditSummary 视图中检索所有学生的学分统计信息，或者根据提供的学生姓名进行过滤。
     * 搜索基于学生姓名的相似匹配。
     *
     * @param searchTerm 可选的搜索词，可以是学生姓名或学号。如果为 null 或空，则返回所有学生的学分统计。
     * @return 包含匹配 CreditSummary 对象的列表；如果未找到或发生错误，则返回空列表。
     */
    public List<CreditSummary> searchCreditSummaries(String searchTerm) {
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT student_id, student_name, total_credits FROM CreditSummary"
        );
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String trimmedSearchTerm = searchTerm.trim();
            try {
                // 尝试将搜索词解析为整数 (学号)
                int studentId = Integer.parseInt(trimmedSearchTerm);
                sqlBuilder.append(" WHERE student_id = ?");
                params.add(studentId);
            } catch (NumberFormatException e) {
                // 如果解析失败，则按学生姓名进行模糊搜索
                sqlBuilder.append(" WHERE student_name LIKE ?");
                params.add("%" + trimmedSearchTerm + "%");
            }
        }

        sqlBuilder.append(" ORDER BY total_credits DESC, student_name ASC");

        try {
            return jdbcTemplate.query(sqlBuilder.toString(), params.toArray(), new CreditSummaryRowMapper());
        } catch (Exception e) {
            System.err.println("搜索学分统计信息失败 (searchTerm: " + searchTerm + "): " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有学生的学分统计信息。
     *
     * @return 包含所有学生 CreditSummary 对象的列表。
     */
    public List<CreditSummary> getAllCreditSummaries() {
        // 调用搜索方法，不传递任何搜索条件，以获取所有记录
        return searchCreditSummaries(null);
    }

    /**
     * 根据学生ID获取该学生的学分统计信息。
     *
     * @param studentId 学生的ID。
     * @return CreditSummary 对象，如果未找到该学生的学分记录则返回 null。
     */
    public CreditSummary getCreditSummaryByStudentId(int studentId) {
        String sql = "SELECT student_id, student_name, total_credits FROM CreditSummary WHERE student_id = ?";
        try {
            // queryForObject 期望返回单条记录，如果找不到会抛出 EmptyResultDataAccessException
            return jdbcTemplate.queryForObject(sql, new CreditSummaryRowMapper(), studentId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // 如果没有找到对应ID的记录，返回null是合理的行为
            return null;
        } catch (Exception e) {
            System.err.println("根据学生ID获取学分统计信息失败 (studentId: " + studentId + "): " + e.getMessage());
            e.printStackTrace();
            return null; // 发生其他错误时也返回null
        }
    }

    // 注意：CreditSummary 是一个视图，通常是只读的。
    // 因此，一般不会有 add, update, delete 等修改数据的方法针对这个视图。
    // 对学分的修改是通过 Enrollment 表中的成绩来实现的，视图会自动更新。
}