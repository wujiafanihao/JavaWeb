package com.azyasaxi.model;

import org.springframework.util.StringUtils;

import java.math.BigDecimal; // 用于学分
import java.util.List;       // 用于课程列表

/**
 * Student 模型类
 * 代表学生基本信息，并扩展以包含其班级、专业、学院、选课和学分等详细信息。
 */
public class Student {

    // --- 原有字段 ---
    private Integer studentId;    // 学号 (主键)
    private String name;          // 姓名
    private String gender;        // 性别
    private Integer classId;      // 班级ID (外键)
    private String userName;      // 登录用户名
    private String password;      // 登录密码 (通常存储哈希值)

    // --- 扩展字段 (用于列表显示或基本信息) ---
    private String className;     // 班级名称 (通过JOIN查询获取)

    // --- 新增字段 (用于学生详细信息页面) ---
    private String majorName;     // 学生所属专业的名称 (通过班级->专业JOIN获取)
    private String collegeName;   // 学生所属学院的名称 (通过班级->专业->学院JOIN获取)

    // 选课与学分信息
    // 使用 Enrollment 列表，因为 Enrollment 对象可以同时包含课程信息和该生的成绩
    private List<Enrollment> enrollments; // 该学生的所有选课记录 (包含课程详情和成绩)
    private BigDecimal totalEarnedCredits; // 该学生已获得的总学分 (从 CreditSummary 视图获取或计算)

    // 默认构造函数
    public Student() {
    }

    // --- Getter 和 Setter 方法 (包括新增字段的) ---

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    // 新增字段的 Getters 和 Setters
    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public BigDecimal getTotalEarnedCredits() {
        return totalEarnedCredits;
    }

    public void setTotalEarnedCredits(BigDecimal totalEarnedCredits) {
        this.totalEarnedCredits = totalEarnedCredits;
    }

    /**
     * 判断是否明确将 classId 设置为 null。
     * 
     * @return 如果 classId 被显式设置为 null，则返回 true；否则返回 false。
     */
    public boolean isExplicitlySettingClassIdToNull() {
        // 假设 classId 为 null 且没有其他字段被更新时，认为是显式设置为 null
        return this.classId == null && !hasOtherFieldsUpdated();
    }

    /**
     * 检查是否有其他字段被更新。
     * 
     * @return 如果有其他字段被更新，则返回 true；否则返回 false。
     */
    private boolean hasOtherFieldsUpdated() {
        // 这里可以根据实际业务逻辑扩展，例如检查 name、gender、userName 等字段是否被更新
        return StringUtils.hasText(this.name) || StringUtils.hasText(this.gender) || StringUtils.hasText(this.userName);
    }

    // (可选) toString 方法，方便调试
    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", classId=" + classId +
                ", className='" + className + '\'' +
                ", userName='" + userName + '\'' +
                // ", password='[PROTECTED]'" + // 避免直接打印密码
                ", majorName='" + majorName + '\'' +
                ", collegeName='" + collegeName + '\'' +
                ", enrollmentsCount=" + (enrollments != null ? enrollments.size() : 0) +
                ", totalEarnedCredits=" + totalEarnedCredits +
                '}';
    }
    
}