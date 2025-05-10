package com.azyasaxi.model;

/**
 * 专业信息实体类 (Major)
 * 对应数据库中的 Major 表，存储专业的基本信息及其所属学院。
 */
public class Major {

    private int majorId;        // 专业的唯一标识符，主键 (对应数据库中的 major_id)
    private String majorName;   // 专业名称，不能为空
    private Integer collegeId;      // 该专业所属学院的ID (外键关联 College 表)

    // 默认构造函数
    public Major() {
    }

    // 包含所有字段的构造函数
    public Major(int majorId, String majorName, int collegeId) {
        this.majorId = majorId;
        this.majorName = majorName;
        this.collegeId = collegeId;
    }

    // Getter 和 Setter 方法
    public int getMajorId() {
        return majorId;
    }

    public void setMajorId(int majorId) {
        this.majorId = majorId;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public Integer getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Integer collegeId) {
        this.collegeId = collegeId;
    }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "Major{" +
                "majorId=" + majorId +
                ", majorName='" + majorName + '\'' +
                ", collegeId=" + collegeId +
                '}';
    }
}
