package com.azyasaxi.controller;

import com.azyasaxi.model.Major; // 导入 Major 模型类
import com.azyasaxi.service.MajorService; // 导入 MajorService 类
import jakarta.servlet.ServletConfig; // 导入 ServletConfig
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext; // 导入 WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils; // 导入 WebApplicationContextUtils

import java.io.IOException;
import java.util.List; // 导入 List

/**
 * ShowAddClassFormServlet
 * 负责显示用于添加新班级的表单页面。
 * 它会预先加载所有专业列表供用户选择。
 */
@WebServlet("/admin/showAddClassForm")
public class ShowAddClassFormServlet extends HttpServlet {

    private MajorService majorService; // MajorService 实例

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.majorService = context.getBean(MajorService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ShowAddClassFormServlet. Ensure ContextLoaderListener is configured in web.xml.");
        }
    }

    /**
     * 处理 GET 请求。
     * 获取所有专业列表，将其设置到请求属性中，并转发到添加班级的 JSP 页面 (addClassForm.jsp)。
     *
     * @param request  HttpServletRequest 对象。
     * @param response HttpServletResponse 对象。
     * @throws ServletException 如果发生 Servlet 相关错误。
     * @throws IOException      如果发生 I/O 错误。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (this.majorService == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MajorService 未正确初始化。");
            return;
        }

        try {
            List<Major> majorList = majorService.listAllMajors();
            request.setAttribute("majorList", majorList);
        } catch (Exception e) {
            System.err.println("加载专业列表时出错 (for add class form): " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "加载专业列表失败，无法正常添加班级。");
            // 即使加载专业失败，也尝试显示表单，但下拉列表会是空的或只有错误提示
        }

        // 转发到表单页面
        request.getRequestDispatcher("/addClassForm.jsp").forward(request, response);
    }
}