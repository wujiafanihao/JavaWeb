package com.azyasaxi.dao;

import com.azyasaxi.model.AdminLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Repository
public class AdminLogDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AdminLogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class AdminLogMapper implements RowMapper<AdminLog> {
        @Override
        public AdminLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdminLog log = new AdminLog();
            log.setLogId(rs.getInt("log_id"));
            
            int adminId = rs.getInt("admin_id");
            if (rs.wasNull()) {
                log.setAdminId(null);
            } else {
                log.setAdminId(adminId);
            }
            
            log.setAdminUsername(rs.getString("admin_username"));
            log.setActionType(rs.getString("action_type"));
            log.setTargetEntity(rs.getString("target_entity"));
            log.setTargetEntityId(rs.getString("target_entity_id"));
            log.setActionDescription(rs.getString("action_description"));
            
            Timestamp timestamp = rs.getTimestamp("action_timestamp");
            if (timestamp != null) {
                log.setActionTimestamp(timestamp.toLocalDateTime());
            }
            return log;
        }
    }

    /**
     * 添加一条管理员操作日志。
     * @param log AdminLog 对象，不包含 logId (会自动生成) 和 actionTimestamp (数据库默认)。
     * @return 如果插入成功返回 true，否则返回 false。
     */
    public boolean addLog(AdminLog log) {
        String sql = "INSERT INTO AdminLog (admin_id, admin_username, action_type, target_entity, target_entity_id, action_description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    log.getAdminId(),
                    log.getAdminUsername(),
                    log.getActionType(),
                    log.getTargetEntity(),
                    log.getTargetEntityId(),
                    log.getActionDescription());
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("添加管理员日志失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 分页获取管理员操作日志。
     * @param offset 记录的起始偏移量。
     * @param limit 每页的记录数。
     * @return AdminLog 对象的列表。
     */
    public List<AdminLog> getLogs(int offset, int limit) {
        String sql = "SELECT * FROM AdminLog ORDER BY action_timestamp DESC LIMIT ? OFFSET ?";
        try {
            return jdbcTemplate.query(sql, new AdminLogMapper(), limit, offset);
        } catch (Exception e) {
            System.err.println("获取管理员日志列表失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有管理员操作日志的总数。
     * @return 日志总数。
     */
    public int countLogs() {
        String sql = "SELECT COUNT(*) FROM AdminLog";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("统计管理员日志总数失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}