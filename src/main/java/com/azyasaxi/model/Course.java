package com.azyasaxi.model;

import java.math.BigDecimal;
import java.util.List; // 导入 List

/**
 * 课程信息实体类 (Course)
 * 对应数据库中的 Course 表，存储课程的基本信息，如名称、学分和所属专业。
 * 扩展以包含选修此课程的学生列表。
 */
public class Course {

    private Integer courseId;       // 课程的唯一标识符，主键 (对应数据库中的 course_id)
    private String courseName;  // 课程名称，不能为空
    private BigDecimal credit;      // 课程的学分，不能为空
    private String courseTeacher;
    private Integer majorId;        // 该课程所属专业的ID (外键关联 Major 表)，用于区分专业课和选修课
    private String majorName;       // 专业名称 (通过JOIN获取)

    private List<Enrollment> enrollments; // 选修此课程的学生列表 (包含成绩等信息)

    // 默认构造函数
    public Course() {
    }

    // 包含基本字段的构造函数 (enrollments 通常在获取详情时单独填充)
    public Course(Integer courseId, String courseName, BigDecimal credit, Integer majorId, String courseTeacher, String majorName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.majorId = majorId;
        this.courseTeacher = courseTeacher;
        this.majorName = majorName;
    }

    // Getter 和 Setter 方法
    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }

    public String getCourseTeacher() { return courseTeacher; }

    public void setCourseTeacher(String courseTeacher) { this.courseTeacher = courseTeacher; }

    public String getMajorName() { return majorName; }

    public void setMajorName(String majorName) { this.majorName = majorName; }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", credit=" + credit +
                ", majorId=" + majorId +
                ", courseTeacher='" + courseTeacher + '\'' +
                ", majorName='" + majorName + '\'' +
                ", enrollmentsCount=" + (enrollments != null ? enrollments.size() : 0) +
                '}';
    }
}
