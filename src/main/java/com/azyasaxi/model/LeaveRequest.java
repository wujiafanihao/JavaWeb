package com.azyasaxi.model;

import java.util.Date; // 导入 Date 类用于处理日期和时间

/**
 * 请假信息实体类 (LeaveRequest)
 * 对应数据库中的 LeaveRequest 表
 */
public class LeaveRequest {

    private Integer leaveId; // 请假记录的唯一标识符，主键，自增
    private Integer studentId; // 提交请假申请的学生ID (外键关联 Student 表)
    private String studentName; //  提交请假申请的学生姓名，不能为空
    private String adminName; // 审批该请假申请的管理员姓名，可以为空
    private String reason; // 请假原因，不能为空
    private Date startDate; // 请假开始日期，不能为空
    private Date endDate; // 请假结束日期，不能为空
    private String status; // 请假申请的状态 (例如: "待审批", "已批准", "已驳回")，默认为 "待审批"
    private Date requestDate; // 请假申请提交的时间，默认为当前时间戳
    private Date approvalDate; // 请假申请被审批的时间，可以为空
    private Integer approvedByAdminId; // 审批该请假申请的管理员ID (外键关联 Admin 表)，可以为空

    // 默认构造函数
    public LeaveRequest() {
    }

    // 包含所有字段的构造函数 (可选，根据需要添加)
    public LeaveRequest(Integer leaveId, Integer studentId, String studentName, String  adminName,String reason, Date startDate, Date endDate, String status, Date requestDate, Date approvalDate, Integer approvedByAdminId) {
        this.leaveId = leaveId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.adminName = adminName;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.requestDate = requestDate;
        this.approvalDate = approvalDate;
        this.approvedByAdminId = approvedByAdminId;
    }

    // Getter 和 Setter 方法
    public Integer getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(Integer leaveId) {
        this.leaveId = leaveId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() { return studentName; }

    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAdminUserName() { return adminName; }

    public void setAdminUsername(String adminName) { this.adminName = adminName; }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Integer getApprovedByAdminId() {
        return approvedByAdminId;
    }

    public void setApprovedByAdminId(Integer approvedByAdminId) {
        this.approvedByAdminId = approvedByAdminId;
    }

    // toString 方法 (可选，方便调试)
    @Override
    public String toString() {
        return "LeaveRequest{" +
                "leaveId=" + leaveId +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", adminName='" + adminName + '\'' +
                ", reason='" + reason + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", requestDate=" + requestDate +
                ", approvalDate=" + approvalDate +
                ", approvedByAdminId=" + approvedByAdminId +
                '}';
    }
}
