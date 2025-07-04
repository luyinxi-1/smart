package com.upc.codeGenerator;

/**
 * 代码生成器的主启动类。注意: 使用后要更改配置，以防生成的表被覆盖
 *
 * @author xth
 */
public class CodeGenerator {

    public static void main(String[] args) {
//        作者信息
        String author = "byh";

//        模块名
        String moduleName = "main";

//        表名
        String[] tables = {
                "teaching_question","student_exercises_content","student_exercises_record","questions_banks_list","teaching_question_bank"
        };

        new CodeGeneratorConfig(author, moduleName, tables).generate();
    }
}
