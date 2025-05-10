package com.azyasaxi.controller.delete;

import com.azyasaxi.service.StudentService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

@WebServlet("/admin/deleteStudent")
public class DeleteStudentServlet extends HttpServlet {
    private StudentService studentService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for DeleteStudentServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()){
            activeModule = "student"; // 默认激活学生管理模块
        }

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int studentId = Integer.parseInt(idStr);
                boolean success = studentService.deleteStudent(studentId);
                if (success) {
                    request.getSession().setAttribute("successMessage", "学生 (ID: " + studentId + ") 删除成功！");
                } else {
                    request.getSession().setAttribute("errorMessage", "删除学生 (ID: " + studentId + ") 失败。可能学生不存在或操作未成功。");
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "无效的学生ID格式。");
            } catch (Exception e) {
                request.getSession().setAttribute("errorMessage", "删除学生时发生错误：" + e.getMessage());
                e.printStackTrace(); // 记录到服务器日志
            }
        } else {
            request.getSession().setAttribute("errorMessage", "未提供学生ID，无法删除。");
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#student");
    }
}