package com.azyasaxi.controller.saveData;

import com.azyasaxi.model.Student;
import com.azyasaxi.service.AdminLogService; // 新增：导入 AdminLogService
import com.azyasaxi.service.StudentService;
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
 * SaveStudentServlet
 * 负责处理添加新学生的表单提交。
 */
@WebServlet("/admin/saveStudent")
public class SaveStudentServlet extends HttpServlet {

    private StudentService studentService;
    private AdminLogService adminLogService; // 新增：AdminLogService 实例

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
            this.adminLogService = context.getBean(AdminLogService.class); // 新增：获取 AdminLogService
        } else {
            throw new ServletException("Spring WebApplicationContext not found for SaveStudentServlet.");
        }
    }

    /**
     * 处理 POST 请求，用于添加或更新学生信息。
     *
     * @param request  HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生 Servlet 相关错误。
     * @throws IOException      如果发生 I/O 错误。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (this.studentService == null || this.adminLogService == null) { // 确保 adminLogService 也被检查
            request.getSession().setAttribute("errorMessage", "系统服务错误，请稍后重试。");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard#student");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String studentIdStr = request.getParameter("studentId");
        String name = request.getParameter("name");
        String gender = request.getParameter("gender");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password"); // 对于更新，此字段可选
        String classIdStr = request.getParameter("classId");
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()) {
            activeModule = "student";
        }

        // 通用基本验证 (姓名、性别、用户名在添加和更新时都不能为空)
        if (name == null || name.trim().isEmpty() ||
            gender == null || gender.trim().isEmpty() ||
            userName == null || userName.trim().isEmpty()) {
            
            request.getSession().setAttribute("errorMessage", "学生姓名、性别和用户名不能为空！");
            // 如果是更新操作失败，理想情况是重定向回编辑页面并回显，但这里简化处理
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
            return;
        }

        Student student = new Student();
        student.setName(name.trim());
        student.setGender(gender);
        student.setUserName(userName.trim());

        if (classIdStr != null && !classIdStr.trim().isEmpty()) {
            try {
                student.setClassId(Integer.parseInt(classIdStr.trim()));
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "班级ID格式不正确！");
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
                return;
            }
        } else {
            student.setClassId(null); // 允许不分配班级或清除班级
        }

        boolean success;
        String actionMessage;

        if (studentIdStr != null && !studentIdStr.trim().isEmpty()) {
            // 更新操作
            try {
                student.setStudentId(Integer.parseInt(studentIdStr.trim()));
                if (password != null && !password.isEmpty()) {
                    student.setPassword(password); // Service层会处理哈希
                } else {
                    student.setPassword(null); // 明确传递null表示不更新密码
                }
                success = studentService.updateStudent(student);
                actionMessage = success ? "学生 (ID: " + student.getStudentId() + ") 信息更新成功！" : "学生 (ID: " + student.getStudentId() + ") 信息更新失败。";
                
                if (success) {
                    // 记录修改日志
                    HttpSession session = request.getSession(false);
                    Integer adminId = (session != null) ? (Integer) session.getAttribute("adminId") : null;
                    String adminUsername = (session != null) ? (String) session.getAttribute("username") : "未知管理员";
                    adminLogService.recordAdminAction(adminId, adminUsername,
                            "修改学生信息", "学生", String.valueOf(student.getStudentId()),
                            "修改了学生 " + student.getName() + " (ID: " + student.getStudentId() + ") 的信息。");
                }
            } catch (NumberFormatException e) {
                success = false;
                actionMessage = "无效的学生ID格式，更新失败。";
            }
        } else {
            // 添加操作
            if (password == null || password.isEmpty()) {
                request.getSession().setAttribute("errorMessage", "添加新学生时，密码不能为空！");
                response.sendRedirect(request.getContextPath() + "/admin/showAddStudentForm"); // 转发回添加表单
                return;
            }
            student.setPassword(password); // Service层会处理哈希
            success = studentService.addStudent(student); // studentDao.addStudent 不返回ID，所以日志中ID可能不准确
            
            if (success) {
                actionMessage = "学生 '" + student.getName() + "' 添加成功！";
                // 记录新增日志
                // 由于 addStudent 不返回新生成的ID，我们暂时无法在日志中记录准确的 target_entity_id
                // 可以记录用户名作为标识，或者后续改进DAO层使其返回ID
                HttpSession session = request.getSession(false);
                Integer adminId = (session != null) ? (Integer) session.getAttribute("adminId") : null;
                String adminUsername = (session != null) ? (String) session.getAttribute("username") : "未知管理员";
                adminLogService.recordAdminAction(adminId, adminUsername,
                        "新增学生", "学生", student.getUserName(), // 使用用户名作为临时ID
                        "添加了新学生: " + student.getName() + " (用户名: " + student.getUserName() + ")");
            } else {
                 actionMessage = "添加学生 '" + student.getName() + "' 失败。可能是用户名已存在。";
            }
        }

        if (success) {
            request.getSession().setAttribute("successMessage", actionMessage);
        } else {
            request.getSession().setAttribute("errorMessage", actionMessage);
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
    }
}