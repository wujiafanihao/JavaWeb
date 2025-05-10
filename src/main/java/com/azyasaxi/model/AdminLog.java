package com.azyasaxi.model;

import java.time.LocalDateTime;
import java.util.Date; // 新增：导入 java.util.Date
import java.sql.Timestamp; // 新增：导入 java.sql.Timestamp 用于转换

public class AdminLog {
    private int logId;
    private Integer adminId; // 可以为null，如果Admin被删除
    private String adminUsername;
    private String actionType;
    private String targetEntity;
    private String targetEntityId;
    private String actionDescription;
    private LocalDateTime actionTimestamp;

    // 构造函数
    public AdminLog() {
    }

    public AdminLog(Integer adminId, String adminUsername, String actionType, String targetEntity, String targetEntityId, String actionDescription) {
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.targetEntity = targetEntity;
        this.targetEntityId = targetEntityId;
        this.actionDescription = actionDescription;
        // this.actionTimestamp = LocalDateTime.now(); // 通常由数据库默认设置或在Service层设置
    }

    // Getter 和 Setter 方法
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(String targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public LocalDateTime getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(LocalDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    /**
     * 将 LocalDateTime 转换为 java.util.Date 以便 JSTL fmt:formatDate 使用。
     * @return java.util.Date 对象，如果 actionTimestamp 为 null 则返回 null。
     */
    public Date getActionTimestampAsDate() {
        if (this.actionTimestamp != null) {
            return Timestamp.valueOf(this.actionTimestamp);
        }
        return null;
    }

    @Override
    public String toString() {
        return "AdminLog{" +
                "logId=" + logId +
                ", adminId=" + adminId +
                ", adminUsername='" + adminUsername + '\'' +
                ", actionType='" + actionType + '\'' +
                ", targetEntity='" + targetEntity + '\'' +
                ", targetEntityId='" + targetEntityId + '\'' +
                ", actionDescription='" + actionDescription + '\'' +
                ", actionTimestamp=" + actionTimestamp +
                '}';
    }
}