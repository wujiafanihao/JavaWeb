package com.azyasaxi.model;

import java.math.BigDecimal; // 用于总学分

/**
 * CreditSummary 模型类 (数据传输对象 DTO)
 * 代表从 CreditSummary 视图中查询到的学生学分统计信息。
 * 这个类不是直接对应一个可修改的表，而是用于展示查询结果。
 */
public class CreditSummary {

    private Integer studentId;        // 学生ID
    private String studentName;       // 学生姓名
    private BigDecimal totalCredits;  // 该学生已获得的总学分

    // 默认构造函数
    public CreditSummary() {
    }

    // 包含所有字段的构造函数
    public CreditSummary(Integer studentId, String studentName, BigDecimal totalCredits) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalCredits = totalCredits;
    }

    // Getter 和 Setter 方法
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(BigDecimal totalCredits) {
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