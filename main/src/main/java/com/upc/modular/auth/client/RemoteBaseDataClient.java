package com.upc.modular.auth.client;
import com.upc.modular.auth.dto.RemoteStudentDTO;
import com.upc.modular.auth.dto.RemoteTeacherDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RemoteBaseDataClient {

    private final RestTemplate restTemplate;

    @Value("${remote.base-data.host}")
    private String baseHost;

    // 这两个路径按对方实际接口改
    private static final String STUDENT_PATH = "/api/students";
    private static final String TEACHER_PATH = "/api/teachers";

    public List<RemoteStudentDTO> getAllStudents() {
        String url = baseHost + STUDENT_PATH;
        RemoteStudentDTO[] arr = restTemplate.getForObject(url, RemoteStudentDTO[].class);
        if (arr == null) return Collections.emptyList();
        return Arrays.asList(arr);
    }

    public List<RemoteTeacherDTO> getAllTeachers() {
        String url = baseHost + TEACHER_PATH;
        RemoteTeacherDTO[] arr = restTemplate.getForObject(url, RemoteTeacherDTO[].class);
        if (arr == null) return Collections.emptyList();
        return Arrays.asList(arr);
    }
}
