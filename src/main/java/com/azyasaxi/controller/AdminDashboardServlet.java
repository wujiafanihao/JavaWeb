package com.azyasaxi.controller;

import com.azyasaxi.model.ClassInfo; // 导入 ClassInfo 模型类
import com.azyasaxi.model.Student;
import com.azyasaxi.service.ClassInfoService; // 导入 ClassInfoService 类
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
import java.util.Collections; // 用于返回空列表，避免null
import java.util.List;

/**
 * AdminDashboardServlet
 * 负责处理管理员仪表盘相关请求，例如加载学生列表、班级列表并将其转发给 JSP 页面进行显示。
 */
@WebServlet("/admin/dashboard") // 定义 Servlet 的访问路径
public class AdminDashboardServlet extends HttpServlet {

    private StudentService studentService; // StudentService 实例
    private ClassInfoService classInfoService; // ClassInfoService 实例

    /**
     * Servlet 初始化方法。
     * 从 Spring WebApplicationContext 中获取 StudentService 和 ClassInfoService beans。
     * @param config Servlet 配置对象。
     * @throws ServletException 如果初始化失败。
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            try {
                this.studentService = context.getBean(StudentService.class);
                this.classInfoService = context.getBean(ClassInfoService.class); // 获取 ClassInfoService bean
            } catch (Exception e) {
                // 如果获取bean失败，也应该抛出异常或记录严重错误
                throw new ServletException("获取 Spring beans 失败 (StudentService or ClassInfoService)", e);
            }
        } else {
            throw new ServletException("Spring WebApplicationContext not found for AdminDashboardServlet. Ensure ContextLoaderListener is configured in web.xml.");
        }
    }

    /**
     * 处理 GET 请求。
     * 获取学生列表和班级列表，将它们设置到请求属性中，并转发到 adminView.jsp 页面。
     * @param request  HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生 Servlet 相关错误。
     * @throws IOException      如果发生 I/O 错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 检查 Service 是否已正确初始化
        if (this.studentService == null || this.classInfoService == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "所需服务未正确初始化，请检查服务器日志。");
            return;
        }

        String errorMessage = null;
        List<Student> studentList = Collections.emptyList(); // 初始化为空列表，避免后续JSTL检查null
        List<ClassInfo> classList = Collections.emptyList(); // 初始化为空列表

        try {
            // 获取班级列表的搜索词
            String searchClassTerm = request.getParameter("searchClassTerm");
            System.out.println("AdminDashboardServlet: Received searchClassTerm: " + searchClassTerm);

            // 获取学生列表的搜索词
            String searchStudentTerm = request.getParameter("searchStudentTerm");
            System.out.println("AdminDashboardServlet: Received searchStudentTerm: " + searchStudentTerm);

            System.out.println("AdminDashboardServlet: Attempting to fetch student list with search term...");
            // 调用 StudentService 的搜索方法，如果 searchTerm 为 null 或空，它会返回所有学生
            studentList = studentService.searchStudents(searchStudentTerm);
            System.out.println("AdminDashboardServlet: Student list fetched, size: " + (studentList != null ? studentList.size() : "null"));
            // 将学生搜索词也传递回 JSP
            if (searchStudentTerm != null) {
                request.setAttribute("searchStudentTerm", searchStudentTerm);
            }

            System.out.println("AdminDashboardServlet: Attempting to fetch class list with search term...");
            // 调用 ClassInfoService 的搜索方法，如果 searchTerm 为 null 或空，它会返回所有班级
            classList = classInfoService.searchClassInfos(searchClassTerm);
            System.out.println("AdminDashboardServlet: Class list fetched, size: " + (classList != null ? classList.size() : "null"));
            // 将搜索词也传递回 JSP，以便在搜索框中显示用户输入的值
            if (searchClassTerm != null) {
                request.setAttribute("searchClassTerm", searchClassTerm);
            }

        } catch (Exception e) {
            // 记录异常信息
            System.err.println("AdminDashboardServlet: 加载学生或班级列表时出错: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "加载数据时发生内部错误，请稍后再试。";
        }

        request.setAttribute("studentList", studentList); // 将学生列表设置到请求属性中
        request.setAttribute("classList", classList);     // 将班级列表设置到请求属性中
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }

        // 转发到 adminView.jsp 页面
        System.out.println("AdminDashboardServlet: Forwarding to adminView.jsp");
        request.getRequestDispatcher("/adminView.jsp").forward(request, response);
    }
}