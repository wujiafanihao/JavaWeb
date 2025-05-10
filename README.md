# JavaWeb

controller：处理请求，调用 service 层，转发到 JSP；

filter：放登录验证、权限控制等；

model：纯 POJO 实体类；

dao：封装 JDBC 操作，如 insertStudent(), getAllCourses()；

service：写业务逻辑，如“学生选课同时更新学分”；

utils：公用工具，如数据库连接池、字符串处理等；

config：存 Spring 配置（可用注解或 XML）；
