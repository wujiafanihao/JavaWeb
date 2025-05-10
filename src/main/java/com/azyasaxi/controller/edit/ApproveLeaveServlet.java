package com.azyasaxi.controller.edit;

import com.azyasaxi.service.LeaveRequestService;
import com.azyasaxi.service.AdminLogService; // 新增：导入 AdminLogService
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

@WebServlet("/admin/approveLeave")
public class ApproveLeaveServlet extends HttpServlet {
    private LeaveRequestService leaveRequestService;
    private AdminLogService adminLogService; // 新增：AdminLogService 实例

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.leaveRequestService = context.getBean(LeaveRequestService.class);
            this.adminLogService = context.getBean(AdminLogService.class); // 新增：获取 AdminLogService
        } else {
            throw new ServletException("Spring WebApplicationContext not found for ApproveLeaveServlet.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (this.leaveRequestService == null || this.adminLogService == null) { // 新增：检查 adminLogService
            request.getSession().setAttribute("errorMessage", "系统服务错误，请稍后重试。");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=leave#leave");
            return;
        }
        
        HttpSession session = request.getSession(false); //不创建新会话
        String activeModule = "leave"; // 操作完成后返回请假模块

        if (session == null || session.getAttribute("adminId") == null) {
            // 如果会话为空，尝试获取或创建会话来设置错误消息
            if (session == null) session = request.getSession();
            session.setAttribute("errorMessage", "请先登录管理员账户后再进行操作。");
            response.sendRedirect(request.getContextPath() + "/loginPage.jsp");
            return;
        }

        Integer adminId = (Integer) session.getAttribute("adminId");
        String leaveIdStr = request.getParameter("id");

        if (leaveIdStr != null && !leaveIdStr.isEmpty()) {
            try {
                int leaveId = Integer.parseInt(leaveIdStr);
                boolean success = leaveRequestService.approveOrRejectLeaveRequest(leaveId, "已批准", adminId);
                if (success) {
                    session.setAttribute("successMessage", "请假申请 (ID: " + leaveId + ") 已成功批准。");
                    // 记录日志
                    String adminUsername = (String) session.getAttribute("username"); // 获取用户名
                    adminLogService.recordAdminAction(adminId, adminUsername,
                            "批准请假申请", "请假申请", leaveIdStr,
                            "批准了请假申请 (ID: " + leaveIdStr + ")");
                } else {
                    session.setAttribute("errorMessage", "批准请假申请 (ID: " + leaveId + ") 失败。可能申请不存在或状态已改变。");
                }
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "无效的请假申请ID格式。");
            } catch (Exception e) {
                session.setAttribute("errorMessage", "批准请假申请时发生错误：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            session.setAttribute("errorMessage", "未提供请假申请ID，无法批准。");
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?activeModule=" + activeModule + "#" + activeModule);
    }
}