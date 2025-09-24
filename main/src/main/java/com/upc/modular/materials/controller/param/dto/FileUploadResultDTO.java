package com.upc.modular.materials.controller.param.dto;
import lombok.Data; // 使用Lombok可以自动生成getter, setter, toString等方法
import java.util.List;
/**
 * 文件上传成功后的返回结果封装对象
 */
@Data
public class FileUploadResultDTO {

    /**
     * 所有成功上传的文件的完整访问路径列表。
     * 例如: ["upload/public/image/file1.jpg", "upload/public/image/file2.png"]
     */
    private List<String> filePaths;

    /**
     * 对于图集（imageSet）这类由多个文件组成的素材，这个字段表示它们所在的父目录的路径。
     * 对于单文件上传，这个字段可以为 null 或空。
     * 例如: "upload/public/imageSet/20250923/some_uuid_5"
     */
    private String directoryPath;

    /**
     * 所有上传文件的总大小，单位是 MB。
     * 例如: 2.5
     */
    private Double totalSizeMB;

}