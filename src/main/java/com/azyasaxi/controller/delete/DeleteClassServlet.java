package com.azyasaxi.controller.delete;

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

@WebServlet("/admin/deleteClass")
public class DeleteClassServlet extends HttpServlet {
    private ClassInfoService classInfoService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.classInfoService = context.getBean(ClassInfoService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for DeleteClassServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String activeModule = request.getParameter("activeModule");
        if (activeModule == null || activeModule.isEmpty()){
            activeModule = "student"; // 默认激活学生管理模块的班级列表部分
        }


        if (idStr != null && !idStr.isEmpty()) {
            try {
                int classId = Integer.parseInt(idStr);
                boolean success = classInfoService.deleteClassInfo(classId);
                if (success) {
                    request.getSession().setAttribute("successMessage", "班级 (ID: " + classId + ") 删除成功！");
                } else {
                    request.getSession().setAttribute("errorMessage", "删除班级 (ID: " + classId + ") 失败。可能班级不存在或有关联数据无法删除。");
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "无效的班级ID格式。");
            } catch (Exception e) {
                request.getSession().setAttribute("errorMessage", "删除班级时发生错误：" + e.getMessage());
                e.printStackTrace(); // 记录到服务器日志
            }
        } else {
            request.getSession().setAttribute("errorMessage", "未提供班级ID，无法删除。");
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#student");
    }
}