package com.azyasaxi.model;

/**
 * 学生基本信息实体类 (Student)
 * 对应数据库中的 Student 表，存储学生的基本信息、班级关联以及登录认证信息。
 */
public class Student {

    private int studentId;      // 学生的唯一标识符，主键 (对应数据库中的 student_id)
    private String name;        // 学生姓名，不能为空
    private String gender;      // 学生性别 (例如: "男", "女")，不能为空
    private Integer classId;     // 学生所属班级的ID (外键关联 ClassInfo 表)，可以为空
                                // 使用 Integer 类型以允许班级ID为空 (null)
    private String userName;    // 学生登录用户名，唯一且不能为空 (对应数据库中的 username)
    private String password;    // 学生登录密码，不能为空
    private String className;   // 学生所属班级的名称 (通过 JOIN 查询得到)

    // 默认构造函数
    public Student() {
    }

    // 包含所有字段的构造函数 (包括 className)
    public Student(int studentId, String name, String gender, Integer classId, String userName, String password, String className) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.classId = classId;
        this.userName = userName;
        this.password = password;
        this.className = className;
    }

    // Getter 和 Setter 方法
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    // toString 方法 (方便调试)
    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", classId=" + classId +
                ", className='" + className + '\'' + // 添加 className
                ", userName='" + userName + '\'' +
                // 注意：通常不在 toString 中直接打印密码
                ", password='[PROTECTED]'" +
                '}';
    }
}
