package com.upc.common.utils;

import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;

public enum FileType {
    // 常见的文件类型定义
    //  图像文件
    JPEG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),
    BMP("image/bmp"),
    TIFF("image/tiff"),
    SVG("image/svg+xml"),
    // 文档文件
    PDF("application/pdf"),
    DOC("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLS("application/vnd.ms-excel"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PPT("application/vnd.ms-powerpoint"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    // 文本文件
    TXT("text/plain"),
    HTML("text/html"),
    CSS("text/css"),
    JS("application/javascript"),
    // 音频文件
    MP3("audio/mpeg"),
    WAV("audio/wav"),
    // 视频文件
    MP4("video/mp4"),
    WEBM("video/webm"),
    // 压缩文件
    ZIP("application/zip"),
    GZIP("application/gzip"),
    TAR("application/x-tar"),
    RAR("application/x-rar-compressed"),
    // 数据文件
    JSON("application/json"),
    XML("application/xml"),
    YAML("application/x-yaml"),
    // 3D 模型文件
    STL("application/sla"),
    OBJ("application/octet-stream"),
    FBX("application/x-fbx"),
    PLY("application/polygon"),
    GLTF("model/gltf+json"),
    GLB("model/gltf-binary"),
    COLLADA("application/vnd.collada+xml"),
    X3D("model/x3d+xml"),
    AMF("application/amf+xml");

    private String mimeType;

    FileType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static boolean isValidFileType(String contentType) {
        for (FileType fileType : FileType.values()) {
            if (fileType.getMimeType().equals(contentType)) {
                return true;
            }
        }
        return false;
    }
}
