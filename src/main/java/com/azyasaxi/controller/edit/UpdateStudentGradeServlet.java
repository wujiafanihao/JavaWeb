package com.azyasaxi.controller.edit;

import com.azyasaxi.service.EnrollmentService;
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
import org.springframework.util.StringUtils; // For StringUtils.hasText

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/admin/updateStudentGrade")
public class UpdateStudentGradeServlet extends HttpServlet {
    private EnrollmentService enrollmentService;
    private AdminLogService adminLogService; // 新增：AdminLogService 实例

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.enrollmentService = context.getBean(EnrollmentService.class);
            this.adminLogService = context.getBean(AdminLogService.class); // 新增：获取 AdminLogService
        } else {
            throw new ServletException("Spring WebApplicationContext not found for UpdateStudentGradeServlet.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (this.enrollmentService == null || this.adminLogService == null) { // 新增：检查 adminLogService
            request.getSession().setAttribute("errorMessage", "系统服务错误，请稍后重试。");
            // 尝试重定向回仪表盘的学分模块
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=credit#credit");
            return;
        }
        
        request.setCharacterEncoding("UTF-8");
        String studentIdStr = request.getParameter("studentId");
        String courseIdStr = request.getParameter("courseId");
        String newGradeStr = request.getParameter("newGrade");

        if (!StringUtils.hasText(studentIdStr) || !StringUtils.hasText(courseIdStr)) {
            request.getSession().setAttribute("errorMessage", "更新成绩失败：缺少学生ID或课程ID。");
            // 尝试重定向回仪表盘的学分模块，因为可能无法确定是哪个学生的详情页
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=credit#credit");
            return;
        }

        int studentId = 0;
        int courseId = 0;
        BigDecimal gradeToUpdate = null;

        try {
            studentId = Integer.parseInt(studentIdStr);
            courseId = Integer.parseInt(courseIdStr);

            if (StringUtils.hasText(newGradeStr)) {
                gradeToUpdate = new BigDecimal(newGradeStr.trim());
            } // 如果 newGradeStr 为空或null, gradeToUpdate 保持 null, 表示清除成绩

            boolean success = enrollmentService.updateStudentCourseGrade(studentId, courseId, gradeToUpdate);

            if (success) {
                request.getSession().setAttribute("successMessage", "学生 (ID: " + studentId + ") 的课程 (ID: " + courseId + ") 成绩更新成功！");
                // 记录日志
                HttpSession session = request.getSession(false);
                Integer adminIdFromSession = (session != null) ? (Integer) session.getAttribute("adminId") : null;
                String adminUsername = (session != null) ? (String) session.getAttribute("username") : "未知管理员";
                String description = "修改了学生 (ID: " + studentIdStr + ") 的课程 (ID: " + courseIdStr + ") 的成绩为: " +
                                     (gradeToUpdate != null ? gradeToUpdate.toPlainString() : "空值/清除");
                adminLogService.recordAdminAction(adminIdFromSession, adminUsername,
                        "修改学生成绩", "学生选课记录", "S:" + studentIdStr + "_C:" + courseIdStr,
                        description);
            } else {
                request.getSession().setAttribute("errorMessage", "更新成绩失败。请检查输入或联系管理员。");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "无效的ID或成绩格式。");
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "更新成绩时发生错误：" + e.getMessage());
            e.printStackTrace();
        }

        // 重定向回该学生的学分详情页
        response.sendRedirect(request.getContextPath() + "/admin/viewStudentCreditDetails?studentId=" + studentIdStr);
    }
}