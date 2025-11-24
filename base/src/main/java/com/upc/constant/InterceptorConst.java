package com.upc.constant;

/**
 * 拦截其中使用的常量类
 *
 * @author qiutian
 */
public class InterceptorConst {
    /**
     * 拦截器中排除的路径(这里的路径通常都是swagger资源等，自定义的排除路径应写在别的常量中)，这些路径不会经过拦截器
     */
    public static final String[] EXCLUDE_PATH_PATTERNS = {
            "/", "/csrf", "/upload/public/**", "/upload/teaching_materials/**", "/doc.html", "/doc.html/**", "/sys-user/login", "/download/**", "/webjars/**",
            "/swagger-resources/**", "/v2/**", "/swagger-ui.html/**", "/error/**", "/sys-log/**", "/sso/**"
    };

    /**
     * 匹配所有路径
     */
    public static final String ALL_PATH_PATTERN = "/**";

    /**
     * 权限部分（auth模块）路径前缀
     */
    public static final String[] BACK_STAGE_INTERCEPT_PATTERNS = {

    };

    /**
     * 权限部分要放行的路径
     */
    public static final String[] AUTH_PUBLIC_URL = {

    };

    /**
     * 自定义的放行路径
     */
    public static final String[] WEIXIN_PUBLISH_EXCLUDE_PATH_PATTERNS = {
//            自定义的放行路径
            "/sys-user/getUserAuthTree",
            "/textbook-authority/textbookAuthorityJudge",
            "/textbook-authority/textbookAuthorityEditJudge",
            "/student/resetStudentPassword",
            "/textbook-authority/textbookAuthorityEditJudge",
            "/ping"
    };
}