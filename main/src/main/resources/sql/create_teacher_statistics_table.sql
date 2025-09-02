-- 创建教师统计表
CREATE TABLE teacher_statistics (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    class_count INTEGER DEFAULT 0 COMMENT '授课班级数量',
    student_count INTEGER DEFAULT 0 COMMENT '授课人数',
    textbook_count INTEGER DEFAULT 0 COMMENT '教材数量',
    course_count INTEGER DEFAULT 0 COMMENT '授课课程数量',
    statistics_date TIMESTAMP COMMENT '统计时间',
    creator BIGINT COMMENT '创建人',
    add_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    operator BIGINT COMMENT '操作人',
    operation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
);

-- 添加注释
COMMENT ON TABLE teacher_statistics IS '教师统计信息表';
COMMENT ON COLUMN teacher_statistics.id IS '主键ID';
COMMENT ON COLUMN teacher_statistics.teacher_id IS '教师ID';
COMMENT ON COLUMN teacher_statistics.class_count IS '授课班级数量';
COMMENT ON COLUMN teacher_statistics.student_count IS '授课人数';
COMMENT ON COLUMN teacher_statistics.textbook_count IS '教材数量';
COMMENT ON COLUMN teacher_statistics.course_count IS '授课课程数量';
COMMENT ON COLUMN teacher_statistics.statistics_date IS '统计时间';
COMMENT ON COLUMN teacher_statistics.creator IS '创建人';
COMMENT ON COLUMN teacher_statistics.add_datetime IS '创建时间';
COMMENT ON COLUMN teacher_statistics.operator IS '操作人';
COMMENT ON COLUMN teacher_statistics.operation_datetime IS '操作时间';

-- 创建索引
CREATE INDEX idx_teacher_statistics_teacher_id ON teacher_statistics(teacher_id);
CREATE INDEX idx_teacher_statistics_statistics_date ON teacher_statistics(statistics_date);
CREATE INDEX idx_teacher_statistics_creator ON teacher_statistics(creator);

-- 添加外键约束（如果需要的话，根据实际业务情况调整）
-- ALTER TABLE teacher_statistics ADD CONSTRAINT fk_teacher_statistics_teacher_id 
--     FOREIGN KEY (teacher_id) REFERENCES teacher(id);
-- ALTER TABLE teacher_statistics ADD CONSTRAINT fk_teacher_statistics_creator 
--     FOREIGN KEY (creator) REFERENCES sys_tbuser(id); 