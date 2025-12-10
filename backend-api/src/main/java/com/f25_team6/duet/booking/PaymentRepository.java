package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByLesson_Tutor_IdAndStatusAndCreatedAtBetween(Long tutorId, PaymentStatus status, OffsetDateTime start, OffsetDateTime end);
	List<Payment> findTop50ByLesson_Tutor_IdOrderByCreatedAtDesc(Long tutorId);
}
