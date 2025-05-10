package com.azyasaxi.controller.showDataForm;

import com.azyasaxi.model.Course;
import com.azyasaxi.model.Major;
import com.azyasaxi.service.CourseService;
import com.azyasaxi.service.MajorService;
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

@WebServlet("/admin/editCourse")
public class ShowEditCourseFormServlet extends HttpServlet {
    private CourseService courseService;
    private MajorService majorService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.courseService = context.getBean(CourseService.class);
            this.majorService = context.getBean(MajorService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ShowEditCourseFormServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = "course"; // 编辑课程后通常返回课程模块

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int courseId = Integer.parseInt(idStr);
                // findCourseById 获取课程基本信息，对于编辑表单足够
                Course courseToEdit = courseService.findCourseById(courseId); 
                List<Major> majorList = majorService.listAllMajors(); // 获取所有专业用于下拉选择

                if (courseToEdit != null) {
                    request.setAttribute("editData", courseToEdit); // 将课程数据传递给JSP
                    request.setAttribute("majorList", majorList);   // 将专业列表传递给JSP
                    request.setAttribute("viewMode", "editCourse"); // 指示JSP显示课程编辑表单
                } else {
                    request.getSession().setAttribute("errorMessage", "未找到ID为 " + courseId + " 的课程以进行编辑。");
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
                    return;
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "无效的课程ID格式。");
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
                return;
            } catch (Exception e) {
                request.getSession().setAttribute("errorMessage", "加载课程编辑表单时发生错误：" + e.getMessage());
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
                return;
            }
        } else {
            request.getSession().setAttribute("errorMessage", "未提供课程ID，无法编辑。");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
            return;
        }

        request.setAttribute("activeModule", activeModule);
        // 转发到通用的 detail.jsp 页面
        request.getRequestDispatcher("/detail.jsp").forward(request, response);
    }
}