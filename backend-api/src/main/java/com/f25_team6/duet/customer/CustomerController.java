package com.f25_team6.duet.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    public ResponseEntity<Customer> create(
            @Valid @RequestBody Customer incoming,
            UriComponentsBuilder uriBuilder
    ) {
        Customer created = service.create(incoming);
        return ResponseEntity
                .created(uriBuilder.path("/api/customers/{id}").buildAndExpand(created.getId()).toUri())
                .body(created);
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public Page<Customer> list(Pageable pageable) {
        return service.list(pageable);
    }

    @PutMapping("/{id}")
    public Customer put(@PathVariable Long id, @Valid @RequestBody Customer incoming) {
        return service.update(id, incoming);
    }

    @PatchMapping("/{id}")
    public Customer patch(@PathVariable Long id, @RequestBody Customer incoming) {
        return service.update(id, incoming);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
