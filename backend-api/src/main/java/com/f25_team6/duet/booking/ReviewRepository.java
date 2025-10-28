package com.f25_team6.duet.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  long countByTutor_Id(Long tutorId);

  @Query("select coalesce(avg(r.rating), 0) from Review r where r.tutor.id = :tutorUserId")
  Double avgForTutor(Long tutorId);
}