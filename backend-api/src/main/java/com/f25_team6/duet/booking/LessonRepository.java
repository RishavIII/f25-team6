package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

	List<Lesson> findTop10ByTutor_IdAndStatusAndStartUtcAfterOrderByStartUtcAsc(Long tutorId, LessonStatus status,
			OffsetDateTime after);

	List<Lesson> findByTutor_Id(Long tutorId);

	List<Lesson> findByTutor_IdAndStartUtcBetween(Long tutorId, OffsetDateTime start, OffsetDateTime end);

	@Query("select count(distinct l.student.id) from Lesson l where l.tutor.id = :tutorId")
	long countDistinctStudentsByTutorId(Long tutorId);

	void deleteByTutor_Id(Long tutorId);

	void deleteByStudent_Id(Long studentId);
}
