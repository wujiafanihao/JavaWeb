package com.azyasaxi.controller;

import com.azyasaxi.model.Admin;   // 导入 Admin 模型类
import com.azyasaxi.service.AdminService; // 导入 AdminService 类
import jakarta.servlet.ServletConfig; // 导入 ServletConfig
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext; // 导入 WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils; // 导入 WebApplicationContextUtils

import java.io.IOException;

@WebServlet("/login") // Servlet 映射路径
public class LoginServlet extends HttpServlet {
    // 定义临时学生用户的用户名和密码常量
    private static final String TEMP_STUDENT_USERNAME = "user";
    private static final String TEMP_STUDENT_PASSWORD = "user123";

    private AdminService adminService; // AdminService 实例，用于处理业务逻辑

    /**
     * Servlet 初始化方法。
     * 在 Servlet 第一次被加载时调用。
     * 此方法从 Spring 的 WebApplicationContext 中获取 AdminService bean。
     * 注意：这要求 Spring 的 ContextLoaderListener 已在 web.xml 中配置。
     * @param config Servlet 配置对象，用于获取 ServletContext。
     * @throws ServletException 如果初始化过程中发生错误。
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // 调用父类的 init 方法
        // 从 ServletContext 获取 Spring WebApplicationContext
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            // 从 Spring 上下文中获取 AdminService bean
            this.adminService = context.getBean(AdminService.class);
        } else {
            // 如果 Spring 上下文未初始化 (例如，web.xml 中缺少 ContextLoaderListener)，则抛出异常
            throw new ServletException("Spring WebApplicationContext not found. Please ensure ContextLoaderListener is configured in web.xml.");
        }
    }

    /**
     * 处理 POST 请求，用于用户登录认证。
     * 根据用户类型（管理员或学生）重定向到相应的页面。
     * 管理员登录将通过 AdminService 进行认证。
     * @param request HttpServletRequest对象，包含用户提交的登录信息。
     * @param response HttpServletResponse对象，用于向客户端发送响应。
     * @throws ServletException 如果处理请求时发生 Servlet 相关错误。
     * @throws IOException 如果发生 I/O 错误。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 从请求中获取用户名和密码参数
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 尝试通过 AdminService 认证管理员
        Admin authenticatedAdmin = null;
        if (this.adminService == null) {
             // 这种情况理论上不应该发生，因为 init 方法中会检查
            request.setAttribute("error", "系统服务初始化失败，请联系管理员。");
            request.getRequestDispatcher("/loginPage.jsp").forward(request, response);
            return;
        }
        authenticatedAdmin = adminService.authenticateAdmin(username, password);


        if (authenticatedAdmin != null) {
            // 管理员登录成功
            // 将管理员信息存入会话 (session)
            request.getSession().setAttribute("userType", "admin"); // 存储用户类型
            request.getSession().setAttribute("username", authenticatedAdmin.getUsername()); // 存储管理员用户名
            // 重定向到 AdminDashboardServlet，由它来加载数据并转发到 adminView.jsp
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else if (TEMP_STUDENT_USERNAME.equals(username) && TEMP_STUDENT_PASSWORD.equals(password)) {
            // 临时学生用户登录成功
            // 将学生信息存入会话 (session)
            request.getSession().setAttribute("userType", "student"); // 存储用户类型
            request.getSession().setAttribute("username", username); // 存储临时学生用户名
            // 重定向到学生界面
            response.sendRedirect(request.getContextPath() + "/studentView.jsp");
        }
        // 未来可以扩展以支持数据库中的学生用户登录
        // else if (studentService.authenticateStudent(username, password) != null) { // 需要创建 StudentService 和相应方法
        //     request.getSession().setAttribute("userType", "student");
        //     request.getSession().setAttribute("username", username); // 或者从认证后的学生对象获取更详细信息
        //     response.sendRedirect(request.getContextPath() + "/studentView.jsp");
        // }
        else {
            // 登录失败 (管理员验证失败且不是临时学生账户)
            // 在请求中设置错误消息
            request.setAttribute("error", "用户名或密码错误，请重新输入！");
            // 将请求转发回登录页面，并显示错误消息
            request.getRequestDispatcher("/loginPage.jsp").forward(request, response);
        }
    }
}
