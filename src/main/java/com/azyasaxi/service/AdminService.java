package com.azyasaxi.service;

import com.azyasaxi.dao.AdminDao; // 导入 AdminDao 类
import com.azyasaxi.model.Admin;   // 导入 Admin 模型类
import org.springframework.beans.factory.annotation.Autowired; // 用于依赖注入
import org.springframework.stereotype.Service; // 声明这是一个 Service 组件

/**
 * AdminService 类 (服务层)
 * 负责处理与管理员相关的业务逻辑，例如管理员认证。
 * 它会调用 AdminDao 来与数据库进行交互。
 */
@Service // 将此类标记为 Spring 管理的 Service 组件
public class AdminService {

    private final AdminDao adminDao; // AdminDao 实例，通过构造函数注入

    /**
     * 构造函数，通过 Spring 依赖注入 AdminDao。
     *
     * @param adminDao 由 Spring 容器提供的 AdminDao 实例。
     */
    @Autowired // 自动注入 AdminDao bean
    public AdminService(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    /**
     * 认证管理员。
     * 根据提供的用户名和密码验证管理员身份。
     *
     * @param username 用户输入的管理员用户名。
     * @param password 用户输入的管理员密码。
     * @return 如果认证成功，则返回对应的 Admin 对象；否则返回 null。
     */
    public Admin authenticateAdmin(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return null; // 用户名或密码为空，认证失败
        }

        Admin adminFromDb = adminDao.getAdminByUsername(username); // 从 DAO 获取管理员信息

        if (adminFromDb != null && adminFromDb.getPassword().equals(password)) {
            // 用户名存在且密码匹配
            return adminFromDb; // 认证成功，返回 Admin 对象
        }

        return null; // 用户名不存在或密码不匹配，认证失败
    }

    // 未来可以根据需求添加其他管理员相关的业务逻辑方法，例如：
    // public boolean changeAdminPassword(String username, String oldPassword, String newPassword) { ... }
    // public Admin getAdminDetails(int adminId) { ... }
}