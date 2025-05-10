package com.azyasaxi.controller.detail;

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

@WebServlet("/admin/viewClassInfoDetails")
public class ViewClassInfoDetailsServlet extends HttpServlet {
    private ClassInfoService classInfoService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.classInfoService = context.getBean(ClassInfoService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ViewClassInfoDetailsServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()) {
            activeModule = "student"; // 默认是学生管理模块下的班级列表
        }

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int classId = Integer.parseInt(idStr);
                ClassInfo classDetails = classInfoService.findClassInfoWithDetailsById(classId);

                if (classDetails != null) {
                    request.setAttribute("detailsData", classDetails);
                    request.setAttribute("viewMode", "classDetails"); // 指示JSP显示班级详情片段
                } else {
                    request.setAttribute("errorMessage", "未找到ID为 " + classId + " 的班级详情。");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "无效的班级ID格式。");
            } catch (Exception e) {
                request.setAttribute("errorMessage", "获取班级详情时发生错误：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            request.setAttribute("errorMessage", "未提供班级ID以查看详情。");
        }

        request.setAttribute("activeModule", activeModule); // 传递 activeModule 以便 "返回仪表盘" 链接能正确高亮或定位
        // 转发到新的 detail.jsp 页面
        request.getRequestDispatcher("/detail.jsp").forward(request, response);
    }
}