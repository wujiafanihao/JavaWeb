package com.azyasaxi.controller.admin;

import com.azyasaxi.model.AdminLog;
import com.azyasaxi.service.AdminLogService;
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

@WebServlet("/admin/systemLogs")
public class SystemLogServlet extends HttpServlet {
    private AdminLogService adminLogService;
    private static final int DEFAULT_PAGE_SIZE = 15; // 每页默认显示15条日志

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.adminLogService = context.getBean(AdminLogService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for SystemLogServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (this.adminLogService == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "AdminLogService 未正确初始化。");
            return;
        }

        int pageNumber = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageStr);
                if (pageNumber < 1) {
                    pageNumber = 1;
                }
            } catch (NumberFormatException e) {
                System.err.println("SystemLogServlet: 无效的页码参数: " + pageStr);
                // pageNumber 保持为 1
            }
        }

        try {
            List<AdminLog> logList = adminLogService.getLogsPaged(pageNumber, DEFAULT_PAGE_SIZE);
            int totalLogs = adminLogService.getTotalLogCount();
            int totalPages = (int) Math.ceil((double) totalLogs / DEFAULT_PAGE_SIZE);
            if (totalPages == 0 && totalLogs > 0) totalPages = 1; // 至少一页

            request.setAttribute("logList", logList);
            request.setAttribute("currentPage", pageNumber);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", DEFAULT_PAGE_SIZE); // 如果JSP需要知道
            request.setAttribute("totalLogs", totalLogs);

        } catch (Exception e) {
            System.err.println("SystemLogServlet: 获取系统日志时出错: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "加载系统日志时发生内部错误。");
        }

        request.getRequestDispatcher("/systemLogs.jsp").forward(request, response);
    }
}