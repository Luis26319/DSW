package mx.edu.backendacademico.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class CalendarioClientTest {
    @Test void convierteLaRespuestaDelCalendario() {
        // El servidor simulado intercepta el transporte; esta prueba no llama a Internet.
        var builder = RestClient.builder().baseUrl("https://calendario.example");
        var servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo("https://calendario.example/periodos/2026A"))
            .andRespond(withSuccess("""
                {"clave":"2026A","inicio":"2026-01-12","fin":"2026-06-19"}
                """, MediaType.APPLICATION_JSON));
        var periodo = new CalendarioClient(builder.build()).consultarPeriodo("2026A");
        assertEquals("2026A", periodo.clave());
        assertTrue(periodo.inicio().isBefore(periodo.fin()));
        servidor.verify();
    }

    @Test void un503NoSeConvierteEnUnPeriodoFalso() {
        var builder = RestClient.builder().baseUrl("https://calendario.example");
        var servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo("https://calendario.example/periodos/2026A"))
            .andRespond(withServiceUnavailable());
        assertThrows(RestClientResponseException.class, () ->
            new CalendarioClient(builder.build()).consultarPeriodo("2026A"));
        servidor.verify();
    }
}
