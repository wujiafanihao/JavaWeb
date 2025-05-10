package com.azyasaxi.controller.student;

import com.azyasaxi.service.EnrollmentService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;

@WebServlet("/student/enrollCourse")
public class EnrollCourseServlet extends HttpServlet {
    private EnrollmentService enrollmentService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.enrollmentService = context.getBean(EnrollmentService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for EnrollCourseServlet.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null || !"student".equals(session.getAttribute("userType")) || session.getAttribute("studentId") == null) {
            response.sendRedirect(request.getContextPath() + "/loginPage.jsp");
            return;
        }

        Integer studentId = (Integer) session.getAttribute("studentId");
        String courseIdStr = request.getParameter("courseId");

        if (!StringUtils.hasText(courseIdStr)) {
            session.setAttribute("errorMessage", "选课失败：未指定课程ID。");
            response.sendRedirect(request.getContextPath() + "/student/dashboard#course-selection");
            return;
        }

        int courseId;
        try {
            courseId = Integer.parseInt(courseIdStr);
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "选课失败：课程ID格式无效。");
            response.sendRedirect(request.getContextPath() + "/student/dashboard#course-selection");
            return;
        }

        boolean success = enrollmentService.enrollStudentInCourse(studentId, courseId);

        if (success) {
            session.setAttribute("successMessage", "课程 (ID: " + courseId + ") 选修成功！");
        } else {
            // Service 层会打印更具体的日志，这里给用户一个通用提示
            session.setAttribute("errorMessage", "选修课程 (ID: " + courseId + ") 失败。您可能已选修该课程或发生其他错误。");
        }
        response.sendRedirect(request.getContextPath() + "/student/dashboard#course-selection");
    }
}