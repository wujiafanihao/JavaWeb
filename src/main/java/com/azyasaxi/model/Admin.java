package com.azyasaxi.model;

/**
 * 管理员信息实体类 (Admin)
 * 对应数据库中的 Admin 表，存储管理员的登录认证信息。
 */
public class Admin {

    private int id; // 管理员的唯一标识符，主键 (对应数据库中的 admin_id)
    private String username; // 管理员登录用户名，唯一且不能为空
    private String password; // 管理员登录密码，不能为空

    // 默认构造函数
    public Admin() {
    }

    // 包含所有字段的构造函数
    public Admin(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                // 注意：通常不在 toString 中直接打印密码，这里为了演示完整性包含
                // 在实际应用中，出于安全考虑，可能需要省略或脱敏密码字段
                ", password='[PROTECTED]'" +
                '}';
    }
}
