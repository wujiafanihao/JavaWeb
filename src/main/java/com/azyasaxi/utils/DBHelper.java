package com.azyasaxi.utils;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DBHelper {

    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            // 读取 application.properties 文件
            Properties props = new Properties();
            InputStream input = DBHelper.class.getClassLoader().getResourceAsStream("application.properties");
            if (input == null) {
                throw new RuntimeException("无法找到 application.properties 文件");
            }
            props.load(input);
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");

            // 加载 JDBC 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException("初始化数据库连接失败", e);
        }
    }

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    // 执行查询
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            closeResources(conn, stmt, rs);
            throw e;
        }
    }

    // 执行更新（插入、更新、删除）
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            closeResources(conn, stmt);
            throw e;
        }
    }

    // 关闭资源
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        if (rs != null && !rs.isClosed()) {
            rs.close();
        }
        if (stmt != null && !stmt.isClosed()) {
            stmt.close();
        }
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // 重载关闭资源的方法，不带 ResultSet
    public static void closeResources(Connection conn, Statement stmt) throws SQLException {
        closeResources(conn, stmt, null);
    }
}
