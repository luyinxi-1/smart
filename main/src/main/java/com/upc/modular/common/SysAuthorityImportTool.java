package com.upc.modular.common;

import org.springframework.stereotype.Component;

/**
 * 快速导入所有的权限信息到数据库（注意：不使用时注释掉该方法！）
 *
 * 结合sql语句：
 * INSERT INTO role_authority_list (role_id, authority_id)
 * SELECT
 *     1 AS role_id,               -- 设置 role_id
 *     id AS authority_id
 * FROM sys_authority;
 *
 * @Author: xth
 * @Date: 2025/7/11 11:21
 */
@Component
public class SysAuthorityImportTool {
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private SysAuthorityServiceImpl sysAuthorityService; // 用于保存权限数据
//
//    @PostConstruct
//    public void init() {
//        // 获取所有的 @RestController 或 @Controller 注解的类
//        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
//
//        for (Object controller : controllers.values()) {
//            Class<?> controllerClass = controller.getClass();
//            String classPath = getClassPath(controllerClass);  // 获取类的路径（如 /institution）
//
//            // 判断是否已经存在该权限路径，如果不存在，则插入
//            if (sysAuthorityService.findSysAuthorityIdByUrl(classPath) == null) {
//                // 保存 Controller 类路径作为父节点
//                sysAuthorityService.saveSysAuthority(classPath, classPath, 0L);
//            }
//
//            // 获取 Controller 中的所有方法
//            Method[] methods = controllerClass.getDeclaredMethods();
//            for (Method method : methods) {
//                String methodPath = getMethodPath(method);  // 获取方法路径
//
//                if (methodPath != null && !methodPath.isEmpty()) {
//                    // 此时只保存方法路径部分，而不是拼接类路径
//                    Long parentId = sysAuthorityService.findSysAuthorityIdByUrl(classPath);
//
//                    // 保存接口路径作为子节点，父ID为类路径的 ID
//                    sysAuthorityService.saveSysAuthority(methodPath, method.getName(), parentId);
//                }
//            }
//        }
//    }
//
//    // 获取 Controller 类的路径（通常是 @RequestMapping 或 @GetMapping 等）
//    private String getClassPath(Class<?> controllerClass) {
//        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
//        return requestMapping != null ? requestMapping.value()[0] : "";
//    }
//
//    // 获取方法的路径（通过 @RequestMapping、@GetMapping、@PostMapping 等注解）
//    private String getMethodPath(Method method) {
//        String methodPath = "";
//        if (method.isAnnotationPresent(RequestMapping.class)) {
//            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
//            methodPath = requestMapping.value()[0];
//        } else if (method.isAnnotationPresent(GetMapping.class)) {
//            GetMapping getMapping = method.getAnnotation(GetMapping.class);
//            methodPath = getMapping.value()[0];
//        } else if (method.isAnnotationPresent(PostMapping.class)) {
//            PostMapping postMapping = method.getAnnotation(PostMapping.class);
//            methodPath = postMapping.value()[0];
//        }
//        return methodPath;
//    }
}

