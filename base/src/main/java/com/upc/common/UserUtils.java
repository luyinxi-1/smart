package com.upc.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserUtils {
    private static final ThreadLocal<UserInfoToRedis> LOCAL = new ThreadLocal<>();

    public static void set(UserInfoToRedis data) {
        LOCAL.set(data);
    }

    public static UserInfoToRedis get() {
        return LOCAL.get();
    }

    public static void clear() {
        LOCAL.remove();
    }
}
