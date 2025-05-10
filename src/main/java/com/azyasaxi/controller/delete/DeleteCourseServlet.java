package com.azyasaxi.controller.delete;

import com.azyasaxi.service.CourseService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

@WebServlet("/admin/deleteCourse")
public class DeleteCourseServlet extends HttpServlet {
    private CourseService courseService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.courseService = context.getBean(CourseService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for DeleteCourseServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()){
            activeModule = "course"; // 默认激活课程管理模块
        }

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int courseId = Integer.parseInt(idStr);
                boolean success = courseService.deleteCourse(courseId);
                if (success) {
                    request.getSession().setAttribute("successMessage", "课程 (ID: " + courseId + ") 删除成功！相关的选课记录也已删除。");
                } else {
                    request.getSession().setAttribute("errorMessage", "删除课程 (ID: " + courseId + ") 失败。可能课程不存在或操作未成功。");
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "无效的课程ID格式。");
            } catch (Exception e) {
                request.getSession().setAttribute("errorMessage", "删除课程时发生错误：" + e.getMessage());
                e.printStackTrace(); // 记录到服务器日志
            }
        } else {
            request.getSession().setAttribute("errorMessage", "未提供课程ID，无法删除。");
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#course");
    }
}