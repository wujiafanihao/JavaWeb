package com.azyasaxi.service;

import com.azyasaxi.dao.AdminDao; // 导入 AdminDao 类
import com.azyasaxi.model.Admin;   // 导入 Admin 模型类
import com.azyasaxi.utils.CalculateSHA256;
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
     * 认证管理员 - 使用哈希值进行比较。
     * 根据提供的用户名和客户端计算的密码哈希值验证管理员身份。
     *
     * @param username 用户输入的管理员用户名。
     * @param clientProvidedPasswordHash 从客户端提交过来的，经过哈希计算后的密码。
     * @return 如果认证成功，则返回对应的 Admin 对象；否则返回 null。
     */
    public Admin authenticateAdmin(String username, String clientProvidedPasswordHash) {

        // 检查传入的参数
        if (username == null || username.isEmpty() || clientProvidedPasswordHash == null || clientProvidedPasswordHash.isEmpty()) {
             System.err.println("认证失败：用户名或客户端哈希密码为空。");
            return null; // 用户名或客户端哈希为空，认证失败
        }

        System.out.println("服务层：尝试认证用户 '" + username + "，哈希密码：" + clientProvidedPasswordHash );

        // 1. 从数据库获取管理员信息
        Admin adminFromDb = adminDao.getAdminByUsername(username);

        // 2. 比较哈希值
        if (adminFromDb != null) {
             // 假设 adminFromDb.getPassword() 返回的是数据库中存储的、经过同样算法(SHA-256)哈希过的密码
            String storedPasswordHash = CalculateSHA256.calculateSHA256(adminFromDb.getPassword());

            // 核心：比较客户端计算的哈希 和 数据库存储的哈希。
            // 使用 equalsIgnoreCase 更稳妥，以防十六进制出现大小写差异。
            if (clientProvidedPasswordHash.equalsIgnoreCase(storedPasswordHash)) {
                 System.out.println("服务层：用户 '" + username + "' 认证成功。");
                // 用户名存在且密码哈希匹配
                return adminFromDb; // 认证成功，返回 Admin 对象
            } else {
                 System.err.println("服务层：用户 '" + username + "' 认证失败：密码哈希不匹配。");
            }
        } else {
             System.err.println("服务层：用户 '" + username + "' 认证失败：用户不存在。");
        }

        return null; // 用户名不存在或密码哈希不匹配，认证失败
    }

}