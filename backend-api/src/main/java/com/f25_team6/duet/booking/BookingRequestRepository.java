package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
	long countByTutor_Id(Long tutorId);
	long countByTutor_IdAndStatus(Long tutorId, BookingStatus status);
}
