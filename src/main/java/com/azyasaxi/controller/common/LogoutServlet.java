package com.azyasaxi.controller.common;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

@WebServlet("/logout") // Servlet 映射路径
public class LogoutServlet extends HttpServlet {

    /**
     * 处理 GET 请求，用于用户登出。
     * 清除用户的会话信息和相关的 Cookie。
     * @param request HttpServletRequest对象。
     * @param response HttpServletResponse对象。
     * @throws ServletException 如果处理请求时发生 Servlet 相关错误。
     * @throws IOException 如果发生 I/O 错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. 清除会话 (Session)
        HttpSession session = request.getSession(false); // 获取当前会话，如果不存在则不创建新的
        if (session != null) {
            session.removeAttribute("userType"); // 移除 userType 属性
            session.removeAttribute("username"); // 移除 username 属性
            session.invalidate(); // 使整个会话失效
            System.out.println("Session 已清除。");
        } else {
            System.out.println("没有活动的 Session 需要清除。");
        }

        // 2. 清除 Cookie (通过设置同名 Cookie 的 MaxAge 为 0)
        // 我们需要清除在登录时设置的 "usernameCookie"
        Cookie usernameCookie = new Cookie("usernameCookie", null); // 值可以设为 null 或空字符串
        usernameCookie.setMaxAge(0); // 设置有效期为0，指示浏览器立即删除该 Cookie
        usernameCookie.setPath(request.getContextPath() + "/"); // Cookie 的路径必须与创建时设置的路径一致
        response.addCookie(usernameCookie);
        System.out.println("usernameCookie 已清除。");

        // 可以添加清除其他特定 Cookie 的逻辑
        // 例如，JSESSIONID 是由容器管理的会话跟踪 Cookie，通常不需要手动清除它，
        // 因为 session.invalidate() 会处理。但如果应用设置了其他自定义 Cookie，可以在这里清除。

        // 3. 重定向到登录页面
        // 使用 loginPage.jsp 作为登录页面的目标
        response.sendRedirect(request.getContextPath() + "/loginPage.jsp?logout=true"); // 添加一个参数以便登录页面可以显示登出成功消息
        System.out.println("用户已登出，重定向到登录页面。");
    }

    /**
     * 处理 POST 请求，通常登出操作使用 GET 更常见，但也可以支持 POST。
     * 这里简单地调用 doGet 方法来处理。
     * @param request HttpServletRequest对象。
     * @param response HttpServletResponse对象。
     * @throws ServletException 如果处理请求时发生 Servlet 相关错误。
     * @throws IOException 如果发生 I/O 错误。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response); // 将 POST 请求委托给 doGet 方法处理
    }
}