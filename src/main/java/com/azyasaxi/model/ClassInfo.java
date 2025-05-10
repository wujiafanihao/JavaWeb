package com.azyasaxi.model;

import java.util.List; // 导入 List 用于学生列表

/**
 * 班级信息实体类 (ClassInfo)
 * 对应数据库中的 ClassInfo 表，存储班级的基本信息，并可扩展包含班级下的学生列表。
 */
public class ClassInfo {

    private int classId;        // 班级的唯一标识符，主键 (对应数据库中的 class_id)
    private String className;     // 班级名称，不能为空
    private Integer majorId;      // 该班级所属专业的ID (外键关联 Major 表)，使用 Integer 以允许 null
    private String majorName;     // 该班级所属专业的名称 (通过 JOIN 查询得到)
    private Integer collegeId;    // 该班级所属学院的ID (通过 JOIN 查询得到)
    private String collegeName;   // 该班级所属学院名称 (通过 JOIN 查询得到)

    // 新增字段：用于存储该班级下的所有学生
    private List<Student> studentsInClass; // 学生列表

    // 默认构造函数
    public ClassInfo() {
    }

    // 包含所有基本字段的构造函数 (不包括学生列表，学生列表通常在需要时填充)
    public ClassInfo(int classId, String className, Integer majorId, String majorName, Integer collegeId, String collegeName) {
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

    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
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

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    // Getter 和 Setter for studentsInClass
    public List<Student> getStudentsInClass() {
        return studentsInClass;
    }

    public void setStudentsInClass(List<Student> studentsInClass) {
        this.studentsInClass = studentsInClass;
    }

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
                ", studentsInClassCount=" + (studentsInClass != null ? studentsInClass.size() : 0) +
                '}';
    }
}