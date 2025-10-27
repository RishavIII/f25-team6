// com/f25_team6/duet/catalog/TutorInstrument.java
package com.f25_team6.duet.catalog;

import com.f25_team6.duet.common.enums.Level;
import com.f25_team6.duet.user.TutorProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="tutor_instruments",
  uniqueConstraints=@UniqueConstraint(columnNames={"tutor_user_id","instrument_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TutorInstrument {

  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) @JoinColumn(name="tutor_user_id")
  private TutorProfile tutor;

  @ManyToOne(optional=false) @JoinColumn(name="instrument_id")
  private Instrument instrument;

  @Builder.Default
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private Level minLevel = Level.BEGINNER;

  @Builder.Default
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private Level maxLevel = Level.EXPERT;
}
