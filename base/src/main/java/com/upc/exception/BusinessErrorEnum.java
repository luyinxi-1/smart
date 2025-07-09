package com.upc.exception;

import java.util.List;

public enum BusinessErrorEnum implements CommonError {

    /**
     * 参数不合法
     */
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    /**
     * 未知错误
     */
    UNKNOWN_ERROR(10002, "未知错误"),
    /**
     * 有相同权限点
     */
    SAME_AUTHORITY(10003, "有相同权限点"),
    /**
     * 没有此权限
     */
    NOT_PERMISSIONS(10005, "没有此权限"),
    /**
     * 参数不能为空
     */
    IS_EMPTY(10006, "参数不能为空"),
    /**
     * 手机号已被占用
     */
    PHONE_NUMBER_OCCUPY(10007, "手机号被占有"),
    /**
     * 账号不存在
     */
    STUDENT_NOT_EXIST(20001, "账号不存在"),
    /**
     * 账号或密码错误
     */
    LOGIN_FAIL(20002, "账号或密码错误，登录失败！"),
    /**
     * 用户的身份不正确，请检查后重试
     */
    USER_NO(20003, "用户的身份不正确，请检查后重试"),
    /**
     * 用户未登录
     */
    PLEASE_LOGIN(20004, "用户未登录"),
    /**
     * 用户已存在
     */
    USER_IS_EXIST(20005, "账号已存在"),
    /**
     * openId获取失败
     */
    OPENID_IS_NULL(20006, "openId获取失败"),
    /**
     * 用户未绑定
     */
    UN_BOUND(20007, "用户未绑定"),
    /**
     * token失效,请重新登录
     */
    TOKEN_IS_INVALID(20008, "登陆超时"),
    /**
     * 账号状态不为1
     */
    ACCOUNT_BANNED(20009, "账号可能处于封禁状态，请联系管理员"),

    /**
     * 数据库错误
     */
    MYSQL_ERR(30001, "数据库更新错误"),

    /**
     * 短信错误
     */
    MESSAGE_ERROR(40001, "短信发送错误"),
    /**
     * excel导入失败
     */
    EXCEL_ERROR(40002,"excel导入失败"),
    /**
     * 不存在
     */
    NO_EXIT(40003,"不存在"), // 修正了 NO_EXIT 的 errMsg
    /**
     * 删除失败，存在绑定关系
     */
    BINDING_ERR(50001,"删除失败，存在绑定关系"),

    /**
     * "存在嵌套循环，请检查权限的父子关系"
     */
    HAS_CYCLE_ERR(50002, "存在嵌套循环，请检查权限的父子关系"),
    /**
     * 业务错误
     */
    BUSINESS_ERROR(50003, "业务处理异常"); // 修正了 BUSINESS_ERROR 的定义，为其提供了 errCode 和 errMsg

    // 枚举的构造函数
    BusinessErrorEnum(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private final int errCode;
    private String errMsg;
    private List<RuntimeException> errList; // 注意：CommonError 接口中是否有 getErrList/setErrList 方法？如果没有，这里可能会有问题

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public List<RuntimeException> getErrList() {
        return this.errList;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }

    @Override
    public CommonError setErrMsg(List<RuntimeException> errMsgList) {
        this.errList = errMsgList;
        return this;
    }

}