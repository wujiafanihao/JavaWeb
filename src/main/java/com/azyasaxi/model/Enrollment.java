package com.azyasaxi.model;

import java.math.BigDecimal;

/**
 * 选课记录及成绩实体类 (Enrollment)
 * 对应数据库中的 Enrollment 表，记录了学生选修课程以及对应的成绩。
 */
public class Enrollment {

    private int enrollmentId; // 选课记录的唯一标识符，主键 (对应数据库中的 enrollment_id)
    private int studentId;    // 选课学生的ID (外键关联 Student 表)
    private int courseId;     // 所选课程的ID (外键关联 Course 表)
    private Course course;      // 关联的课程对象 (包含课程名、教师、学分等)
    private Student student;    // 关联的学生对象 (包含学生姓名、班级等基本信息)
    private BigDecimal grade;   // 学生在该课程获得的成绩，可以为空

    // 默认构造函数
    public Enrollment() {
    }

    // 包含所有字段的构造函数
    public Enrollment(int enrollmentId, int studentId, int courseId, Course course, Student student, BigDecimal grade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.course = course;
        this.student = student; // 初始化 student 字段
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

    public Course  getCourse() { return course; }

    public void setCourse(Course courseName) { this.course = courseName; }

    public BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                (student != null ? ", studentName='" + student.getName() + '\'' : "") + // 包含学生姓名
                ", courseId=" + courseId +
                (course != null ? ", courseName='" + course.getCourseName() + '\'' : "") + // 包含课程名称
                ", grade=" + grade +
                '}';
    }
}
