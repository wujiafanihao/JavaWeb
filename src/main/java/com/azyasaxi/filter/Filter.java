package com.azyasaxi.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*") // 使用 @WebFilter 注解，将这个过滤器应用到所有的请求路径 ("/*")
public class Filter implements jakarta.servlet.Filter { // 实现 jakarta.servlet.Filter 接口

    /**
     * Filter 初始化方法
     * 当 Web 容器启动并实例化这个 Filter 时调用。
     * @param filterConfig Filter 的配置对象，可以用来获取初始化参数等。
     * @throws ServletException 如果初始化过程中发生错误。
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 这里可以放置一些初始化代码，例如加载配置文件、初始化资源等。
        // 当前示例中为空，表示不需要特殊的初始化操作。
        jakarta.servlet.Filter.super.init(filterConfig); // 调用父类的init方法
    }

    /**
     * 核心的过滤逻辑方法
     * 每当一个请求与 Filter 的 URL 模式匹配时，Web 容器就会调用这个方法。
     * @param request  ServletRequest 对象，代表客户端的请求。
     * @param response ServletResponse 对象，代表服务器的响应。
     * @param chain    FilterChain 对象，用于将请求传递给下一个过滤器或目标资源（如 Servlet、JSP）。
     * @throws IOException      如果发生 I/O 错误。
     * @throws ServletException 如果发生 Servlet 相关的错误。
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // 将 ServletRequest 和 ServletResponse 转换为 HTTP 特定类型的对象，以便使用 HTTP 相关的方法。
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 获取当前会话 (HttpSession)。
        // 参数 false 表示如果当前请求没有关联的会话，则不创建新的会话，返回 null。
        HttpSession session = httpRequest.getSession(false);
        
        // 获取当前请求的路径，不包括应用的上下文路径 (ContextPath)。
        // 例如，如果完整 URL 是 http://localhost:8080/StudentSystem/login，
        // getContextPath() 返回 "/StudentSystem"，
        // getRequestURI() 返回 "/StudentSystem/login"，
        // substring() 之后，path 会是 "/login"。
        // 如果访问的是根路径 http://localhost:8080/StudentSystem/ ，则 path 会是 "" (空字符串)
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // 如果路径为空字符串，代表访问的是应用的根路径，将其规范化为 "/"
        if (path.isEmpty()) {
            path = "/";
        }
        
        // 定义公共可访问路径，包括首页、登录处理Servlet、登录页面JSP和静态资源
        // loginPage.jsp 是实际的登录表单页面，/login 是处理登录的Servlet
        // 确保 loginPage.jsp 也被正确地包含在公共访问路径中
        if (path.equals("/") || path.startsWith("/index.jsp") || path.startsWith("/login") || path.startsWith("/loginPage.jsp") || path.startsWith("/assets/")) {
            chain.doFilter(request, response); // 放行公共路径
            return; // 结束当前过滤器的处理
        }

        // 检查用户是否已登录，通过会话中是否存在 "username" 和 "userType" 属性来判断
        String username = null;
        String userType = null;
        if (session != null) {
            username = (String) session.getAttribute("username");
            userType = (String) session.getAttribute("userType");
        }

        if (username == null || userType == null) {
            // 用户未登录或会话信息不完整，重定向到登录页面
            // 使用 loginPage.jsp 作为登录页面的目标
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/loginPage.jsp");
            return; // 结束当前过滤器的处理
        }

        // 根据用户类型进行权限控制
        // 这里定义了管理员和学生可以访问的特定页面
        // 其他未明确指定的受保护路径，如果用户已登录，则暂时允许访问
        // 后续可以根据 structure.txt 或更详细的需求来细化这些规则
        if ("admin".equals(userType)) {
            // 如果用户是管理员
            // 管理员可以访问 /admin/dashboard (用于加载数据的Servlet) 和 /adminView.jsp (实际的视图)
            if (path.startsWith("/admin/dashboard") || path.startsWith("/adminView.jsp") /* || path.startsWith("/someOtherAdminPath") */) {
                System.out.println("[Filter] Admin access GRANTED for: " + path); // 添加调试打印
                chain.doFilter(request, response); // 允许访问
            } else if (path.startsWith("/studentView.jsp")) {
                // 如果管理员尝试访问学生界面，重定向到管理员主界面
                System.out.println("[Filter] Admin attempting to access studentView, redirecting to adminView for path: " + path); // 添加调试打印
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/adminView.jsp");
            } else {
                // 对于其他未明确指定的受保护路径，如果管理员已登录，也放行
                // 这可以根据具体需求收紧，例如只允许访问特定前缀的路径
                System.out.println("[Filter] Admin access GRANTED for other path: " + path); // 添加调试打印
                chain.doFilter(request, response);
            }
        } else if ("student".equals(userType)) {
            // 如果用户是学生
            // 学生可以访问 studentView.jsp 和其他可能的学生特定路径
            if (path.startsWith("/studentView.jsp") /* || path.startsWith("/someOtherStudentPath") */) {
                chain.doFilter(request, response); // 允许访问
            } else if (path.startsWith("/adminView.jsp")) {
                // 如果学生尝试访问管理员界面，重定向到学生主界面
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/studentView.jsp");
            } else {
                // 对于其他路径，如果学生已登录，暂时允许访问
                chain.doFilter(request, response);
            }
        } else {
            // 如果用户类型未知或不匹配任何已知类型，则视为未授权，重定向到登录页面
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/loginPage.jsp");
        }
    }

    /**
     * Filter 销毁方法
     * 当 Web 容器移除这个 Filter 实例时调用，例如 Web 应用关闭时。
     * 用于释放 Filter 在 init 方法中获取的资源。
     */
    @Override
    public void destroy() {
        // 这里可以放置一些清理代码，例如关闭数据库连接、释放文件句柄等。
        // 当前示例中为空，表示不需要特殊的销毁操作。
        jakarta.servlet.Filter.super.destroy(); // 调用父类的destroy方法
    }
}
