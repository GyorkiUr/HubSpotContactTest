package com.example.hubspot.controller;

import com.example.hubspot.dto.ContactRequest;
import com.example.hubspot.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> upsertContact(@Valid @RequestBody ContactRequest request) {
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
}