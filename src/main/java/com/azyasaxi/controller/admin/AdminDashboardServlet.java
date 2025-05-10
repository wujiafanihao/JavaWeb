package com.azyasaxi.controller.admin;

import com.azyasaxi.model.*; // 导入所有模型类
import com.azyasaxi.service.*; // 导入所有服务类
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * AdminDashboardServlet (控制器)
 * 负责处理管理员仪表盘相关请求，例如加载学生列表、班级列表、课程列表、请假列表和学分统计，
 * 并将这些数据转发给 JSP 页面 (adminView.jsp) 进行显示。
 */
@WebServlet("/admin/dashboard") // 定义 Servlet 的访问路径
public class AdminDashboardServlet extends HttpServlet {

    private StudentService studentService;             // StudentService 实例
    private ClassInfoService classInfoService;         // ClassInfoService 实例
    private CourseService courseService;               // CourseService 实例
    private LeaveRequestService leaveRequestService;     // LeaveRequestService 实例
    private CreditSummaryService creditSummaryService; // CreditSummaryService 实例

    /**
     * Servlet 初始化方法。
     * 从 Spring WebApplicationContext 中获取所有需要的 Service beans。
     * @param config Servlet 配置对象。
     * @throws ServletException 如果初始化过程中发生错误。
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // 调用父类的 init 方法
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            try {
                // 从 Spring 上下文中获取各个 Service bean
                this.studentService = context.getBean(StudentService.class);
                this.classInfoService = context.getBean(ClassInfoService.class);
                this.courseService = context.getBean(CourseService.class);
                this.leaveRequestService = context.getBean(LeaveRequestService.class);
                this.creditSummaryService = context.getBean(CreditSummaryService.class); // 初始化 CreditSummaryService
            } catch (Exception e) {
                // 如果获取任何一个bean失败，都应抛出异常或记录严重错误
                throw new ServletException("获取 Spring beans 失败 (AdminDashboardServlet): " + e.getMessage(), e);
            }
        } else {
            // 如果 Spring 上下文未找到，抛出异常
            throw new ServletException("Spring WebApplicationContext 在 AdminDashboardServlet 中未找到。请确保 ContextLoaderListener 已在 web.xml 中配置。");
        }
    }

    /**
     * 处理 HTTP GET 请求。
     * 获取所有相关模块的数据列表，将它们以及相关的搜索条件设置到请求属性中，
     * 并转发到 adminView.jsp 页面。
     * @param request  HttpServletRequest 对象，包含客户端的请求信息。
     * @param response HttpServletResponse 对象，用于向客户端发送响应。
     * @throws ServletException 如果在处理请求时发生Servlet相关的错误。
     * @throws IOException      如果发生输入或输出相关的错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 检查所有 Service 是否已正确初始化
        if (this.studentService == null || this.classInfoService == null ||
                this.courseService == null || this.leaveRequestService == null ||
                this.creditSummaryService == null) { // 检查 creditSummaryService
            System.err.println("AdminDashboardServlet: 一个或多个核心服务未正确初始化。");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "所需服务未正确初始化，请检查服务器日志。");
            return;
        }

        String errorMessage = null; // 用于存储通用的错误消息
        // 初始化所有列表为空，以避免在JSP中出现null检查
        List<Student> studentList = Collections.emptyList();
        List<ClassInfo> classList = Collections.emptyList();
        List<Course> courseList = Collections.emptyList();
        List<LeaveRequest> leaveRequestList = Collections.emptyList();
        List<CreditSummary> creditSummaryList = Collections.emptyList(); // 初始化学分统计列表

        try {
            // --- 处理学生列表 ---
            String searchStudentTerm = request.getParameter("searchStudentTerm");
            studentList = studentService.searchStudents(searchStudentTerm);
            if (searchStudentTerm != null) {
                request.setAttribute("searchStudentTerm", searchStudentTerm);
            }

            // --- 处理班级列表 ---
            String searchClassTerm = request.getParameter("searchClassTerm");
            classList = classInfoService.searchClassInfos(searchClassTerm);
            if (searchClassTerm != null) {
                request.setAttribute("searchClassTerm", searchClassTerm);
            }

            // --- 处理课程列表 ---
            String searchCourseTerm = request.getParameter("searchCourseTerm");
            courseList = courseService.searchCourses(searchCourseTerm);
            if (searchCourseTerm != null) {
                request.setAttribute("searchCourseTerm", searchCourseTerm);
            }

            // --- 处理请假列表 ---
            String searchLeaveStudentName = request.getParameter("searchLeaveStudentName");
            String searchLeaveStatus = request.getParameter("searchLeaveStatus");
            leaveRequestList = leaveRequestService.listLeaveRequests(searchLeaveStudentName, searchLeaveStatus);
            if (searchLeaveStudentName != null) {
                request.setAttribute("searchLeaveStudentName", searchLeaveStudentName);
            }
            if (searchLeaveStatus != null) {
                request.setAttribute("searchLeaveStatus", searchLeaveStatus);
            }

            // --- 处理学分统计列表 ---
            String searchCreditTerm = request.getParameter("searchCreditTerm"); // 使用通用参数名
            // System.out.println("AdminDashboardServlet: 接收到的 searchCreditTerm: " + searchCreditTerm); // 调试日志
            // 调用 CreditSummaryService 的列表/搜索方法
            creditSummaryList = creditSummaryService.listCreditSummaries(searchCreditTerm); // 传递通用搜索词
            // System.out.println("AdminDashboardServlet: 学分统计列表已获取, 数量: " + (creditSummaryList != null ? creditSummaryList.size() : "null"));
            // 将学分统计的搜索词也传递回 JSP
            if (searchCreditTerm != null) {
                request.setAttribute("searchCreditTerm", searchCreditTerm); // 使用通用参数名
            }

        } catch (Exception e) {
            // 如果在获取任何列表数据时发生异常
            System.err.println("AdminDashboardServlet: 加载仪表盘数据时出错: " + e.getMessage());
            e.printStackTrace(); // 打印完整的异常堆栈到服务器日志
            errorMessage = "加载数据时发生内部错误，请稍后再试或联系管理员。";
        }

        // 将所有获取到的列表数据设置到请求属性中
        request.setAttribute("studentList", studentList);
        request.setAttribute("classList", classList);
        request.setAttribute("courseList", courseList);
        request.setAttribute("leaveRequestList", leaveRequestList);
        request.setAttribute("creditSummaryList", creditSummaryList); // 设置学分统计列表

        // 如果有错误消息，也设置到请求属性中
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }

        // 转发到 adminView.jsp 页面进行渲染
        // System.out.println("AdminDashboardServlet: 正在转发到 adminView.jsp (包含所有模块数据)。"); // 调试日志
        request.getRequestDispatcher("/adminView.jsp").forward(request, response);
    }
}