package com.example.hubspot.service;

import com.example.hubspot.dto.ContactDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final WebClient hubSpotWebClient;

    // --- 2. OPCIONÁLIS FELADAT: LISTÁZÁS ---
    public JsonNode getAllContacts() {
        return hubSpotWebClient.get()
                .uri("/crm/v3/objects/contacts?properties=email,firstname,lastname")
                .retrieve()
                .onStatus(status -> status.value() == 429, response -> {
                    log.warn("Rate limit (429) elérve!");
                    return Mono.error(new RuntimeException("Rate limit reached"));
                })
                .bodyToMono(JsonNode.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable.getMessage().contains("Rate limit")))
                .block();
    }

    // --- 1. KÖTELEZŐ FELADAT: KERESÉS (SEARCH) ---
    public Optional<String> findContactIdByEmail(String email) {
        String body = """
            {
                "filterGroups": [
                    {
                        "filters": [
                            {
                                "propertyName": "email",
                                "operator": "EQ",
                                "value": "%s"
                            }
                        ]
                    }
                ]
            }
            """.formatted(email);

        return hubSpotWebClient.post()
                .uri("/crm/v3/objects/contacts/search")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() == 429, response -> {
                    log.warn("Rate limit (429) a keresésnél!");
                    return Mono.error(new RuntimeException("Rate limit reached"));
                })
                .bodyToMono(JsonNode.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable.getMessage().contains("Rate limit")))
                .map(node -> {
                    if (node.has("results") && node.get("total").asInt() > 0) {
                        return node.get("results").get(0).get("id").asText();
                    }
                    return "";
                })
                .blockOptional()
                .filter(id -> !id.isEmpty());
    }

    // --- 1. KÖTELEZŐ FELADAT: LÉTREHOZÁS (CREATE) ---
    public String createContact(ContactDto request) {
        Map<String, Object> body = Map.of("properties", Map.of(
                "email", request.getEmail(),
                "firstname", request.getFirstname(),
                "lastname", request.getLastname()
        ));

        return hubSpotWebClient.post()
                .uri("/crm/v3/objects/contacts")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() == 429, response -> {
                    log.warn("Rate limit (429) a létrehozásnál!");
                    return Mono.error(new RuntimeException("Rate limit reached"));
                })
                .bodyToMono(JsonNode.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable.getMessage().contains("Rate limit")))
                .map(node -> node.get("id").asText())
                .block();
    }

    // --- 1. KÖTELEZŐ FELADAT: FRISSÍTÉS (UPDATE) ---
    public void updateContact(String contactId, ContactDto request) {
        Map<String, Object> body = Map.of("properties", Map.of(
                "firstname", request.getFirstname(),
                "lastname", request.getLastname()
        ));

        hubSpotWebClient.patch()
                .uri("/crm/v3/objects/contacts/" + contactId)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() == 429, response -> {
                    log.warn("Rate limit (429) a frissítésnél!");
                    return Mono.error(new RuntimeException("Rate limit reached"));
                })
                .toBodilessEntity()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable.getMessage().contains("Rate limit")))
                .block();
    }

    // --- FŐ LOGIKA: UPSERT ---
    public Map<String, String> upsertContact(ContactDto request) {
        Optional<String> existingId = findContactIdByEmail(request.getEmail());

        if (existingId.isPresent()) {
            updateContact(existingId.get(), request);
            return Map.of("id", existingId.get(), "action", "UPDATED");
        } else {
            String newId = createContact(request);
            return Map.of("id", newId, "action", "CREATED");
        }
    }
}