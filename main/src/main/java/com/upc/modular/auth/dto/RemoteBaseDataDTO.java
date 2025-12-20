package com.upc.modular.auth.dto;

import lombok.Data;

import java.util.Collections;
import java.util.List;

//A和B的DTO
@Data
public class RemoteBaseDataDTO {
    private List<RemoteStudentDTO> students;
    private List<RemoteTeacherDTO> teachers;

    public List<RemoteStudentDTO> safeStudents() {
        return students == null ? Collections.emptyList() : students;
    }

    public List<RemoteTeacherDTO> safeTeachers() {
        return teachers == null ? Collections.emptyList() : teachers;
    }
}

