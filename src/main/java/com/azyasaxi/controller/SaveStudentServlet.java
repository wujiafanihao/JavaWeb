package com.azyasaxi.controller;

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

/**
 * SaveStudentServlet
 * 负责处理添加新学生的表单提交。
 */
@WebServlet("/admin/saveStudent")
public class SaveStudentServlet extends HttpServlet {

    private StudentService studentService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.studentService = context.getBean(StudentService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for SaveStudentServlet.");
        }
    }

    /**
     * 处理 POST 请求，用于保存新的学生信息。
     *
     * @param request  HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生 Servlet 相关错误。
     * @throws IOException      如果发生 I/O 错误。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (this.studentService == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "StudentService 未正确初始化。");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String gender = request.getParameter("gender");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password"); // 注意：密码应进行哈希处理
        String classIdStr = request.getParameter("classId");

        // 基本验证
        if (name == null || name.trim().isEmpty() ||
            gender == null || gender.trim().isEmpty() ||
            userName == null || userName.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            
            request.setAttribute("errorMessage", "学生姓名、性别、用户名和密码不能为空！");
            // 保留用户已输入的值，以便在表单中回显
            request.setAttribute("nameValue", name);
            request.setAttribute("genderValue", gender);
            request.setAttribute("userNameValue", userName);
            request.setAttribute("classIdValue", classIdStr);
            // 需要重新加载班级列表以供表单选择
            // (或者在 ShowAddStudentFormServlet 中将班级列表也存入会话，这里从会话取)
            // 为简单起见，这里不重新加载班级列表，但实际应用中需要考虑
            request.getRequestDispatcher("/addStudentForm.jsp").forward(request, response);
            return;
        }

        Student newStudent = new Student();
        newStudent.setName(name.trim());
        newStudent.setGender(gender);
        newStudent.setUserName(userName.trim());
        newStudent.setPassword(password); // TODO: 实际应用中密码应该在Service层加密

        if (classIdStr != null && !classIdStr.trim().isEmpty()) {
            try {
                newStudent.setClassId(Integer.parseInt(classIdStr.trim()));
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "班级ID格式不正确！");
                request.setAttribute("nameValue", name);
                request.setAttribute("genderValue", gender);
                request.setAttribute("userNameValue", userName);
                request.getRequestDispatcher("/addStudentForm.jsp").forward(request, response);
                return;
            }
        } else {
            newStudent.setClassId(null);
        }

        boolean success = studentService.addStudent(newStudent);

        if (success) {
            request.getSession().setAttribute("successMessage", "学生 '" + name + "' 添加成功！");
        } else {
            // 如果Service层或DAO层有更具体的错误原因（例如用户名已存在），可以传递过来
            request.getSession().setAttribute("errorMessage", "添加学生 '" + name + "' 失败。可能是用户名已存在或服务器内部错误。");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard#student");
    }
}