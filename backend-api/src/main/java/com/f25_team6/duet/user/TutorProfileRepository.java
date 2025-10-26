package com.f25_team6.duet.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TutorProfileRepository extends JpaRepository<TutorProfile, Long> {

  @Query("""
    select tp from TutorProfile tp
    join TutorInstrument ti on ti.tutor.userId = tp.userId
    where (:instrumentId is null or ti.instrument.id = :instrumentId)
      and (:online is null or tp.onlineEnabled = :online)
      and (:inPerson is null or tp.inPersonEnabled = :inPerson)
      and (:maxRate is null or tp.hourlyRateCents <= :maxRate)
    """)
  List<TutorProfile> search(Long instrumentId, Boolean online, Boolean inPerson, Integer maxRate);
}
