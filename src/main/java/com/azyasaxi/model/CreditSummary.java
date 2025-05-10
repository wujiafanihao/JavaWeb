package com.azyasaxi.model;

/**
 * 学分统计视图实体类 (CreditSummary)
 * 对应数据库中的 CreditSummary 视图，用于展示学生修读的总学分。
 */
public class CreditSummary {

    private int studentId; // 学生ID
    private String studentName; // 学生姓名 (对应视图中的 s.name AS student_name)
    private double totalCredits; // 学生获得的总学分 (对应视图中的 SUM(c.credit) AS total_credits)

    // 默认构造函数
    public CreditSummary() {
    }

    // 包含所有字段的构造函数 (可选)
    public CreditSummary(int studentId, String studentName, double totalCredits) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalCredits = totalCredits;
    }

    // Getter 和 Setter 方法
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() { // Getter 方法名更新为 getStudentName
        return studentName;
    }

    public void setStudentName(String studentName) { // Setter 方法名更新为 setStudentName，参数也更新
        this.studentName = studentName;
    }

    public double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(double totalCredits) {
        this.totalCredits = totalCredits;
    }

    // toString 方法 (可选，方便调试)
    @Override
    public String toString() {
        return "CreditSummary{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", totalCredits=" + totalCredits +
                '}';
    }
}
