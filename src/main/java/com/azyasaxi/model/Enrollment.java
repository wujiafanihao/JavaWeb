package com.azyasaxi.model;

/**
 * 选课记录及成绩实体类 (Enrollment)
 * 对应数据库中的 Enrollment 表，记录了学生选修课程以及对应的成绩。
 */
public class Enrollment {

    private int enrollmentId; // 选课记录的唯一标识符，主键 (对应数据库中的 enrollment_id)
    private int studentId;    // 选课学生的ID (外键关联 Student 表)
    private int courseId;     // 所选课程的ID (外键关联 Course 表)
    private Double grade;       // 学生在该课程获得的成绩，可以为空 (对应数据库中的 grade DECIMAL(5,2))
                              // 使用 Double 类型以允许成绩为空 (null)

    // 默认构造函数
    public Enrollment() {
    }

    // 包含所有字段的构造函数
    public Enrollment(int enrollmentId, int studentId, int courseId, Double grade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.grade = grade;
    }

    // Getter 和 Setter 方法
    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", grade=" + grade +
                '}';
    }
}
