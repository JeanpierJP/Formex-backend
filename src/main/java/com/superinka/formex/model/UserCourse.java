package com.superinka.formex.model;

import com.superinka.formex.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCourse {

    @EmbeddedId
    private UserCourseId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    // 🎓 Gamificación
    @Column(name = "certificate_issued", nullable = false)
    private Boolean certificateIssued = false;


}
