package com.upc.modular.auth.client;

import com.upc.modular.auth.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RemoteBaseDataClient {

    private final RestTemplate restTemplate;

    @Value("${remote.base-data.host}")
    private String baseHost;

    @Value("${remote.base-data.unified-path:/api/base-data}")
    private String unifiedPath;

    @Value("${remote.base-data.student-path:/api/students}")
    private String studentPath;

    @Value("${remote.base-data.teacher-path:/api/teachers}")
    private String teacherPath;

    /**
     * A / B / C / SPLIT
     */
    @Value("${remote.base-data.mode:A}")
    private String mode;

    // ===== 对外兼容：仍然提供这两个方法 =====
    public List<RemoteStudentDTO> getAllStudents() {
        return fetchAllBaseData().safeStudents();
    }

    public List<RemoteTeacherDTO> getAllTeachers() {
        return fetchAllBaseData().safeTeachers();
    }

    /**
     * 一次拉取学生+教师（按 mode 解析 A/B/C 或走老 SPLIT）
     */
    public RemoteBaseDataDTO fetchAllBaseData() {
        RemoteBaseDataMode m = parseMode(mode);

        if (m == RemoteBaseDataMode.SPLIT) {
            return fetchBySplit();
        }

        String url = joinUrl(baseHost, unifiedPath);

        switch (m) {
            case A:
                return fetchByA(url);
            case B:
                return fetchByB(url);
            case C:
                return fetchByC(url);
            default:
                return fetchByA(url);
        }
    }

    // ===================== 方案 A =====================
    // { "students": [...], "teachers": [...] }
    private RemoteBaseDataDTO fetchByA(String url) {
        RemoteBaseDataDTO dto = restTemplate.getForObject(url, RemoteBaseDataDTO.class);
        return dto == null ? new RemoteBaseDataDTO() : dto;
    }

    // ===================== 方案 B =====================
    // { "code":200, "message":"ok", "data": { "students":[...], "teachers":[...] } }
    private RemoteBaseDataDTO fetchByB(String url) {
        ResponseEntity<ApiResp<RemoteBaseDataDTO>> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResp<RemoteBaseDataDTO>>() {}
        );

        ApiResp<RemoteBaseDataDTO> body = resp.getBody();
        if (body == null || body.getData() == null) {
            return new RemoteBaseDataDTO();
        }
        return body.getData();
    }

    // ===================== 方案 C =====================
    // [ { "type":"student", ... }, { "type":"teacher", ... } ]
    private RemoteBaseDataDTO fetchByC(String url) {
        ResponseEntity<List<RemotePersonDTO>> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RemotePersonDTO>>() {}
        );

        List<RemotePersonDTO> persons = resp.getBody();
        if (persons == null) persons = Collections.emptyList();

        List<RemoteStudentDTO> students = new ArrayList<>();
        List<RemoteTeacherDTO> teachers = new ArrayList<>();

        for (RemotePersonDTO p : persons) {
            if (p == null || !StringUtils.hasText(p.getType())) continue;

            if ("student".equalsIgnoreCase(p.getType())) {
                RemoteStudentDTO s = new RemoteStudentDTO();
                s.setStudentNo(p.getStudentNo());
                s.setName(p.getName());
                s.setIdCard(p.getIdCard());
                s.setGender(p.getGender());
                s.setCollegeName(p.getCollegeName());
                s.setMajorName(p.getMajorName());
                s.setClassId(p.getClassId());
                s.setUnitId(p.getUnitId());
                s.setEmail(p.getEmail());
                s.setPhone(p.getPhone());
                s.setStatus(p.getStatus());
                students.add(s);

            } else if ("teacher".equalsIgnoreCase(p.getType())) {
                RemoteTeacherDTO t = new RemoteTeacherDTO();
                t.setJobNo(p.getJobNo());
                t.setName(p.getName());
                t.setIdCard(p.getIdCard());
                t.setGender(p.getGender());

                // === 完全按你 RemoteTeacherDTO 字段名来 ===
                t.setNationality(p.getNationality());
                t.setBirthday(p.getBirthday());
                t.setPosition(p.getPosition());
                t.setProfessionalTitle(p.getProfessionalTitle());

                t.setUnitId(p.getUnitId());
                t.setEmail(p.getEmail());
                t.setPhone(p.getPhone());
                t.setStatus(p.getStatus());
                teachers.add(t);
            }
        }

        RemoteBaseDataDTO dto = new RemoteBaseDataDTO();
        dto.setStudents(students);
        dto.setTeachers(teachers);
        return dto;
    }

    // ===================== 老方式 SPLIT =====================
    private RemoteBaseDataDTO fetchBySplit() {
        String stuUrl = joinUrl(baseHost, studentPath);
        String teaUrl = joinUrl(baseHost, teacherPath);

        RemoteStudentDTO[] stuArr = restTemplate.getForObject(stuUrl, RemoteStudentDTO[].class);
        RemoteTeacherDTO[] teaArr = restTemplate.getForObject(teaUrl, RemoteTeacherDTO[].class);

        RemoteBaseDataDTO dto = new RemoteBaseDataDTO();
        dto.setStudents(stuArr == null ? Collections.emptyList() : Arrays.asList(stuArr));
        dto.setTeachers(teaArr == null ? Collections.emptyList() : Arrays.asList(teaArr));
        return dto;
    }

    // ===================== utils =====================
    private RemoteBaseDataMode parseMode(String modeStr) {
        if (!StringUtils.hasText(modeStr)) return RemoteBaseDataMode.A;
        try {
            return RemoteBaseDataMode.valueOf(modeStr.trim().toUpperCase());
        } catch (Exception e) {
            return RemoteBaseDataMode.A;
        }
    }

    private String joinUrl(String host, String path) {
        if (!StringUtils.hasText(host)) return path;
        if (!StringUtils.hasText(path)) return host;

        boolean hostEnds = host.endsWith("/");
        boolean pathStarts = path.startsWith("/");

        if (hostEnds && pathStarts) return host.substring(0, host.length() - 1) + path;
        if (!hostEnds && !pathStarts) return host + "/" + path;
        return host + path;
    }
}