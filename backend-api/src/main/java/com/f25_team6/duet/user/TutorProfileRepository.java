package com.f25_team6.duet.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface TutorProfileRepository extends JpaRepository<TutorProfile, Long> {

  @Query("""
    select tp from TutorProfile tp
    left join TutorInstrument ti on ti.tutor.userId = tp.userId
    where (:instrumentId is null or ti.instrument.id = :instrumentId)
      and (:online is null or tp.onlineEnabled = :online)
      and (:inPerson is null or tp.inPersonEnabled = :inPerson)
      and (:maxRate is null or tp.hourlyRateCents <= :maxRate)
    """)
  List<TutorProfile> search(Long instrumentId, Boolean online, Boolean inPerson, Integer maxRate);

  @Modifying
  @Transactional
  @Query("update TutorProfile tp set tp.photoUrl = :url where tp.userId = :userId")
  int updatePhotoUrl(@Param("userId") Long userId, @Param("url") String url);

  @Query("SELECT COUNT(tp) FROM TutorProfile tp WHERE tp.userId = :userId")
  long countByUserId(@Param("userId") Long userId);
}
