package com.superinka.formex.service;

import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.UserCourseRepository;
import com.superinka.formex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        Long students = userRepository.countActiveStudents();
        Long courses = courseRepository.countByEnabledTrue();
        Long income = userCourseRepository.sumPaidIncome();

        if (income == null) income = 0L;

        stats.put("students", students);
        stats.put("studentsGrowth", "—");

        stats.put("courses", courses);
        stats.put("coursesGrowth", "—");

        stats.put("income", income);
        stats.put("incomeGrowth", "—");

        return stats;
    }
}
