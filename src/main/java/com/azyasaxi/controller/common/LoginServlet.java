package com.azyasaxi.controller.common;

import com.azyasaxi.model.Admin;   // 导入 Admin 模型类
import com.azyasaxi.model.Student; // 新增：导入 Student 模型类
import com.azyasaxi.service.AdminService; // 导入 AdminService 类
import com.azyasaxi.service.StudentService; // 新增：导入 StudentService 类
import jakarta.servlet.ServletConfig; // 导入 ServletConfig
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie; // 导入 Cookie 类
import jakarta.servlet.http.HttpSession; // 导入 HttpSession 类
import org.springframework.web.context.WebApplicationContext; // 导入 WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils; // 导入 WebApplicationContextUtils

import java.io.IOException;

@WebServlet("/login") // Servlet 映射路径
public class LoginServlet extends HttpServlet {
    // 定义临时学生用户的用户名和密码常量
    private static final String TEMP_STUDENT_USERNAME = "user";
    private static final String TEMP_STUDENT_PASSWORD = "user123";

    private AdminService adminService; // AdminService 实例
    private StudentService studentService; // 新增：StudentService 实例

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
            this.studentService = context.getBean(StudentService.class); // 新增：获取 StudentService bean
        } else {
            System.err.println("Spring WebApplicationContext 未找到。请确保 ContextLoaderListener 已在 web.xml 中配置。");
            // adminService 和 studentService 将为 null
        }
    }

    /**
     * 处理 POST 请求，用于用户登录认证。
     * 根据用户类型（管理员或学生）重定向到相应的页面。
     * 管理员登录将通过 AdminService 进行认证。
     * 成功登录后会创建 Session 和 Cookie。
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

        // 获取当前会话，如果不存在则创建一个新的会话
        HttpSession session = request.getSession();

        // 尝试通过 AdminService 认证管理员
        Admin authenticatedAdmin = null;
        if (this.adminService != null) {
            authenticatedAdmin = adminService.authenticateAdmin(username, password);
        } else {
            System.err.println("LoginServlet: AdminService 未初始化。管理员登录认证跳过。");
        }

        if (authenticatedAdmin != null) {
            // 管理员登录成功
            session.setAttribute("userType", "admin");
            session.setAttribute("username", authenticatedAdmin.getUsername());
            session.setAttribute("adminId", authenticatedAdmin.getId());

            Cookie usernameCookie = new Cookie("usernameCookie", authenticatedAdmin.getUsername());
            usernameCookie.setMaxAge(60 * 60 * 24 * 7);
            usernameCookie.setPath(request.getContextPath() + "/");
            response.addCookie(usernameCookie);

            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            // 管理员认证失败，尝试学生认证 (数据库用户)
            Student authenticatedStudent = null;
            if (this.studentService != null) {
                authenticatedStudent = studentService.authenticateStudent(username, password);
            } else {
                System.err.println("LoginServlet: StudentService 未初始化。数据库学生登录认证跳过。");
            }

            if (authenticatedStudent != null) {
                // 数据库学生登录成功
                session.setAttribute("userType", "student");
                session.setAttribute("username", authenticatedStudent.getUserName());
                session.setAttribute("studentId", authenticatedStudent.getStudentId()); // 存储学生ID

                Cookie usernameCookie = new Cookie("usernameCookie", authenticatedStudent.getUserName());
                usernameCookie.setMaxAge(60 * 60 * 24 * 7);
                usernameCookie.setPath(request.getContextPath() + "/");
                response.addCookie(usernameCookie);
                // 重定向到学生仪表盘 Servlet (后续创建)
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
            } else if (TEMP_STUDENT_USERNAME.equals(username) && TEMP_STUDENT_PASSWORD.equals(password)) {
                // 临时学生用户登录成功
                session.setAttribute("userType", "student");
                session.setAttribute("username", username);
                // 对于临时用户，可能没有 studentId，或者可以设置一个特殊值
                // session.setAttribute("studentId", -1);

                Cookie usernameCookie = new Cookie("usernameCookie", username);
                usernameCookie.setMaxAge(60 * 60 * 24 * 7);
                usernameCookie.setPath(request.getContextPath() + "/");
                response.addCookie(usernameCookie);
                // 临时学生仍然重定向到旧的 studentView.jsp，或统一到 /student/dashboard
                response.sendRedirect(request.getContextPath() + "/studentView.jsp");
            } else {
                // 所有认证均失败
                request.setAttribute("error", "用户名或密码错误，请重新输入！");
                request.getRequestDispatcher("/loginPage.jsp").forward(request, response);
            }
        }
    }
}