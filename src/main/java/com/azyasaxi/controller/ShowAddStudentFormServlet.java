package com.azyasaxi.controller;

import com.azyasaxi.model.ClassInfo;
import com.azyasaxi.service.ClassInfoService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.List;

/**
 * ShowAddStudentFormServlet
 * 负责显示用于添加新学生的表单页面。
 * 它会预先加载班级列表供用户选择。
 */
@WebServlet("/admin/showAddStudentForm")
public class ShowAddStudentFormServlet extends HttpServlet {

    private ClassInfoService classInfoService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.classInfoService = context.getBean(ClassInfoService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ShowAddStudentFormServlet.");
        }
    }

    /**
     * 处理 GET 请求。
     * 获取所有班级列表，并转发到添加学生的 JSP 页面 (addStudentForm.jsp)。
     *
     * @param request  HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生 Servlet 相关错误。
     * @throws IOException      如果发生 I/O 错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (this.classInfoService == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ClassInfoService 未正确初始化。");
            return;
        }

        try {
            List<ClassInfo> classList = classInfoService.listAllClassInfos();
            request.setAttribute("classList", classList);
        } catch (Exception e) {
            System.err.println("加载班级列表时出错 (for add student form): " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "加载班级列表失败，无法添加学生。");
            // 仍然尝试转发，JSP中可以显示错误
        }

        request.getRequestDispatcher("/addStudentForm.jsp").forward(request, response);
    }
}