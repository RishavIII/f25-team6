package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
	long countByTutor_Id(Long tutorId);

	long countByTutor_IdAndStatus(Long tutorId, BookingStatus status);

	void deleteByStudent_Id(Long studentId);

	void deleteByTutor_Id(Long tutorId);

	java.util.List<BookingRequest> findByTutor_IdAndStatus(Long tutorId, BookingStatus status);

	java.util.List<BookingRequest> findByTutor_Id(Long tutorId);
}
