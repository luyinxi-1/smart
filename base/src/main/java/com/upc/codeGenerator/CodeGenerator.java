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
        String moduleName = "base";

//        表名
        String[] tables = {
                "sys_user","sys_authority","sys_role","role_authority_list","user_role_list","teaching_material","sys_dict_type","sys_dict_item","sys_log"
        };

        new CodeGeneratorConfig(author, moduleName, tables).generate();
    }
}
