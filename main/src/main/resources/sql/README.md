# 数据库表创建脚本

## 问题描述

当运行应用时出现以下错误：
```
org.springframework.jdbc.BadSqlGrammarException: 
### Error updating database.  Cause: com.kingbase8.util.KSQLException: ERROR: 关系 "teacher_statistics" 不存在
```

## 解决方案

### 1. 执行SQL脚本创建表

在KingBase数据库中执行以下SQL脚本：

```sql
-- 执行 create_teacher_statistics_table.sql 文件中的内容
```

### 2. 表结构说明

`teacher_statistics` 表包含以下字段：

| 字段名 | 类型 | 说明 | 是否必填 |
|--------|------|------|----------|
| id | BIGSERIAL | 主键ID，自增 | 是 |
| teacher_id | BIGINT | 教师ID | 是 |
| class_count | INTEGER | 授课班级数量 | 否，默认0 |
| student_count | INTEGER | 授课人数 | 否，默认0 |
| textbook_count | INTEGER | 教材数量 | 否，默认0 |
| course_count | INTEGER | 授课课程数量 | 否，默认0 |
| statistics_date | TIMESTAMP | 统计时间 | 否 |
| creator | BIGINT | 创建人 | 否 |
| add_datetime | TIMESTAMP | 创建时间 | 否，默认当前时间 |
| operator | BIGINT | 操作人 | 否 |
| operation_datetime | TIMESTAMP | 操作时间 | 否，默认当前时间 |

### 3. 索引说明

脚本会自动创建以下索引以提高查询性能：
- `idx_teacher_statistics_teacher_id`: 教师ID索引
- `idx_teacher_statistics_statistics_date`: 统计时间索引  
- `idx_teacher_statistics_creator`: 创建人索引

### 4. 注意事项

1. 确保在KingBase数据库中执行此脚本
2. 执行前请备份数据库
3. 外键约束已注释，可根据实际业务需求启用
4. 表名使用下划线命名法，符合项目规范

### 5. 验证

执行脚本后，可以通过以下SQL验证表是否创建成功：

```sql
-- 查看表结构
\d teacher_statistics

-- 查看表是否存在
SELECT table_name FROM information_schema.tables WHERE table_name = 'teacher_statistics';
```

## 相关文件

- `create_teacher_statistics_table.sql`: 创建表的SQL脚本
- `TeacherStatistics.java`: 对应的实体类
- `TeacherStatisticsMapper.java`: 对应的Mapper接口
- `TeacherStatisticsMapper.xml`: 对应的XML映射文件 