package com.azyasaxi.service;

import com.azyasaxi.dao.AdminLogDao;
import com.azyasaxi.model.AdminLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AdminLogService {

    private final AdminLogDao adminLogDao;

    @Autowired
    public AdminLogService(AdminLogDao adminLogDao) {
        this.adminLogDao = adminLogDao;
    }

    /**
     * 记录一条管理员操作日志。
     *
     * @param adminId           操作管理员的ID。
     * @param adminUsername     操作管理员的用户名。
     * @param actionType        操作类型 (例如："新增学生", "修改课程")。
     * @param targetEntity      操作针对的实体类型 (例如："学生", "课程")。
     * @param targetEntityId    操作针对的实体ID。
     * @param actionDescription 对操作的详细描述。
     * @return 如果记录成功返回 true，否则返回 false。
     */
    public boolean recordAdminAction(Integer adminId, String adminUsername, String actionType,
                                     String targetEntity, String targetEntityId, String actionDescription) {
        if (adminId == null || adminUsername == null || adminUsername.trim().isEmpty() ||
            actionType == null || actionType.trim().isEmpty()) {
            System.err.println("AdminLogService: 记录日志失败，管理员ID、用户名或操作类型不能为空。");
            return false;
        }

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAdminUsername(adminUsername);
        log.setActionType(actionType);
        log.setTargetEntity(targetEntity); // 可以为 null
        log.setTargetEntityId(targetEntityId); // 可以为 null
        log.setActionDescription(actionDescription); // 可以为 null

        try {
            return adminLogDao.addLog(log);
        } catch (Exception e) {
            System.err.println("AdminLogService: 记录管理员操作日志时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 分页获取管理员操作日志。
     * @param pageNumber 当前页码 (从1开始)。
     * @param pageSize 每页记录数。
     * @return 日志列表。
     */
    public List<AdminLog> getLogsPaged(int pageNumber, int pageSize) {
        if (pageNumber < 1) pageNumber = 1;
        if (pageSize < 1) pageSize = 10; // 默认每页10条
        int offset = (pageNumber - 1) * pageSize;
        try {
            return adminLogDao.getLogs(offset, pageSize);
        } catch (Exception e) {
            System.err.println("AdminLogService: 分页获取日志时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有管理员操作日志的总数。
     * @return 日志总数。
     */
    public int getTotalLogCount() {
        try {
            return adminLogDao.countLogs();
        } catch (Exception e) {
            System.err.println("AdminLogService: 获取日志总数时发生错误: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}