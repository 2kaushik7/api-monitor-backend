package com.apimonitor.ambackend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apimonitor.ambackend.model.Alert;
import com.apimonitor.ambackend.model.Endpoint;
import com.apimonitor.ambackend.repository.AlertRepository;
import com.apimonitor.ambackend.repository.EndpointRepository;

@ExtendWith(MockitoExtension.class)
class EndpointServiceImplTest {

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private EndpointServiceImpl endpointService;

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

    // --- getAllApis ---

    @Test
    void getAllApis_returnsUrlList() {
        when(endpointRepository.findAll()).thenReturn(List.of(endpoint));

        List<String> result = endpointService.getAllApis();

        assertThat(result).containsExactly("https://example.com/api");
    }

    @Test
    void getAllApis_returnsEmptyList_whenNoEndpoints() {
        when(endpointRepository.findAll()).thenReturn(List.of());

        List<String> result = endpointService.getAllApis();

        assertThat(result).isEmpty();
    }

    // --- getAllEndpoints ---

    @Test
    void getAllEndpoints_returnsList() {
        when(endpointRepository.findAll()).thenReturn(List.of(endpoint));

        List<Endpoint> result = endpointService.getAllEndpoints();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).isEqualTo("https://example.com/api");
    }

    @Test
    void getAllEndpoints_returnsEmptyList_whenNoEndpoints() {
        when(endpointRepository.findAll()).thenReturn(List.of());

        List<Endpoint> result = endpointService.getAllEndpoints();

        assertThat(result).isEmpty();
    }

    // --- addEndpoint ---

    @Test
    void addEndpoint_savesAndReturnsEndpoint() {
        when(endpointRepository.save(endpoint)).thenReturn(endpoint);

        Endpoint result = endpointService.addEndpoint(endpoint);

        assertThat(result).isEqualTo(endpoint);
        verify(endpointRepository, times(1)).save(endpoint);
    }

    // --- deleteEndpoint ---

    @Test
    void deleteEndpoint_callsDeleteById() {
        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));

        endpointService.deleteEndpoint(endpointId);

        verify(alertRepository, times(1)).deleteByEndpoint(endpoint);
        verify(endpointRepository, times(1)).delete(endpoint);
    }

    // --- updateEndpoint ---

    @Test
    void updateEndpoint_updatesAndReturnsEndpoint() {
        Endpoint update = new Endpoint();
        update.setUrl("https://new-url.com/api");
        update.setStatus("DOWN");

        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(endpoint);

        Endpoint result = endpointService.updateEndpoint(endpointId, update);

        assertThat(result.getUrl()).isEqualTo("https://new-url.com/api");
        assertThat(result.getStatus()).isEqualTo("DOWN");
        verify(endpointRepository, times(1)).save(endpoint);
    }

    @Test
    void updateEndpoint_throwsException_whenEndpointNotFound() {
        UUID unknownId = UUID.randomUUID();
        Endpoint update = new Endpoint();
        update.setUrl("https://new-url.com/api");
        update.setStatus("DOWN");

        when(endpointRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> endpointService.updateEndpoint(unknownId, update))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Endpoint not found: " + unknownId);

        verify(endpointRepository, never()).save(any());
    }
}
