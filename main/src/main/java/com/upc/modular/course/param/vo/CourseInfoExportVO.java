package com.upc.modular.course.param.vo;

import lombok.Data;
import java.util.List;

@Data
public class CourseInfoExportVO {
    private String courseName;
    private String courseIntro;

    private String teacherName;
    private String teacherContact;

    // 旧字段：保留兼容（如果只导一个班级时可继续用）
    private String className;
    private String institutionName;
    private List<StudentInfo> students;

    private List<TextbookInfo> textbooks;

    // 新增：多班级导出用
    private List<ClassSection> classSections;

    @Data
    public static class ClassSection {
        private Long classId;
        private String className;
        private String institutionName;
        private List<StudentInfo> students;
    }

    @Data
    public static class TextbookInfo {
        private String textbookName;
        private String authorName;
    }

    @Data
    public static class StudentInfo {
        private String className;
        private String studentName;
        private String studentNo;
        private String phone;
    }
}