package com.azyasaxi.controller;

import com.azyasaxi.model.ClassInfo;
import com.azyasaxi.service.ClassInfoService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

/**
 * SaveClassServlet
 * 负责处理添加新班级的表单提交。
 */
@WebServlet("/admin/saveClass")
public class SaveClassServlet extends HttpServlet {

    private ClassInfoService classInfoService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.classInfoService = context.getBean(ClassInfoService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for SaveClassServlet.");
        }
    }

    /**
     * 处理 POST 请求，用于保存新的班级信息。
     *
     * @param request  HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生 Servlet 相关错误。
     * @throws IOException      如果发生 I/O 错误。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (this.classInfoService == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ClassInfoService 未正确初始化。");
            return;
        }

        request.setCharacterEncoding("UTF-8"); //确保正确处理中文字符

        String className = request.getParameter("className");
        String majorIdStr = request.getParameter("majorId");

        // 基本验证
        if (className == null || className.trim().isEmpty()) {
            request.setAttribute("errorMessage", "班级名称不能为空！");
            // 将用户导回表单页面，并显示错误
            // 如果需要，也可以将已填写的 majorId 传回
            request.getRequestDispatcher("/addClassForm.jsp").forward(request, response);
            return;
        }

        ClassInfo newClassInfo = new ClassInfo();
        newClassInfo.setClassName(className.trim());

        if (majorIdStr != null && !majorIdStr.trim().isEmpty()) {
            try {
                newClassInfo.setMajorId(Integer.parseInt(majorIdStr.trim()));
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "专业ID格式不正确！请输入数字。");
                request.setAttribute("classNameValue", className); // 保留用户已输入的班级名
                request.getRequestDispatcher("/addClassForm.jsp").forward(request, response);
                return;
            }
        } else {
            newClassInfo.setMajorId(null); // 如果专业ID为空，则设置为null
        }

        boolean success = classInfoService.addClassInfo(newClassInfo);

        if (success) {
            // 设置成功消息到会话中，以便重定后显示 (Flash Message 模式)
            request.getSession().setAttribute("successMessage", "班级 '" + className + "' 添加成功！");
        } else {
            request.getSession().setAttribute("errorMessage", "添加班级 '" + className + "' 失败，请检查输入或联系管理员。");
        }

        // 重定向到班级列表页面 (通常是管理员仪表盘)
        response.sendRedirect(request.getContextPath() + "/admin/dashboard#student"); // 添加#student以定位到学生管理模块
    }
}