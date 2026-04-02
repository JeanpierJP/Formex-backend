package com.superinka.formex.service;

import com.superinka.formex.payload.response.StudentDto;
import java.util.List;

public interface SessionStudentService {

    // alumnos por CURSO
    List<StudentDto> getStudentsForCourse(Long courseId);

    // alumnos por SESIÓN (si lo sigues usando)
    List<StudentDto> getStudentsForSession(Long sessionId);

}
