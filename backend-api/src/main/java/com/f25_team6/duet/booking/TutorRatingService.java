package com.f25_team6.duet.booking;

import com.f25_team6.duet.user.TutorProfile;
import com.f25_team6.duet.user.TutorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TutorRatingService {

  private final TutorProfileRepository tutorProfileRepo;
  private final ReviewRepository reviewRepo;

  @Transactional
  public void applyNewReview(Long tutorUserId, int newRating) {
    TutorProfile tp = tutorProfileRepo.findById(tutorUserId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tutor profile not found"));
    int count = tp.getRatingCount() == null ? 0 : tp.getRatingCount();
    double avg = tp.getRatingAvg() == null ? 0.0 : tp.getRatingAvg();
    double newAvg = ((avg * count) + newRating) / (count + 1);
    tp.setRatingCount(count + 1);
    tp.setRatingAvg(round2(newAvg));
  }

  @Transactional
  public void recomputeFromAllReviews(Long tutorUserId) {
    TutorProfile tp = tutorProfileRepo.findById(tutorUserId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tutor profile not found"));
    long count = reviewRepo.countByTutor_Id(tutorUserId);
    double avg = (count == 0) ? 0.0 : reviewRepo.avgForTutor(tutorUserId);
    tp.setRatingCount((int) count);
    tp.setRatingAvg(round2(avg));
  }

  private double round2(double x) {
    return Math.round(x * 100.0) / 100.0;
  }
}
