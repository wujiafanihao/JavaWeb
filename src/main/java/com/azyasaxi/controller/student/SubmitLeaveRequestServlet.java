package com.azyasaxi.controller.student;

import com.azyasaxi.service.LeaveRequestService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/student/submitLeave")
public class SubmitLeaveRequestServlet extends HttpServlet {
    private LeaveRequestService leaveRequestService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        if (context != null) {
            this.leaveRequestService = context.getBean(LeaveRequestService.class);
        } else {
            throw new ServletException("Spring WebApplicationContext not found for SubmitLeaveRequestServlet.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null || !"student".equals(session.getAttribute("userType")) || session.getAttribute("studentId") == null) {
            response.sendRedirect(request.getContextPath() + "/loginPage.jsp");
            return;
        }

        Integer studentId = (Integer) session.getAttribute("studentId");
        String reason = request.getParameter("reason");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        if (!StringUtils.hasText(reason) || !StringUtils.hasText(startDateStr) || !StringUtils.hasText(endDateStr)) {
            session.setAttribute("errorMessage", "请假事由、开始日期和结束日期均不能为空。");
            response.sendRedirect(request.getContextPath() + "/student/dashboard#leave-management");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false); // 不允许宽松的日期解析
        Date startDate;
        Date endDate;

        try {
            startDate = dateFormat.parse(startDateStr);
            endDate = dateFormat.parse(endDateStr);
        } catch (ParseException e) {
            session.setAttribute("errorMessage", "日期格式不正确，请使用 yyyy-MM-dd 格式。");
            response.sendRedirect(request.getContextPath() + "/student/dashboard#leave-management");
            return;
        }

        if (endDate.before(startDate)) {
            session.setAttribute("errorMessage", "结束日期不能早于开始日期。");
            response.sendRedirect(request.getContextPath() + "/student/dashboard#leave-management");
            return;
        }
        
        // 可以在这里添加对请假日期是否在未来的校验，或请假时长限制等
        Date today = new Date();
        if (startDate.before(today) && !isSameDay(startDate, today)) { // 允许请假当天，但不允许请假过去的时间
             session.setAttribute("errorMessage", "请假开始日期不能早于今天。");
             response.sendRedirect(request.getContextPath() + "/student/dashboard#leave-management");
             return;
        }


        int leaveId = leaveRequestService.submitLeaveRequest(studentId, reason, startDate, endDate);

        if (leaveId > 0) {
            session.setAttribute("successMessage", "请假申请已成功提交，申请ID: " + leaveId + "，请等待审批。");
        } else {
            session.setAttribute("errorMessage", "提交请假申请失败，请稍后再试或联系管理员。");
        }
        response.sendRedirect(request.getContextPath() + "/student/dashboard#leave-management");
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }
}