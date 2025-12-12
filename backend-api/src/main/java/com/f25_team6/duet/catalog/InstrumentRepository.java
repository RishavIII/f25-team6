package com.f25_team6.duet.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    java.util.Optional<Instrument> findByName(String name);
}
