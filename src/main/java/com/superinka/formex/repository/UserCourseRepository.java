package com.superinka.formex.repository;

import com.superinka.formex.model.UserCourse;
import com.superinka.formex.model.UserCourseId;
import com.superinka.formex.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserCourseRepository
        extends JpaRepository<UserCourse, UserCourseId> {

    List<UserCourse> findByUserIdAndPaymentStatus(
            Long userId,
            PaymentStatus paymentStatus
    );

    List<UserCourse> findByCourse_Id(Long courseId);
    List<UserCourse> findByUser_Id(Long userId);
    @Query("""
    SELECT COALESCE(SUM(c.price), 0)
    FROM UserCourse uc
    JOIN uc.course c
    WHERE uc.paymentStatus = com.superinka.formex.model.enums.PaymentStatus.PAID
""")
    Long sumPaidIncome();

}


