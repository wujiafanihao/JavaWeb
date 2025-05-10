package com.azyasaxi.controller.detail;

import com.azyasaxi.model.Course;
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

@WebServlet("/admin/viewCourseDetails")
public class ViewCourseDetailsServlet extends HttpServlet {
    private CourseService courseService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.courseService = context.getBean(CourseService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ViewCourseDetailsServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()) {
            activeModule = "course"; // 默认是课程管理模块
        }

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int courseId = Integer.parseInt(idStr);
                // getCourseDetailsById 方法应该返回包含选课学生列表的Course对象
                Course courseDetails = courseService.getCourseDetailsById(courseId);

                if (courseDetails != null) {
                    request.setAttribute("detailsData", courseDetails);
                    request.setAttribute("viewMode", "courseDetails"); // 指示JSP显示课程详情片段
                } else {
                    request.setAttribute("errorMessage", "未找到ID为 " + courseId + " 的课程详情。");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "无效的课程ID格式。");
            } catch (Exception e) {
                request.setAttribute("errorMessage", "获取课程详情时发生错误：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            request.setAttribute("errorMessage", "未提供课程ID以查看详情。");
        }

        request.setAttribute("activeModule", activeModule);
        request.getRequestDispatcher("/detail.jsp").forward(request, response);
    }
}