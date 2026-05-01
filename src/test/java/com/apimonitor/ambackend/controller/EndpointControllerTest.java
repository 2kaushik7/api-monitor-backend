package com.apimonitor.ambackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.apimonitor.ambackend.model.Endpoint;
import com.apimonitor.ambackend.service.EndpointService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EndpointController.class)
class EndpointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EndpointService endpointService;

    @Autowired
    private ObjectMapper objectMapper;

    private Endpoint endpoint;
    private UUID endpointId;

    @BeforeEach
    void setUp() {
        endpointId = UUID.randomUUID();
        endpoint = new Endpoint();
        endpoint.setId(endpointId);
        endpoint.setUrl("https://example.com/api");
        endpoint.setStatus("UP");
        endpoint.setResponse(120L);
    }

    // --- GET /apis/get-apis ---

    @Test
    void getAllApis_returns200WithUrlList() throws Exception {
        when(endpointService.getAllApis()).thenReturn(List.of("https://example.com/api"));

        mockMvc.perform(get("/apis/get-apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("https://example.com/api"));
    }

    @Test
    void getAllApis_returns200WithEmptyList() throws Exception {
        when(endpointService.getAllApis()).thenReturn(List.of());

        mockMvc.perform(get("/apis/get-apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /apis/get-endpoints ---

    @Test
    void getAllEndpoints_returns200WithEndpointList() throws Exception {
        when(endpointService.getAllEndpoints()).thenReturn(List.of(endpoint));

        mockMvc.perform(get("/apis/get-endpoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("https://example.com/api"))
                .andExpect(jsonPath("$[0].status").value("UP"));
    }

    @Test
    void getAllEndpoints_returns200WithEmptyList() throws Exception {
        when(endpointService.getAllEndpoints()).thenReturn(List.of());

        mockMvc.perform(get("/apis/get-endpoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- POST /apis/add-api ---

    @Test
    void addEndpoint_returns200WithSavedEndpoint() throws Exception {
        when(endpointService.addEndpoint(any(Endpoint.class))).thenReturn(endpoint);

        mockMvc.perform(post("/apis/add-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.com/api"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // --- PUT /apis/{id} ---

    @Test
    void updateEndpoint_returns200WithUpdatedEndpoint() throws Exception {
        Endpoint updated = new Endpoint();
        updated.setId(endpointId);
        updated.setUrl("https://updated.com/api");
        updated.setStatus("DOWN");

        when(endpointService.updateEndpoint(eq(endpointId), any(Endpoint.class))).thenReturn(updated);

        mockMvc.perform(put("/apis/" + endpointId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://updated.com/api"))
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    void updateEndpoint_returns404_whenEndpointNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(endpointService.updateEndpoint(eq(unknownId), any(Endpoint.class)))
                .thenThrow(new RuntimeException("Endpoint not found: " + unknownId));

        mockMvc.perform(put("/apis/" + unknownId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpoint)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /apis/{id} ---

    @Test
    void deleteEndpoint_returns204() throws Exception {
        doNothing().when(endpointService).deleteEndpoint(endpointId);

        mockMvc.perform(delete("/apis/" + endpointId))
                .andExpect(status().isNoContent());
    }
}
