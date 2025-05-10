package com.azyasaxi.dao;

import com.azyasaxi.model.Admin;
import org.springframework.beans.factory.annotation.Autowired; // 用于依赖注入
import org.springframework.dao.EmptyResultDataAccessException; // 用于处理查询结果为空的情况
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper; // 用于将 ResultSet 行映射到对象
import org.springframework.stereotype.Repository; // 声明这是一个 DAO 组件

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * AdminDAO 类 (Data Access Object)
 * 负责与数据库中的 Admin 表进行交互，使用 Spring JdbcTemplate 执行管理员数据的查询操作。
 */
@Repository // 将此类标记为 Spring 管理的 DAO 组件
public class AdminDao {

    private final JdbcTemplate jdbcTemplate; // Spring JdbcTemplate 实例

    /**
     * 构造函数，通过 Spring 依赖注入 JdbcTemplate。
     *
     * @param jdbcTemplate 由 Spring 容器提供的 JdbcTemplate 实例。
     */
    @Autowired // 自动注入 JdbcTemplate bean
    public AdminDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据用户名从数据库中检索管理员信息。
     *
     * @param username 要查询的管理员用户名。
     * @return 如果找到匹配的管理员，则返回一个 Admin 对象；否则返回 null。
     */
    public Admin getAdminByUsername(String username) {
        String sql = "SELECT admin_id, username, password FROM Admin WHERE username = ?"; // SQL 查询语句

        try {
            // 使用 queryForObject，如果找不到记录会抛出 EmptyResultDataAccessException
            // 我们也可以使用 query 方法，它返回一个 List，然后检查 List 是否为空
            return jdbcTemplate.queryForObject(sql, new Object[]{username}, new AdminRowMapper());
        } catch (EmptyResultDataAccessException e) {
            // 如果没有找到对应的管理员，queryForObject 会抛出此异常
            return null; // 返回 null 表示未找到
        } catch (Exception e) {
            // 处理其他可能的数据库访问异常
            // 在实际应用中，这里应该有更完善的日志记录和异常处理
            e.printStackTrace(); // 简单打印堆栈信息
            return null;
        }
    }

    /**
     * RowMapper 实现，用于将 ResultSet 中的一行数据映射到一个 Admin 对象。
     */
    private static class AdminRowMapper implements RowMapper<Admin> {
        @Override
        public Admin mapRow(ResultSet rs, int rowNum) throws SQLException {
            Admin admin = new Admin();
            admin.setId(rs.getInt("admin_id"));
            admin.setUsername(rs.getString("username"));
            admin.setPassword(rs.getString("password"));
            return admin;
        }
    }

}