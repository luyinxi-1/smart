package com.upc.exception;

public class BusinessException extends RuntimeException{

    private final CommonError commonError;
    // 新增字段来记录错误计数，例如重复数据的数量
    private int errorCount;
    /**
     * 直接使用CommonError实现枚举类来初始化BusinessException
     *
     * @param commonError CommonError对象
     */
    public BusinessException(CommonError commonError) {
        super(commonError.getErrMsg());
        this.commonError = commonError;
    }

    /**
     * 使用CommonError实现枚举类来初始化BusinessException，同时对errMsg进行拼接
     *
     * @param commonError CommonError对象
     * @param errMsg      待拼接的内容
     */
    public BusinessException(CommonError commonError, String errMsg) {
        super(commonError.getErrMsg() + errMsg);
        this.commonError = commonError;
    }

    public CommonError getCommonError() {
        return commonError;
    }

    /**
     * 用于初始化BusinessException时包含自定义消息和错误计数
     *
     * @param commonError CommonError对象
     * @param errMsg 自定义错误消息
     * @param errorCount 错误计数，例如重复数据的数量
     */
    public BusinessException(CommonError commonError, String errMsg, int errorCount) {
        super(commonError.getErrMsg() + errMsg);
        this.commonError = commonError;
        this.errorCount = errorCount;
    }

    /**
     * 用于获取错误计数
     * @return 错误计数
     */
    public int getErrorCount() {
        return errorCount;
    }


}
