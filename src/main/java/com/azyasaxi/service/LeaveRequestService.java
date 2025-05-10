package com.azyasaxi.service;

import com.azyasaxi.dao.LeaveRequestDao; // 导入 LeaveRequestDao
import com.azyasaxi.model.LeaveRequest;   // 导入 LeaveRequest 模型
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections; // 用于返回空列表
import java.util.Date;        // 用于处理日期
import java.util.List;

/**
 * LeaveRequestService 类 (服务层)
 * 负责处理与请假申请相关的业务逻辑。
 * 它会调用 LeaveRequestDao 来与数据库进行交互。
 */
@Service // 将此类标记为 Spring 管理的 Service 组件
public class LeaveRequestService {

    private final LeaveRequestDao leaveRequestDao; // LeaveRequestDao 实例，通过构造函数注入

    /**
     * 构造函数，通过 Spring 依赖注入 LeaveRequestDao。
     *
     * @param leaveRequestDaoArg 由 Spring 容器提供的 LeaveRequestDao 实例。
     *                           参数名使用 leaveRequestDaoArg 以避免与成员变量混淆。
     */
    @Autowired // 自动注入 LeaveRequestDao bean
    public LeaveRequestService(LeaveRequestDao leaveRequestDaoArg) {
        this.leaveRequestDao = leaveRequestDaoArg;
    }

    /**
     * 获取所有请假申请信息的列表，或者根据条件进行搜索。
     *
     * @param studentNameSearchTerm 用于在学生姓名中搜索的词 (可选)。
     * @param statusSearchTerm 用于在请假状态中搜索的词 (可选)。
     * @return 包含匹配 LeaveRequest 对象的列表。
     */
    public List<LeaveRequest> listLeaveRequests(String studentNameSearchTerm, String statusSearchTerm) {
        // 直接调用 DAO 层的搜索方法
        try {
            return leaveRequestDao.searchLeaveRequests(studentNameSearchTerm, statusSearchTerm);
        } catch (Exception e) {
            System.err.println("LeaveRequestService: 获取请假列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // 出错时返回空列表
        }
    }

    /**
     * 根据学生ID获取该学生的所有请假申请。
     *
     * @param studentId 学生的ID。
     * @return 包含该学生所有请假申请的列表。
     */
    public List<LeaveRequest> listLeaveRequestsByStudent(int studentId) {
        if (studentId <= 0) {
            System.err.println("LeaveRequestService: 无效的学生ID: " + studentId);
            return Collections.emptyList();
        }
        try {
            return leaveRequestDao.getLeaveRequestsByStudentId(studentId);
        } catch (Exception e) {
            System.err.println("LeaveRequestService: 根据学生ID获取请假列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 学生提交一个新的请假申请。
     *
     * @param studentId 提交申请的学生ID。
     * @param reason 请假原因。
     * @param startDate 请假开始日期。
     * @param endDate 请假结束日期。
     * @return 如果添加成功，返回新创建的请假记录的ID；否则返回 -1 或其他错误指示。
     */
    public int submitLeaveRequest(Integer studentId, String reason, Date startDate, Date endDate) {
        // 基本的业务验证
        if (studentId == null || studentId <= 0) {
            System.err.println("LeaveRequestService: 提交请假失败，无效的学生ID。");
            return -1; // 或抛出业务异常
        }
        if (reason == null || reason.trim().isEmpty()) {
            System.err.println("LeaveRequestService: 提交请假失败，请假原因不能为空。");
            return -1;
        }
        if (startDate == null || endDate == null) {
            System.err.println("LeaveRequestService: 提交请假失败，开始或结束日期不能为空。");
            return -1;
        }
        if (endDate.before(startDate)) { // 结束日期不能早于开始日期
            System.err.println("LeaveRequestService: 提交请假失败，结束日期不能早于开始日期。");
            return -1;
        }
        // 可以添加更多业务规则，例如请假时长限制等

        LeaveRequest newLeaveRequest = new LeaveRequest();
        newLeaveRequest.setStudentId(studentId);
        newLeaveRequest.setReason(reason.trim());
        newLeaveRequest.setStartDate(startDate);
        newLeaveRequest.setEndDate(endDate);
        newLeaveRequest.setStatus("待审批"); // 新提交的申请默认为“待审批”状态
        // requestDate 由数据库自动生成 (DEFAULT CURRENT_TIMESTAMP)

        try {
            return leaveRequestDao.addLeaveRequest(newLeaveRequest);
        } catch (Exception e) {
            System.err.println("LeaveRequestService: 提交请假申请时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 根据请假ID获取请假申请的详细信息。
     *
     * @param leaveId 请假记录的ID。
     * @return 如果找到，返回 LeaveRequest 对象；否则返回 null。
     */
    public LeaveRequest findLeaveRequestById(int leaveId) {
        if (leaveId <= 0) {
            System.err.println("LeaveRequestService: 无效的请假ID: " + leaveId);
            return null;
        }
        try {
            return leaveRequestDao.getLeaveRequestById(leaveId);
        } catch (Exception e) {
            System.err.println("LeaveRequestService: 根据ID查找请假申请时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 管理员审批请假申请。
     *
     * @param leaveId 要审批的请假记录的ID。
     * @param newStatus 新的审批状态 ("已批准" 或 "已驳回")。
     * @param adminId 执行审批的管理员ID。
     * @return 如果操作成功，返回 true；否则返回 false。
     */
    public boolean approveOrRejectLeaveRequest(int leaveId, String newStatus, Integer adminId) {
        if (leaveId <= 0) {
            System.err.println("LeaveRequestService: 审批失败，无效的请假ID。");
            return false;
        }
        if (adminId == null || adminId <= 0) {
            System.err.println("LeaveRequestService: 审批失败，无效的管理员ID。");
            return false;
        }
        if (!"已批准".equals(newStatus) && !"已驳回".equals(newStatus)) { // 验证审批状态的有效性
            System.err.println("LeaveRequestService: 审批失败，无效的审批状态: " + newStatus);
            return false;
        }

        // （可选）可以先获取请假记录，检查当前状态是否允许审批等业务逻辑
        LeaveRequest existingRequest = leaveRequestDao.getLeaveRequestById(leaveId);
        if (existingRequest == null) {
            System.err.println("LeaveRequestService: 审批失败，未找到ID为 " + leaveId + " 的请假申请。");
            return false;
        }
        if (!"待审批".equals(existingRequest.getStatus())) {
            System.err.println("LeaveRequestService: 审批失败，该请假申请 (ID: " + leaveId + ") 当前状态为 '" + existingRequest.getStatus() + "'，不能重复审批。");
            return false;
        }

        try {
            int rowsAffected = leaveRequestDao.updateLeaveRequestStatus(leaveId, newStatus, adminId);
            return rowsAffected > 0; // 如果影响行数大于0，则表示更新成功
        } catch (Exception e) {
            System.err.println("LeaveRequestService: 审批请假申请时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 学生更新自己的请假申请。
     * 只有在“待审批”状态下才能修改。
     *
     * @param leaveId 要修改的请假ID。
     * @param studentId 进行修改操作的学生ID (用于验证权限)。
     * @param newReason 新的请假原因。
     * @param newStartDate 新的开始日期。
     * @param newEndDate 新的结束日期。
     * @return 如果更新成功返回true，否则返回false。
     */
    public boolean updateLeaveRequestByStudent(int leaveId, int studentId, String newReason, Date newStartDate, Date newEndDate) {
        if (leaveId <= 0 || studentId <= 0) {
            System.err.println("LeaveRequestService: 更新请假失败，无效的请假ID或学生ID。");
            return false;
        }
        // 其他参数验证 (原因、日期等)
        if (newReason == null || newReason.trim().isEmpty() || newStartDate == null || newEndDate == null || newEndDate.before(newStartDate)) {
            System.err.println("LeaveRequestService: 更新请假失败，参数无效。");
            return false;
        }

        LeaveRequest existingRequest = leaveRequestDao.getLeaveRequestById(leaveId);
        if (existingRequest == null) {
            System.err.println("LeaveRequestService: 更新失败，未找到ID为 " + leaveId + " 的请假申请。");
            return false;
        }
        if (existingRequest.getStudentId() != studentId) {
            System.err.println("LeaveRequestService: 更新失败，学生ID不匹配，无权修改此请假申请。");
            return false; // 权限验证
        }
        if (!"待审批".equals(existingRequest.getStatus())) {
            System.err.println("LeaveRequestService: 更新失败，该请假申请 (ID: " + leaveId + ") 当前状态为 '" + existingRequest.getStatus() + "'，不能修改。");
            return false;
        }

        LeaveRequest updatedRequest = new LeaveRequest();
        updatedRequest.setLeaveId(leaveId);
        updatedRequest.setReason(newReason.trim());
        updatedRequest.setStartDate(newStartDate);
        updatedRequest.setEndDate(newEndDate);
        // studentId, status, requestDate, approvalDate, approvedByAdminId 不应由学生修改

        try {
            int rowsAffected = leaveRequestDao.updateLeaveRequestByStudent(updatedRequest);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("LeaveRequestService: 学生更新请假申请时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 根据请假ID删除请假申请 (通常由学生在申请被审批前，或管理员进行操作)。
     *
     * @param leaveId 要删除的请假记录的ID。
     * @param requesterId 执行删除操作的用户ID (可以是学生ID或管理员ID，用于权限判断)。
     * @param requesterType 请求者类型 ("student" 或 "admin")。
     * @return 如果删除成功，返回 true；否则返回 false。
     */
    public boolean deleteLeaveRequest(int leaveId, int requesterId, String requesterType) {
        if (leaveId <= 0) {
            System.err.println("LeaveRequestService: 删除失败，无效的请假ID。");
            return false;
        }

        LeaveRequest existingRequest = leaveRequestDao.getLeaveRequestById(leaveId);
        if (existingRequest == null) {
            System.err.println("LeaveRequestService: 删除失败，未找到ID为 " + leaveId + " 的请假申请。");
            return false;
        }

        // 权限控制：学生只能删除自己“待审批”的申请，管理员可以删除任何申请
        if ("student".equalsIgnoreCase(requesterType)) {
            if (existingRequest.getStudentId() != requesterId) {
                System.err.println("LeaveRequestService: 学生删除失败，无权删除不属于自己的请假申请。");
                return false;
            }
            if (!"待审批".equals(existingRequest.getStatus())) {
                System.err.println("LeaveRequestService: 学生删除失败，只能删除状态为“待审批”的请假申请。");
                return false;
            }
        } else if (!"admin".equalsIgnoreCase(requesterType)) {
            System.err.println("LeaveRequestService: 删除失败，未知的请求者类型。");
            return false;
        }
        // 如果是管理员，则不加额外限制 (或者可以根据业务添加其他限制)

        try {
            int rowsAffected = leaveRequestDao.deleteLeaveRequest(leaveId);
            return rowsAffected > 0; // 如果影响行数大于0，则表示删除成功
        } catch (Exception e) {
            System.err.println("LeaveRequestService: 删除请假申请时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}