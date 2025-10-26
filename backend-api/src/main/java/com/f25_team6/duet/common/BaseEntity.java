package com.f25_team6.duet.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
public abstract class BaseEntity {
  @CreationTimestamp
  @Column(nullable=false, updatable=false)
  protected OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable=false)
  protected OffsetDateTime updatedAt;
}

