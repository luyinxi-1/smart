package com.upc.context;


import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;

/**
 * 获取当前登录用户信息的接口
 *
 * @author xth
 */
public class LoginContextHolder {
    private static final ThreadLocal<Boolean> IS_LOGINED = ThreadLocal.withInitial(() -> false);

    /**
     * 获取登录用户的信息
     *
     * @return 已登陆用户信息
     */
    public static UserInfoToRedis getUserInfoToRedis() {
        return UserUtils.get();
    }

    /**
     * 设置登陆状态
     */
    public static void setLogined(Boolean b) {
        IS_LOGINED.set(b);
    }

    /**
     * 是否登录
     */
    public static Boolean getIsLogined() {
        return IS_LOGINED.get();
    }

    /**
     * 删除值，一定要在使用完毕后调用此方法，否则可能导致内存泄漏！！
     */
    public static void clear() {
       IS_LOGINED.remove();
    }
}
