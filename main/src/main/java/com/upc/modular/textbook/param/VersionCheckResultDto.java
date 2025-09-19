package com.upc.modular.textbook.param;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 版本检查结果的数据传输对象 (DTO)
 */
@Data // 核心注解：自动生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // 生成无参构造函数
@AllArgsConstructor // 生成全参构造函数
@JsonInclude(JsonInclude.Include.NON_NULL) // JSON序列化时忽略null值的字段
public class VersionCheckResultDto {
    /**
     * 教材ID
     */
    private Long textbookId;

    /**
     * 检查状态:
     * - UNAVAILABLE: 资格不符（未发布或未审查）
     * - MATCH: 版本一致
     * - MISMATCH: 版本不一致
     */
    private String status;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 服务器端的最新版本号 (仅在 status 为 MISMATCH 时提供)
     */
    private String serverVersion;
}

