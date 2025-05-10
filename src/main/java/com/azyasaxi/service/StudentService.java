package com.azyasaxi.service;

import com.azyasaxi.dao.StudentDao;          // 导入 StudentDao
import com.azyasaxi.dao.CreditSummaryDao;    // 导入 CreditSummaryDao
import com.azyasaxi.model.Student;           // 导入 Student 模型
import com.azyasaxi.model.Enrollment;        // 导入 Enrollment 模型
import com.azyasaxi.model.CreditSummary;     // 导入 CreditSummary 模型
import com.azyasaxi.utils.CalculateSHA256;   // 导入密码哈希工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Spring 工具类

import java.math.BigDecimal; // 用于初始化总学分
import java.util.Collections;
import java.util.List;

/**
 * StudentService 类 (服务层)
 * 负责处理与学生相关的业务逻辑，包括获取学生的完整详细信息。
 */
@Service
public class StudentService {

    private final StudentDao studentDao;                 // StudentDao 实例
    private final CreditSummaryDao creditSummaryDao;   // CreditSummaryDao 实例

    /**
     * 构造函数，通过 Spring 依赖注入 StudentDao 和 CreditSummaryDao。
     *
     * @param studentDaoArg         由 Spring 容器提供的 StudentDao 实例。
     * @param creditSummaryDaoArg   由 Spring 容器提供的 CreditSummaryDao 实例。
     */
    @Autowired
    public StudentService(StudentDao studentDaoArg, CreditSummaryDao creditSummaryDaoArg) {
        this.studentDao = studentDaoArg;
        this.creditSummaryDao = creditSummaryDaoArg;
    }

    // --- 原有方法 (略作调整以使用 BasicStudentRowMapper 返回的 Student 对象) ---
    public List<Student> searchStudents(String searchTerm) {
        // (与您之前提供的代码一致)
        try {
            return studentDao.searchStudents(searchTerm);
        } catch (Exception e) {
            System.err.println("StudentService: 搜索学生时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 根据学生ID查找学生基本信息 (用于编辑等场景)。
     *
     * @param studentId 学生的ID。
     * @return Student 对象 (只包含基本信息和班级名)，如果未找到则返回 null。
     */
    public Student findStudentByIdForEdit(int studentId) {
        if (studentId <= 0) {
            System.err.println("StudentService: 查找学生失败，无效的学生ID: " + studentId);
            return null;
        }
        try {
            return studentDao.getStudentByIdForEdit(studentId); // 调用新的DAO方法
        } catch (Exception e) {
            System.err.println("StudentService: 根据ID查找学生基本信息时发生错误 (ID: " + studentId + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean addStudent(Student student) {
        // (与您之前提供的代码一致)
        if (student == null || !StringUtils.hasText(student.getName()) ||
                !StringUtils.hasText(student.getUserName()) ||
                !StringUtils.hasText(student.getPassword())) {
            System.err.println("StudentService: 添加学生失败，姓名、用户名和密码不能为空。");
            return false;
        }
        if (student.getGender() == null || (!"男".equals(student.getGender()) && !"女".equals(student.getGender()))) {
            System.err.println("StudentService: 添加学生失败，性别必须是 '男' 或 '女'。");
            return false;
        }
        if (studentDao.getStudentByUsername(student.getUserName().trim()) != null) {
            System.err.println("StudentService: 添加学生失败，用户名 '" + student.getUserName().trim() + "' 已存在。");
            return false;
        }
        
        // 哈希密码
        String hashedPassword = CalculateSHA256.calculateSHA256(student.getPassword());
        student.setPassword(hashedPassword); // 使用哈希后的密码

        try {
            int rowsAffected = studentDao.addStudent(student);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("StudentService: 添加学生时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStudent(Student studentWithUpdates) {
        // (与您之前提供的代码一致，使用可选更新逻辑)
        if (studentWithUpdates == null || studentWithUpdates.getStudentId() == null || studentWithUpdates.getStudentId() <= 0) {
            System.err.println("StudentService: 更新学生失败，学生对象或学生ID无效。");
            return false;
        }
        Student existingStudent = studentDao.getStudentByIdForEdit(studentWithUpdates.getStudentId()); // 获取基本信息以比较
        if (existingStudent == null) {
            System.err.println("StudentService: 更新学生失败，未找到ID为 " + studentWithUpdates.getStudentId() + " 的学生记录。");
            return false;
        }
        Student studentToSave = new Student();
        studentToSave.setStudentId(existingStudent.getStudentId());
        boolean needsUpdate = false;

        if (StringUtils.hasText(studentWithUpdates.getName()) && !studentWithUpdates.getName().trim().equals(existingStudent.getName())) {
            studentToSave.setName(studentWithUpdates.getName().trim());
            needsUpdate = true;
        } else {
            studentToSave.setName(existingStudent.getName());
        }
        if (StringUtils.hasText(studentWithUpdates.getGender()) && ("男".equals(studentWithUpdates.getGender()) || "女".equals(studentWithUpdates.getGender())) && !studentWithUpdates.getGender().equals(existingStudent.getGender())) {
            studentToSave.setGender(studentWithUpdates.getGender());
            needsUpdate = true;
        } else {
            studentToSave.setGender(existingStudent.getGender());
        }
        // ClassId 更新逻辑
        if (studentWithUpdates.getClassId() != null) {
            if (existingStudent.getClassId() == null || !studentWithUpdates.getClassId().equals(existingStudent.getClassId())) {
                studentToSave.setClassId(studentWithUpdates.getClassId() == 0 ? null : studentWithUpdates.getClassId());
                needsUpdate = true;
            } else {
                studentToSave.setClassId(existingStudent.getClassId());
            }
        } else if (existingStudent.getClassId() != null && studentWithUpdates.isExplicitlySettingClassIdToNull()) {
            // 假设Student模型有一个方法 isExplicitlySettingClassIdToNull() 来判断是否用户明确要清除班级
            // 否则，如果 studentWithUpdates.getClassId() 为null，则默认不更新
            studentToSave.setClassId(null);
            needsUpdate = true;
        } else {
            studentToSave.setClassId(existingStudent.getClassId());
        }


        if (StringUtils.hasText(studentWithUpdates.getUserName()) && !studentWithUpdates.getUserName().trim().equalsIgnoreCase(existingStudent.getUserName())) {
            String newUsername = studentWithUpdates.getUserName().trim();
            Student studentWithSameUsername = studentDao.getStudentByUsername(newUsername);
            if (studentWithSameUsername != null && !studentWithSameUsername.getStudentId().equals(existingStudent.getStudentId())) { // 使用equals比较Integer
                System.err.println("StudentService: 更新学生失败，用户名 '" + newUsername + "' 已被其他学生使用。");
                return false;
            }
            studentToSave.setUserName(newUsername);
            needsUpdate = true;
        } else {
            studentToSave.setUserName(existingStudent.getUserName());
        }

        // 处理密码更新
        if (StringUtils.hasText(studentWithUpdates.getPassword())) {
            // 如果提供了新密码，则哈希并设置
            studentToSave.setPassword(CalculateSHA256.calculateSHA256(studentWithUpdates.getPassword()));
            needsUpdate = true; // 标记需要更新
        } else {
            // 如果密码字段为空，则不传递密码给DAO，DAO的逻辑是此时不更新密码字段
            studentToSave.setPassword(null);
        }

        if (!needsUpdate) {
            System.out.println("StudentService: 学生信息 (ID: " + existingStudent.getStudentId() + ") 没有需要更新的字段。");
            return true; // 如果没有任何字段（包括密码）被修改，则认为操作“成功”但未执行更新
        }
        try {
            int rowsAffected = studentDao.updateStudent(studentToSave);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("StudentService: 更新学生 (ID: " + studentToSave.getStudentId() + ") 时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStudent(int studentId) {
        // (与您之前提供的代码一致)
        if (studentId <= 0) {
            System.err.println("StudentService: 删除学生失败，无效的学生ID: " + studentId);
            return false;
        }
        try {
            int rowsAffected = studentDao.deleteStudent(studentId);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("StudentService: 删除学生 (ID: " + studentId + ") 时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- 新增：获取学生完整详细信息的方法 ---
    /**
     * 根据学生ID获取该学生的完整详细信息。
     * 信息将填充到传入的 Student 对象中或返回一个新的 Student 对象。
     *
     * @param studentId 学生的ID。
     * @return 一个 Student 对象，其中包含了基本信息、班级、专业、学院名称、
     *         所有选课记录 (含课程详情和成绩) 以及已获得的总学分。
     *         如果学生不存在，则返回 null。
     */
    public Student getStudentFullDetailsById(int studentId) {
        if (studentId <= 0) {
            System.err.println("StudentService: 获取学生详细信息失败，无效的学生ID: " + studentId);
            return null;
        }

        // 1. 获取学生基本信息、班级、专业、学院信息，并填充到一个Student对象
        Student studentWithDetails = studentDao.getStudentWithHierarchyDetailsById(studentId);

        if (studentWithDetails == null) {
            System.out.println("StudentService: 未找到ID为 " + studentId + " 的学生，无法获取详细信息。");
            return null; // 如果学生基本信息都找不到，则直接返回null
        }

        // 2. 获取学生已选修的课程列表 (包含成绩和课程详情)
        try {
            List<Enrollment> enrollments = studentDao.getEnrollmentsWithCourseDetailsByStudentId(studentId);
            studentWithDetails.setEnrollments(enrollments); // 设置到Student对象的enrollments属性
        } catch (Exception e) {
            System.err.println("StudentService: 获取学生 (ID: " + studentId + ") 的选课列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            studentWithDetails.setEnrollments(Collections.emptyList()); // 出错时设置为空列表
        }

        // 3. 获取学生已获得的总学分 (从 CreditSummary 视图)
        try {
            CreditSummary summary = creditSummaryDao.getCreditSummaryByStudentId(studentId);
            if (summary != null) {
                studentWithDetails.setTotalEarnedCredits(summary.getTotalCredits());
            } else {
                studentWithDetails.setTotalEarnedCredits(BigDecimal.ZERO); // 没有记录则总学分为0
            }
        } catch (Exception e) {
            System.err.println("StudentService: 获取学生 (ID: " + studentId + ") 的总学分时发生错误: " + e.getMessage());
            e.printStackTrace();
            studentWithDetails.setTotalEarnedCredits(null); // 出错时设为null或0
        }

        return studentWithDetails; // 返回填充了所有详细信息的Student对象
    }

    // --- 数据统计相关服务方法 ---

    /**
     * 获取每个学院的学生人数和平均总获得学分统计数据。
     * @return List of Maps, 每个 Map 包含 "college_name", "student_count", "average_total_credits".
     */
    public List<java.util.Map<String, Object>> getStudentStatsByCollege() {
        try {
            return studentDao.getStudentStatsByCollege();
        } catch (Exception e) {
            System.err.println("StudentService: 获取各学院学生统计数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 获取不同学分区间的学生人数统计。
     * @return List of Maps, 每个 Map 包含 "credit_range" 和 "student_count".
     */
    public List<java.util.Map<String, Object>> getStudentCountByCreditRanges() {
        try {
            return studentDao.getStudentCountByCreditRanges();
        } catch (Exception e) {
            System.err.println("StudentService: 获取学分区间学生统计数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}