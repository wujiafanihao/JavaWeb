package com.azyasaxi.controller.detail;

import com.azyasaxi.model.Student;
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

@WebServlet("/admin/viewStudentDetails")
public class ViewStudentDetailsServlet extends HttpServlet {
    private StudentService studentService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ViewStudentDetailsServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()) {
            activeModule = "student"; // 默认是学生管理模块
        }

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int studentId = Integer.parseInt(idStr);
                // getStudentFullDetailsById 方法应该返回包含所有所需信息的Student对象
                Student studentDetails = studentService.getStudentFullDetailsById(studentId);

                if (studentDetails != null) {
                    request.setAttribute("detailsData", studentDetails);
                    request.setAttribute("viewMode", "studentDetails"); // 指示JSP显示学生详情片段
                } else {
                    request.setAttribute("errorMessage", "未找到ID为 " + studentId + " 的学生详情。");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "无效的学生ID格式。");
            } catch (Exception e) {
                request.setAttribute("errorMessage", "获取学生详情时发生错误：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            request.setAttribute("errorMessage", "未提供学生ID以查看详情。");
        }

        request.setAttribute("activeModule", activeModule);
        request.getRequestDispatcher("/detail.jsp").forward(request, response);
    }
}