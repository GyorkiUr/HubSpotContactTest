package com.example.hubspot.controller;

import com.example.hubspot.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    void whenInvalidEmail_thenReturns400() throws Exception {
        String invalidContact = "{\"email\": \"rossz-email\", \"firstname\": \"A\", \"lastname\": \"B\"}";

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidContact))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void whenHubspotUnauthorized_thenReturns401() throws Exception {
        when(contactService.upsertContact(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"test@test.hu\", \"firstname\": \"A\", \"lastname\": \"B\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Érvénytelen HubSpot API kulcs!"));
    }

    @Test
    void whenHubspotForbidden_thenReturns403() throws Exception {
        // 403 szimulálása: nincs jogosultsága a kulcsnak a kért művelethez
        when(contactService.upsertContact(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"test@test.hu\", \"firstname\": \"A\", \"lastname\": \"B\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Nincs jogosultsága a kért művelethez a HubSpotban."));
    }

    @Test
    void whenHubspotRateLimited_thenReturns429() throws Exception {
        // 429 szimulálása: túl sok kérés
        when(contactService.upsertContact(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"test@test.hu\", \"firstname\": \"A\", \"lastname\": \"B\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("HubSpot Rate Limit elérve. Kérjük, várjon!"));
    }

    @Test
    void whenUnexpectedError_thenReturns500() throws Exception {
        // 500 szimulálása: váratlan Runtime hiba (pl. null pointer vagy adatbázis hiba)
        when(contactService.upsertContact(any()))
                .thenThrow(new RuntimeException("Váratlan hiba történt a szerveren"));

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"test@test.hu\", \"firstname\": \"A\", \"lastname\": \"B\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Váratlan hiba történt: Váratlan hiba történt a szerveren"));
    }
}