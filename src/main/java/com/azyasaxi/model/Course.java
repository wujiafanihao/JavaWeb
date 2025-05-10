package com.azyasaxi.model;

/**
 * 课程信息实体类 (Course)
 * 对应数据库中的 Course 表，存储课程的基本信息，如名称、学分和所属专业。
 */
public class Course {

    private int courseId;       // 课程的唯一标识符，主键 (对应数据库中的 course_id)
    private String courseName;  // 课程名称，不能为空
    private double credit;      // 课程的学分，不能为空
    private String courseTeacher;
    private int majorId;        // 该课程所属专业的ID (外键关联 Major 表)，用于区分专业课和选修课

    // 默认构造函数
    public Course() {
    }

    // 包含所有字段的构造函数
    public Course(int courseId, String courseName, double credit, int majorId, String courseTeacher) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.majorId = majorId;
        this.courseTeacher = courseTeacher;
    }

    // Getter 和 Setter 方法
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public int getMajorId() {
        return majorId;
    }

    public void setMajorId(int majorId) {
        this.majorId = majorId;
    }

    public String getCourseTeacher() { return courseTeacher; }

    public void setCourseTeacher(String courseTeacher) { this.courseTeacher = courseTeacher; }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", credit=" + credit +
                ", majorId=" + majorId +
                ", courseTeacher='" + courseTeacher + '\'' +
                '}';
    }
}
