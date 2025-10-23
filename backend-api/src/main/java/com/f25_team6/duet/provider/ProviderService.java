package com.f25_team6.duet.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderService {

    private final ProviderRepository repository;

    public Provider create(Provider incoming) {
        normalize(incoming);

        if (repository.existsByEmailIgnoreCase(incoming.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email already in use.");
        }
        if (incoming.getPasswordHash() == null || incoming.getPasswordHash().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "passwordHash is required.");
        }

        return repository.save(incoming);
    }

    @Transactional(readOnly = true)
    public Provider get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Provider not found."));
    }

    @Transactional(readOnly = true)
    public List<Provider> list() {
        return repository.findAll();
    }

    public Provider update(Long id, Provider incoming) {
        Provider cur = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Provider not found."));

        if (incoming.getEmail() != null) {
            String newEmail = normalizeEmail(incoming.getEmail());
            if (!newEmail.equalsIgnoreCase(cur.getEmail())
                    && repository.existsByEmailIgnoreCase(newEmail)) {
                throw new ResponseStatusException(CONFLICT, "Email already in use.");
            }
            cur.setEmail(newEmail);
        }

        if (incoming.getPasswordHash() != null && !incoming.getPasswordHash().isBlank()) {
            cur.setPasswordHash(incoming.getPasswordHash());
        }

        if (incoming.getName() != null)     cur.setName(incoming.getName());
        if (incoming.getBio() != null)      cur.setBio(incoming.getBio());
        if (incoming.getPhotoUrl() != null) cur.setPhotoUrl(incoming.getPhotoUrl());

        if (incoming.getInstruments() != null) cur.setInstruments(incoming.getInstruments());
        if (incoming.getGenres() != null)      cur.setGenres(incoming.getGenres());
        if (incoming.getSkillLevel() != null)  cur.setSkillLevel(incoming.getSkillLevel());

        cur.setEmailVerified(incoming.isEmailVerified());
        cur.setTeachesOnline(incoming.isTeachesOnline());
        cur.setTeachesInPerson(incoming.isTeachesInPerson());

        if (incoming.getTravelRadiusKm() != null) cur.setTravelRadiusKm(incoming.getTravelRadiusKm());

        if (incoming.getAddress() != null)    cur.setAddress(incoming.getAddress());
        if (incoming.getCity() != null)       cur.setCity(incoming.getCity());
        if (incoming.getState() != null)      cur.setState(incoming.getState());
        if (incoming.getPostalCode() != null) cur.setPostalCode(incoming.getPostalCode());
        if (incoming.getCountry() != null)    cur.setCountry(incoming.getCountry());

        if (incoming.getLatitude() != null)   cur.setLatitude(incoming.getLatitude());
        if (incoming.getLongitude() != null)  cur.setLongitude(incoming.getLongitude());

        if (incoming.getHourlyRate() != null) cur.setHourlyRate(incoming.getHourlyRate());

        return cur; 
    }

    // 
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Provider not found.");
        }
        repository.deleteById(id);
    }

    private void normalize(Provider p) {
        p.setEmail(normalizeEmail(p.getEmail()));
        if (p.getName() != null) p.setName(p.getName().trim());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
