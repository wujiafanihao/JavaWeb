package com.azyasaxi.model;

/**
 * 学院信息实体类 (College)
 * 对应数据库中的 College 表，存储学院的基本信息。
 */
public class College {

    private int collegeId;      // 学院的唯一标识符，主键 (对应数据库中的 college_id)
    private String collegeName; // 学院名称，唯一且不能为空

    // 默认构造函数
    public College() {
    }

    // 包含所有字段的构造函数
    public College(int collegeId, String collegeName) {
        this.collegeId = collegeId;
        this.collegeName = collegeName;
    }

    // Getter 和 Setter 方法
    public int getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(int collegeId) {
        this.collegeId = collegeId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "College{" +
                "collegeId=" + collegeId +
                ", collegeName='" + collegeName + '\'' +
                '}';
    }
}
