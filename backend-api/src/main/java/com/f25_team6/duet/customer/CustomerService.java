package com.f25_team6.duet.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.List;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository repository;

    public Customer create(Customer incoming) {
        normalize(incoming);

        if (repository.existsByEmailIgnoreCase(incoming.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email already in use.");
        }

        if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
            incoming.setPassword(incoming.getPassword());
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "Password is required.");
        }

        if (incoming.getNotifyByEmail() == null)
            incoming.setNotifyByEmail(true);

        if (incoming.getNotifyBySms() == null)
            incoming.setNotifyBySms(false);

        return repository.save(incoming);
    }

    public Customer get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found."));
    }

    public List<Customer> list() {
        return repository.findAll();
    }

    public Customer update(Long id, Customer incoming) {
        Customer cur = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found."));

        if (incoming.getEmail() != null) {
            String newEmail = normalizeEmail(incoming.getEmail());
            if (!newEmail.equalsIgnoreCase(cur.getEmail())
                    && repository.existsByEmailIgnoreCase(newEmail)) {
                throw new ResponseStatusException(CONFLICT, "Email already in use.");
            }
            cur.setEmail(newEmail);
        }

        if (incoming.getPassword() != null && !incoming.getPassword().isBlank())
            cur.setPassword(incoming.getPassword());

        if (incoming.getName() != null)
            cur.setName(incoming.getName());
        if (incoming.getPhone() != null)
            cur.setPhone(incoming.getPhone());
        if (incoming.getBio() != null)
            cur.setBio(incoming.getBio());

        if (incoming.getNotifyByEmail() != null)
            cur.setNotifyByEmail(incoming.getNotifyByEmail());
        if (incoming.getNotifyBySms() != null)
            cur.setNotifyBySms(incoming.getNotifyBySms());

        if (incoming.getEmailVerified() != null)
            cur.setEmailVerified(incoming.getEmailVerified());

        if (incoming.getAddress() != null)
            cur.setAddress(incoming.getAddress());
        if (incoming.getCity() != null)
            cur.setCity(incoming.getCity());
        if (incoming.getState() != null)
            cur.setState(incoming.getState());
        if (incoming.getPostalCode() != null)
            cur.setPostalCode(incoming.getPostalCode());
        if (incoming.getCountry() != null)
            cur.setCountry(incoming.getCountry());

        if (incoming.getLatitude() != null)
            cur.setLatitude(incoming.getLatitude());
        if (incoming.getLongitude() != null)
            cur.setLongitude(incoming.getLongitude());

        if (incoming.getPreferredInstruments() != null) {
            cur.setPreferredInstruments(incoming.getPreferredInstruments());
        }
        if (incoming.getSkillLevel() != null)
            cur.setSkillLevel(incoming.getSkillLevel());

        return cur;
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Customer not found.");
        }   
        repository.deleteById(id);
    }

    private void normalize(Customer c) {
        c.setEmail(normalizeEmail(c.getEmail()));
        if (c.getName() != null)
            c.setName(c.getName().trim());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
