package com.azyasaxi.dao;

import com.azyasaxi.model.LeaveRequest; // 导入 LeaveRequest 模型类
import com.azyasaxi.model.Student;    // 可能需要 Student 模型用于连接查询学生姓名
import com.azyasaxi.model.Admin;      // 可能需要 Admin 模型用于连接查询审批管理员姓名
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder; // 用于获取自增主键
import org.springframework.jdbc.support.KeyHolder;       // 用于获取自增主键
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement; // 用于设置 Statement.RETURN_GENERATED_KEYS
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;   // 用于 Statement.RETURN_GENERATED_KEYS
import java.sql.Timestamp;   // 用于处理 DATETIME 类型
import java.util.ArrayList;
import java.util.Collections; // 用于返回空列表
import java.util.List;

/**
 * LeaveRequestDao 类 (Data Access Object)
 * 负责与数据库中的 LeaveRequest 表进行交互，使用 Spring JdbcTemplate 执行请假申请数据的增删改查操作。
 */
@Repository // 将此类标记为 Spring 管理的 DAO 组件
public class LeaveRequestDao {

    private final JdbcTemplate jdbcTemplate; // Spring JdbcTemplate 实例

    /**
     * 构造函数，通过 Spring 依赖注入 JdbcTemplate。
     *
     * @param jdbcTemplateArg 由 Spring 容器提供的 JdbcTemplate 实例。
     *                       参数名使用 jdbcTemplateArg 以避免与成员变量 jdbcTemplate 混淆。
     */
    @Autowired // 自动注入 JdbcTemplate bean
    public LeaveRequestDao(JdbcTemplate jdbcTemplateArg) {
        this.jdbcTemplate = jdbcTemplateArg;
    }

    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 LeaveRequest 对象。
     * 此 RowMapper 也会尝试连接 Student 表获取学生姓名，以及 Admin 表获取审批管理员姓名 (如果存在)。
     */
    private static class LeaveRequestRowMapper implements RowMapper<LeaveRequest> {
        @Override
        public LeaveRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setLeaveId(rs.getInt("lr_leave_id")); // 使用别名以防列名冲突
            leaveRequest.setStudentId(rs.getInt("lr_student_id"));
            leaveRequest.setReason(rs.getString("lr_reason"));

            // 将 java.sql.Date 转换为 java.util.Date
            java.sql.Date sqlStartDate = rs.getDate("lr_start_date");
            if (sqlStartDate != null) {
                leaveRequest.setStartDate(new java.util.Date(sqlStartDate.getTime()));
            }
            java.sql.Date sqlEndDate = rs.getDate("lr_end_date");
            if (sqlEndDate != null) {
                leaveRequest.setEndDate(new java.util.Date(sqlEndDate.getTime()));
            }

            leaveRequest.setStatus(rs.getString("lr_status"));

            // 将 java.sql.Timestamp 转换为 java.util.Date
            Timestamp sqlRequestDate = rs.getTimestamp("lr_request_date");
            if (sqlRequestDate != null) {
                leaveRequest.setRequestDate(new java.util.Date(sqlRequestDate.getTime()));
            }
            Timestamp sqlApprovalDate = rs.getTimestamp("lr_approval_date");
            if (sqlApprovalDate != null) {
                leaveRequest.setApprovalDate(new java.util.Date(sqlApprovalDate.getTime()));
            }

            // 处理可能为NULL的 approved_by_admin_id
            int approvedByAdminId = rs.getInt("lr_approved_by_admin_id");
            if (rs.wasNull()) {
                leaveRequest.setApprovedByAdminId(null);
            } else {
                leaveRequest.setApprovedByAdminId(approvedByAdminId);
            }

            // 尝试从JOIN的表中获取额外信息 (学生姓名, 管理员姓名)
            // 这些字段的存在取决于执行查询时是否进行了JOIN
            // 为了通用性，检查列是否存在，或者在查询时确保它们被选择并使用别名
            try {
                if (hasColumn(rs, "student_name")) { // 假设JOIN查询中选择了 s.name AS student_name
                    leaveRequest.setStudentName(rs.getString("student_name"));
                }
                if (hasColumn(rs, "admin_username")) { // 假设JOIN查询中选择了 a.username AS admin_username
                    leaveRequest.setAdminUsername(rs.getString("admin_username"));
                }
            } catch (SQLException e) {
                // 列不存在或获取失败，可以忽略或记录日志，不中断主对象映射
                // System.err.println("LeaveRequestRowMapper: 获取 student_name 或 admin_username 时出错: " + e.getMessage());
            }

            return leaveRequest;
        }

        /**
         * 辅助方法，检查 ResultSet 中是否存在指定的列名。
         * @param rs ResultSet 对象。
         * @param columnName 要检查的列名。
         * @return 如果列存在则返回 true，否则返回 false。
         */
        private boolean hasColumn(ResultSet rs, String columnName) {
            try {
                rs.findColumn(columnName); // 尝试查找列
                return true; // 如果没抛异常，说明列存在
            } catch (SQLException e) {
                return false; // 抛异常说明列不存在
            }
        }
    }

    /**
     * 从数据库中检索所有请假申请的信息，或者根据提供的搜索条件进行过滤。
     * 搜索可以基于学生姓名或请假状态。
     *
     * @param studentNameSearchTerm 可选的学生姓名搜索词。
     * @param statusSearchTerm 可选的请假状态搜索词。
     * @return 包含匹配 LeaveRequest 对象的列表；如果未找到或发生错误，则返回空列表。
     */
    public List<LeaveRequest> searchLeaveRequests(String studentNameSearchTerm, String statusSearchTerm) {
        // SQL 查询语句，连接 Student 表获取学生姓名，连接 Admin 表获取审批管理员用户名
        // 使用别名 lr, s, a 来区分不同表中的同名字段
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT lr.leave_id AS lr_leave_id, lr.student_id AS lr_student_id, lr.reason AS lr_reason, " +
                        "lr.start_date AS lr_start_date, lr.end_date AS lr_end_date, lr.status AS lr_status, " +
                        "lr.request_date AS lr_request_date, lr.approval_date AS lr_approval_date, " +
                        "lr.approved_by_admin_id AS lr_approved_by_admin_id, " +
                        "s.name AS student_name, " + // 从 Student 表获取学生姓名
                        "a.username AS admin_username " + // 从 Admin 表获取审批管理员的用户名
                        "FROM LeaveRequest lr " +
                        "JOIN Student s ON lr.student_id = s.student_id " + // 内连接 Student 表
                        "LEFT JOIN Admin a ON lr.approved_by_admin_id = a.admin_id" // 左连接 Admin 表，因为审批管理员可能为空
        );
        List<Object> params = new ArrayList<>(); // 用于存放查询参数
        boolean hasWhereClause = false; // 标记是否已添加WHERE子句

        // 根据学生姓名搜索条件构建SQL
        if (studentNameSearchTerm != null && !studentNameSearchTerm.trim().isEmpty()) {
            sqlBuilder.append(" WHERE s.name LIKE ?");
            params.add("%" + studentNameSearchTerm.trim() + "%");
            hasWhereClause = true;
        }

        // 根据请假状态搜索条件构建SQL
        if (statusSearchTerm != null && !statusSearchTerm.trim().isEmpty()) {
            if (hasWhereClause) {
                sqlBuilder.append(" AND lr.status = ?"); // 如果已有WHERE，则用AND连接
            } else {
                sqlBuilder.append(" WHERE lr.status = ?"); // 否则用WHERE开始
                // hasWhereClause = true; // 如果后面还有其他条件，这里应该设置为true
            }
            params.add(statusSearchTerm.trim());
        }

        sqlBuilder.append(" ORDER BY lr.request_date DESC, lr.leave_id DESC"); // 按申请时间降序排序，再按ID降序

        try {
            // 执行查询，并使用 LeaveRequestRowMapper 将结果映射到 LeaveRequest 对象列表
            return jdbcTemplate.query(sqlBuilder.toString(), params.toArray(), new LeaveRequestRowMapper());
        } catch (Exception e) {
            System.err.println("搜索请假申请信息失败 (studentName: " + studentNameSearchTerm + ", status: " + statusSearchTerm + "): " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息到控制台
            return Collections.emptyList(); // 发生错误时返回空列表
        }
    }

    /**
     * 获取所有请假申请信息。
     *
     * @return 包含所有 LeaveRequest 对象的列表。
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        // 调用搜索方法，不传递任何搜索条件，以获取所有记录
        return searchLeaveRequests(null, null);
    }

    /**
     * 根据学生ID获取该学生的所有请假申请信息。
     *
     * @param studentId 学生的ID。
     * @return 包含指定学生所有 LeaveRequest 对象的列表。
     */
    public List<LeaveRequest> getLeaveRequestsByStudentId(int studentId) {
        String sql = "SELECT lr.leave_id AS lr_leave_id, lr.student_id AS lr_student_id, lr.reason AS lr_reason, " +
                "lr.start_date AS lr_start_date, lr.end_date AS lr_end_date, lr.status AS lr_status, " +
                "lr.request_date AS lr_request_date, lr.approval_date AS lr_approval_date, " +
                "lr.approved_by_admin_id AS lr_approved_by_admin_id, " +
                "s.name AS student_name, a.username AS admin_username " +
                "FROM LeaveRequest lr " +
                "JOIN Student s ON lr.student_id = s.student_id " +
                "LEFT JOIN Admin a ON lr.approved_by_admin_id = a.admin_id " +
                "WHERE lr.student_id = ? ORDER BY lr.request_date DESC, lr.leave_id DESC";
        try {
            return jdbcTemplate.query(sql, new LeaveRequestRowMapper(), studentId);
        } catch (Exception e) {
            System.err.println("根据学生ID获取请假申请失败 (studentId: " + studentId + "): " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    /**
     * 向数据库中添加一个新的请假申请信息。
     *
     * @param leaveRequest 包含新请假申请信息的 LeaveRequest 对象。
     *                     其中 leaveId, requestDate, approvalDate, approvedByAdminId 通常由数据库或后续操作设置。
     * @return 如果插入成功，返回新生成的请假记录的 leaveId；如果插入失败，返回 -1 或抛出异常。
     */
    public int addLeaveRequest(LeaveRequest leaveRequest) {
        // SQL 插入语句，request_date 使用数据库默认的 CURRENT_TIMESTAMP
        String sql = "INSERT INTO LeaveRequest (student_id, reason, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";

        // KeyHolder 用于获取数据库生成的自增主键 (leave_id)
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            // 执行更新操作，并指定返回生成的键
            int rowsAffected = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, leaveRequest.getStudentId());
                ps.setString(2, leaveRequest.getReason());
                // 将 java.util.Date 转换为 java.sql.Date
                ps.setDate(3, new java.sql.Date(leaveRequest.getStartDate().getTime()));
                ps.setDate(4, new java.sql.Date(leaveRequest.getEndDate().getTime()));
                ps.setString(5, leaveRequest.getStatus() != null ? leaveRequest.getStatus() : "待审批"); // 确保有默认状态
                return ps;
            }, keyHolder);

            if (rowsAffected > 0 && keyHolder.getKey() != null) {
                return keyHolder.getKey().intValue(); // 返回新生成的 leaveId
            } else {
                System.err.println("添加请假申请失败，没有行被影响或未能获取到生成的键。");
                return -1; // 表示插入失败
            }
        } catch (Exception e) {
            System.err.println("添加请假申请信息失败: " + e.getMessage());
            e.printStackTrace();
            return -1; // 表示插入失败
        }
    }

    /**
     * 根据请假ID获取请假申请的详细信息。
     *
     * @param leaveId 请假记录的ID。
     * @return LeaveRequest 对象，如果未找到则返回 null。
     */
    public LeaveRequest getLeaveRequestById(int leaveId) {
        // SQL 查询语句，与 searchLeaveRequests 中的 SELECT部分类似，但只查询特定ID的记录
        String sql = "SELECT lr.leave_id AS lr_leave_id, lr.student_id AS lr_student_id, lr.reason AS lr_reason, " +
                "lr.start_date AS lr_start_date, lr.end_date AS lr_end_date, lr.status AS lr_status, " +
                "lr.request_date AS lr_request_date, lr.approval_date AS lr_approval_date, " +
                "lr.approved_by_admin_id AS lr_approved_by_admin_id, " +
                "s.name AS student_name, a.username AS admin_username " +
                "FROM LeaveRequest lr " +
                "JOIN Student s ON lr.student_id = s.student_id " +
                "LEFT JOIN Admin a ON lr.approved_by_admin_id = a.admin_id " +
                "WHERE lr.leave_id = ?";
        try {
            // queryForObject 期望返回单条记录，如果找不到会抛出 EmptyResultDataAccessException
            return jdbcTemplate.queryForObject(sql, new LeaveRequestRowMapper(), leaveId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // 如果没有找到对应ID的记录，返回null是合理的行为
            return null;
        } catch (Exception e) {
            System.err.println("根据ID获取请假申请信息失败 (leaveId: " + leaveId + "): " + e.getMessage());
            e.printStackTrace();
            return null; // 发生其他错误时也返回null
        }
    }

    /**
     * 更新现有请假申请的状态和审批信息 (通常由管理员操作)。
     *
     * @param leaveId 要更新的请假记录的ID。
     * @param newStatus 新的请假状态 (例如："已批准", "已驳回")。
     * @param adminId 执行审批操作的管理员ID。
     * @return 如果更新成功，返回影响的行数 (通常为 1)；如果更新失败，返回 0。
     */
    public int updateLeaveRequestStatus(int leaveId, String newStatus, Integer adminId) {
        // SQL 更新语句，同时更新状态、审批管理员ID和审批时间
        String sql = "UPDATE LeaveRequest SET status = ?, approved_by_admin_id = ?, approval_date = CURRENT_TIMESTAMP WHERE leave_id = ?";
        try {
            return jdbcTemplate.update(sql, newStatus, adminId, leaveId);
        } catch (Exception e) {
            System.err.println("更新请假申请状态失败 (leaveId: " + leaveId + ", status: " + newStatus + "): " + e.getMessage());
            e.printStackTrace();
            return 0; // 表示更新失败
        }
    }

    /**
     * 学生更新自己的请假申请 (只能在“待审批”状态下，且不能修改审批相关字段)。
     *
     * @param leaveRequest 包含更新后信息的 LeaveRequest 对象 (leaveId 必须有效)。
     * @return 如果更新成功，返回影响的行数；否则返回0。
     */
    public int updateLeaveRequestByStudent(LeaveRequest leaveRequest) {
        // SQL 更新语句，只允许学生修改原因、开始日期、结束日期，且状态必须是“待审批”
        // 注意：这里不更新 request_date, status, approval_date, approved_by_admin_id
        String sql = "UPDATE LeaveRequest SET reason = ?, start_date = ?, end_date = ? " +
                "WHERE leave_id = ? AND status = '待审批'"; // 关键：只允许在待审批时修改
        try {
            return jdbcTemplate.update(sql,
                    leaveRequest.getReason(),
                    new java.sql.Date(leaveRequest.getStartDate().getTime()),
                    new java.sql.Date(leaveRequest.getEndDate().getTime()),
                    leaveRequest.getLeaveId());
        } catch (Exception e) {
            System.err.println("学生更新请假申请失败 (leaveId: " + leaveRequest.getLeaveId() + "): " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 根据请假ID删除请假申请记录。
     *
     * @param leaveId 要删除的请假记录的ID。
     * @return 如果删除成功，返回影响的行数 (通常为 1)；如果删除失败，返回 0。
     */
    public int deleteLeaveRequest(int leaveId) {
        String sql = "DELETE FROM LeaveRequest WHERE leave_id = ?";
        try {
            return jdbcTemplate.update(sql, leaveId);
        } catch (Exception e) {
            System.err.println("删除请假申请信息失败 (leaveId: " + leaveId + "): " + e.getMessage());
            e.printStackTrace();
            return 0; // 表示删除失败
        }
    }
}