package com.azyasaxi.controller.student;

import com.azyasaxi.model.Course;
import com.azyasaxi.model.LeaveRequest;
import com.azyasaxi.model.Student;
import com.azyasaxi.service.CourseService;
import com.azyasaxi.service.EnrollmentService;
import com.azyasaxi.service.LeaveRequestService;
import com.azyasaxi.service.StudentService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private StudentService studentService;
    private LeaveRequestService leaveRequestService;
    private CourseService courseService;
    // EnrollmentService might be needed by CourseService to determine available courses
    // or directly here if CourseService doesn't handle that logic.

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
            this.leaveRequestService = context.getBean(LeaveRequestService.class);
            this.courseService = context.getBean(CourseService.class);
            // this.enrollmentService = context.getBean(EnrollmentService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for StudentDashboardServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || !"student".equals(session.getAttribute("userType")) || session.getAttribute("studentId") == null) {
            // 用户未登录或非学生用户或学生ID丢失
            response.sendRedirect(request.getContextPath() + "/loginPage.jsp");
            return;
        }

        Integer studentId = (Integer) session.getAttribute("studentId");
        if (studentId == null) { // 再次检查以防万一
             response.sendRedirect(request.getContextPath() + "/loginPage.jsp");
            return;
        }

        try {
            // 1. 获取学生完整信息 (包括已选课程、成绩、总学分)
            Student studentDetails = studentService.getStudentFullDetailsById(studentId);
            if (studentDetails == null) {
                session.invalidate(); // 学生数据异常，使其重新登录
                request.setAttribute("error", "无法加载您的信息，请重新登录。");
                request.getRequestDispatcher("/loginPage.jsp").forward(request, response);
                return;
            }
            request.setAttribute("studentDetails", studentDetails);

            // 2. 获取学生的历史请假记录
            List<LeaveRequest> leaveRequestList = leaveRequestService.getLeaveRequestsByStudentId(studentId);
            request.setAttribute("leaveRequestList", leaveRequestList != null ? leaveRequestList : Collections.emptyList());

            // 3. 获取学生可选的课程列表
            //    This method needs to be implemented in CourseService/CourseDao
            //    It should list courses the student is eligible for and hasn't enrolled in yet.
            List<Course> availableCoursesList = courseService.getAvailableCoursesForStudent(studentId);
            request.setAttribute("availableCoursesList", availableCoursesList != null ? availableCoursesList : Collections.emptyList());
            
            // Flash messages for leave/course enrollment
            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }


        } catch (Exception e) {
            System.err.println("StudentDashboardServlet: Error loading data for student ID " + studentId + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("pageError", "加载页面数据时发生错误，请稍后再试。");
        }

        request.getRequestDispatcher("/studentView.jsp").forward(request, response);
    }
}