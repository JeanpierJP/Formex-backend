package com.superinka.formex.service;

import com.superinka.formex.model.Course;
import com.superinka.formex.model.InstructorRating;
import com.superinka.formex.model.User;
import com.superinka.formex.payload.request.InstructorRatingRequest;
import com.superinka.formex.repository.CourseRepository;
import com.superinka.formex.repository.InstructorRatingRepository;
import com.superinka.formex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class InstructorRatingService {

    private final InstructorRatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public void createRating(InstructorRatingRequest req, Jwt jwt) {

        String auth0Id = jwt.getSubject();

        User student = userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        User instructor = userRepository.findById(req.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        YearMonth now = YearMonth.now();

        boolean alreadyRated =
                ratingRepository.existsByInstructorIdAndStudentIdAndMonthAndYear(
                        instructor.getId(),
                        student.getId(),
                        now.getMonthValue(),
                        now.getYear()
                );

        if (alreadyRated) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya evaluaste a este instructor este mes"
            );
        }

        InstructorRating rating = new InstructorRating();
        rating.setInstructor(instructor);
        rating.setStudent(student);
        rating.setCourse(course);
        rating.setRating(req.getRating());
        rating.setComment(req.getComment());
        rating.setMonth(now.getMonthValue());
        rating.setYear(now.getYear());

        ratingRepository.save(rating);

        // ⭐ REGLA NUEVA
        if (rating.getRating() >= 4) {
            instructor.setPoints(instructor.getPoints() + 5);
            userRepository.save(instructor);
        }
    }
}


