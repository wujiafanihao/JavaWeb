package com.azyasaxi.controller.saveData;

import com.azyasaxi.model.ClassInfo;
import com.azyasaxi.service.AdminLogService; // 新增：导入 AdminLogService
import com.azyasaxi.service.ClassInfoService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // 新增：导入 HttpSession
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
    private AdminLogService adminLogService; // 新增：AdminLogService 实例

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.classInfoService = context.getBean(ClassInfoService.class);
            this.adminLogService = context.getBean(AdminLogService.class); // 新增：获取 AdminLogService
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

        if (this.classInfoService == null || this.adminLogService == null) { // 新增：检查 adminLogService
            request.getSession().setAttribute("errorMessage", "系统服务错误，请稍后重试。");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard#student"); // 假设班级管理在学生模块下
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
            request.getSession().setAttribute("successMessage", "班级 '" + className.trim() + "' 添加成功！");
            // 记录新增日志
            HttpSession session = request.getSession(false);
            Integer adminId = (session != null) ? (Integer) session.getAttribute("adminId") : null;
            String adminUsername = (session != null) ? (String) session.getAttribute("username") : "未知管理员";
            adminLogService.recordAdminAction(adminId, adminUsername,
                    "新增班级", "班级", className.trim(), // 使用班级名作为临时ID
                    "添加了新班级: " + className.trim());
        } else {
            request.getSession().setAttribute("errorMessage", "添加班级 '" + className.trim() + "' 失败。可能班级已存在于该专业下或发生其他错误。");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard#student");
    }
}