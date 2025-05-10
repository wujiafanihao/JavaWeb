package com.azyasaxi.model;

/**
 * 班级信息实体类 (ClassInfo)
 * 对应数据库中的 ClassInfo 表，存储班级的基本信息。
 */
public class ClassInfo {

    private int classId;    // 班级的唯一标识符，主键 (对应数据库中的 class_id)
    private String className; // 班级名称，不能为空
    private Integer majorId;    // 该班级所属专业的ID (外键关联 Major 表)，使用 Integer 以允许 null
    private String majorName;   // 该班级所属专业的名称 (通过 JOIN 查询得到)
    private Integer collegeId;
    private String collegeName; // 该班级所属学院名称 (通过 JOIN 查询得到)

    // 默认构造函数
    public ClassInfo() {
    }

    // 包含所有字段的构造函数
    public ClassInfo(int classId, String className, Integer majorId, String majorName, Integer  collegeId, String collegeName) {
        this.classId = classId;
        this.className = className;
        this.majorId = majorId;
        this.majorName = majorName;
        this.collegeId = collegeId;
        this.collegeName = collegeName;
    }

    // Getter 和 Setter 方法
    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getMajorId() { // Getter 返回类型也更新为 Integer
        return majorId;
    }

    public void setMajorId(Integer majorId) { // Setter 参数类型保持 Integer
        this.majorId = majorId;
    }

    public String getMajorName() { return majorName; }

    public void setMajorName(String majorName) { this.majorName = majorName; }

    public Integer getCollegeId() { return collegeId; }

    public void setCollegeId(Integer collegeId) { this.collegeId = collegeId; }

    public String getCollegeName() { return collegeName; }

    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "ClassInfo{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", majorId=" + majorId +
                ", majorName='" + majorName + '\'' +
                ", collegeId=" + collegeId +
                ", collegeName='" + collegeName + '\'' +
                '}';
    }
}
