package com.azyasaxi.controller.saveData;

import com.azyasaxi.service.CourseService; // 导入 CourseService
import com.azyasaxi.service.MajorService;  // 导入 MajorService
import com.azyasaxi.service.AdminLogService; // 新增：导入 AdminLogService
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
import java.math.BigDecimal; // 用于处理学分
import java.util.Collections; // 用于空列表

/**
 * SaveCourseServlet (控制器)
 * 负责处理从 addCourseForm.jsp 提交的新课程信息的保存操作。
 * 它会调用 CourseService 来执行实际的业务逻辑和数据持久化。
 */
@WebServlet("/admin/saveCourse") // Servlet的映射路径
public class SaveCourseServlet extends HttpServlet {

    private CourseService courseService; // CourseService 实例，用于课程相关的业务操作
    private MajorService majorService;   // MajorService 实例
    private AdminLogService adminLogService; // 新增：AdminLogService 实例

    /**
     * Servlet 初始化方法。
     * 在 Servlet 第一次被加载时调用。
     * 此方法从 Spring 的 WebApplicationContext 中获取 CourseService 和 MajorService beans。
     * @param config Servlet 配置对象，用于获取 ServletContext。
     * @throws ServletException 如果初始化过程中发生错误。
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // 调用父类的 init 方法
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            try {
                this.courseService = context.getBean(CourseService.class);
                this.majorService = context.getBean(MajorService.class);
                this.adminLogService = context.getBean(AdminLogService.class); // 新增：获取 AdminLogService
            } catch (Exception e) {
                throw new ServletException("在 SaveCourseServlet 中初始化服务失败: " + e.getMessage(), e);
            }
        } else {
            throw new ServletException("Spring WebApplicationContext 在 SaveCourseServlet 中未找到。");
        }
    }

    /**
     * 处理 HTTP POST 请求，用于添加或更新课程信息。
     * @param request HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生Servlet特定的错误。
     * @throws IOException 如果发生输入或输出相关的错误。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (this.courseService == null || this.majorService == null || this.adminLogService == null) { // 新增：检查 adminLogService
            request.getSession().setAttribute("errorMessage", "系统服务错误，请稍后重试。");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=course#course");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String courseIdStr = request.getParameter("courseId");
        String courseName = request.getParameter("courseName");
        String courseTeacher = request.getParameter("courseTeacher");
        String creditStr = request.getParameter("credit");
        String majorName = request.getParameter("majorName"); // 编辑时，如果为空字符串，表示清除专业关联
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()) {
            activeModule = "course";
        }

        // 基本验证
        if (courseName == null || courseName.trim().isEmpty()) {
            handleError(request, response, "课程名称不能为空！", courseIdStr, activeModule);
            return;
        }
        if (courseTeacher == null || courseTeacher.trim().isEmpty()) {
            handleError(request, response, "授课教师不能为空！", courseIdStr, activeModule);
            return;
        }
        if (creditStr == null || creditStr.trim().isEmpty()) {
            handleError(request, response, "学分不能为空！", courseIdStr, activeModule);
            return;
        }

        BigDecimal credit;
        try {
            credit = new BigDecimal(creditStr.trim());
            // 学分范围验证 (0.5 到 10.0 是示例，根据实际需求调整)
            if (credit.compareTo(new BigDecimal("0.5")) < 0 || credit.compareTo(new BigDecimal("10.0")) > 0) {
                handleError(request, response, "学分必须在0.5到10.0之间！", courseIdStr, activeModule);
                return;
            }
        } catch (NumberFormatException e) {
            handleError(request, response, "学分格式不正确！请输入有效的数字。", courseIdStr, activeModule);
            return;
        }
        
        String processedMajorName = (majorName != null && !majorName.trim().isEmpty()) ? majorName.trim() : null;

        boolean success;
        String actionMessage;

        if (courseIdStr != null && !courseIdStr.trim().isEmpty()) {
            // 更新操作
            try {
                int courseId = Integer.parseInt(courseIdStr.trim());
                success = courseService.updateCourse(courseId, courseName.trim(), courseTeacher.trim(), credit, processedMajorName);
                actionMessage = success ? "课程 (ID: " + courseId + ") 更新成功！" : "课程 (ID: " + courseId + ") 更新失败。";
                if (success) {
                    HttpSession session = request.getSession(false);
                    Integer adminId = (session != null) ? (Integer) session.getAttribute("adminId") : null;
                    String adminUsername = (session != null) ? (String) session.getAttribute("username") : "未知管理员";
                    adminLogService.recordAdminAction(adminId, adminUsername,
                            "修改课程信息", "课程", String.valueOf(courseId),
                            "修改了课程 " + courseName.trim() + " (ID: " + courseId + ") 的信息。");
                }
            } catch (NumberFormatException e) {
                success = false;
                actionMessage = "无效的课程ID，更新失败。";
            }
        } else {
            // 添加操作
            success = courseService.addCourse(courseName.trim(), courseTeacher.trim(), credit, processedMajorName);
            if (success) {
                actionMessage = "课程 '" + courseName.trim() + "' 添加成功！";
                HttpSession session = request.getSession(false);
                Integer adminId = (session != null) ? (Integer) session.getAttribute("adminId") : null;
                String adminUsername = (session != null) ? (String) session.getAttribute("username") : "未知管理员";
                adminLogService.recordAdminAction(adminId, adminUsername,
                        "新增课程", "课程", courseName.trim(), // 使用课程名作为临时ID
                        "添加了新课程: " + courseName.trim());
            } else {
                actionMessage = "添加课程 '" + courseName.trim() + "' 失败。可能课程已存在或专业名无效。";
                request.setAttribute("errorMessage", actionMessage);
                forwardToFormWithError(request, response);
                return;
            }
        }

        if (success) {
            request.getSession().setAttribute("successMessage", actionMessage);
        } else {
            request.getSession().setAttribute("errorMessage", actionMessage);
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
    }

    /**
     * 辅助方法：处理错误并决定是转发回添加表单还是重定向。
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response, String errorMessage, String courseIdStr, String activeModule)
            throws ServletException, IOException {
        if (courseIdStr != null && !courseIdStr.trim().isEmpty()) {
            // 如果是更新操作中的错误，直接重定向并显示消息
            request.getSession().setAttribute("errorMessage", errorMessage);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
        } else {
            // 如果是添加操作中的错误，转发回添加表单
            request.setAttribute("errorMessage", errorMessage);
            forwardToFormWithError(request, response);
        }
    }
    
    /**
     * 辅助方法：当表单验证失败时，重新加载专业列表并将用户导回添加课程的表单页面。
     * @param request HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果转发时发生错误。
     * @throws IOException 如果转发时发生错误。
     */
    private void forwardToFormWithError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 为了在返回表单时保留用户已输入的数据，JSP页面可以使用 param.fieldName 来回显
        // 例如: <input type="text" name="courseName" value="<c:out value='${param.courseName}'/>">

        // 重新加载专业列表，因为转发会创建一个新的请求，之前的请求属性会丢失
        // 确保 this.majorService 已在 init 方法中初始化
        if (this.majorService != null) {
            try {
                // 调用 MajorService 获取所有专业信息
                request.setAttribute("majorListFromServlet", this.majorService.listAllMajors());
            } catch (Exception e) {
                System.err.println("SaveCourseServlet (forwardToFormWithError): 重新加载专业列表时出错: " + e.getMessage());
                e.printStackTrace();
                // 设置一个关于专业列表加载失败的特定错误消息
                request.setAttribute("errorMessageForMajorList", "无法重新加载专业列表，请手动选择或稍后重试。");
                request.setAttribute("majorListFromServlet", Collections.emptyList()); // 确保属性存在且为空列表
            }
        } else {
            // 如果 majorService 由于某种原因仍为 null (理论上 init 中会处理或抛异常)
            System.err.println("SaveCourseServlet (forwardToFormWithError): MajorService 未初始化，无法重新加载专业列表。");
            request.setAttribute("errorMessageForMajorList", "专业服务不可用，无法加载专业列表。");
            request.setAttribute("majorListFromServlet", Collections.emptyList());
        }

        request.getRequestDispatcher("/addCourseForm.jsp").forward(request, response);
    }
}