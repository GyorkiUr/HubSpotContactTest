package com.example.hubspot.controller;

import com.example.hubspot.dto.ContactDto;
import com.example.hubspot.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> upsertContact(@Valid @RequestBody ContactDto request) {
        var result = contactService.upsertContact(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<?> listContacts() {
        try {
            var contacts = contactService.getAllContacts();
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Hiba a lista lekérésekor: " + e.getMessage()));
        }
    }

    @GetMapping("/test-error/{code}")
    public void simulateError(@PathVariable int code) throws MethodArgumentNotValidException {
        switch (code) {
            case 400 -> throw new MethodArgumentNotValidException(null, null);
            case 401 -> throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            case 429 -> throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
            case 403 -> throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
            default -> throw new RuntimeException("Ismeretlen hiba történt!");
        }
    }
}