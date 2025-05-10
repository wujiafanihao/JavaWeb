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

@WebServlet("/admin/viewStudentCreditDetails")
public class ViewStudentCreditDetailsServlet extends HttpServlet {
    private StudentService studentService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ViewStudentCreditDetailsServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String studentIdStr = request.getParameter("studentId");

        if (studentIdStr != null && !studentIdStr.isEmpty()) {
            try {
                int studentId = Integer.parseInt(studentIdStr);
                Student studentDetails = studentService.getStudentFullDetailsById(studentId);

                if (studentDetails != null) {
                    request.setAttribute("studentDetails", studentDetails);
                } else {
                    request.getSession().setAttribute("errorMessage", "未找到ID为 " + studentId + " 的学生学分详情。");
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=credit#credit");
                    return;
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "无效的学生ID格式。");
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=credit#credit");
                return;
            } catch (Exception e) {
                request.getSession().setAttribute("errorMessage", "获取学生学分详情时发生错误：" + e.getMessage());
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=credit#credit");
                return;
            }
        } else {
            request.getSession().setAttribute("errorMessage", "未提供学生ID以查看学分详情。");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=credit#credit");
            return;
        }
        // 转发到新的JSP页面
        request.getRequestDispatcher("/studentCreditDetails.jsp").forward(request, response);
    }
}