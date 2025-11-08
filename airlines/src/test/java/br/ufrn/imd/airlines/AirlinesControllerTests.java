package br.ufrn.imd.airlines;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.ufrn.imd.airlines.fault.FaultSimulator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AirlinesController.class, FaultSimulator.class})
class AirlinesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvSource({
            "LA8084, 2025-11-15",
            "AA1234, 2025-12-01",
            "BA7777, 2025-10-20",
            "UA5555, 2026-01-10"
    })
    void shouldReturnFlightInfoWhenValidParametersProvided(String flight, String day) throws Exception {
        mockMvc.perform(get("/flight")
                        .param("flight", flight)
                        .param("day", day))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flight").value(flight))
                .andExpect(jsonPath("$.day").value(day))
                .andExpect(jsonPath("$.value").value(450.50));
    }

    @ParameterizedTest
    @CsvSource({
            ", 2025-11-15",
            "AA1234,",
            ","
    })
    void flightEndpointShouldReturnBadRequestWhenMissingParameters(String flight, String day) throws Exception {
        mockMvc.perform(get("/flight")
                        .param("flight", flight)
                        .param("day", day))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "LA8084, 2025-11-15",
            "AA9999, 2025-12-25",
            "DL3333, 2026-02-14"
    })
    void shouldSellTicketAndReturnTransactionId(String flight, String day) throws Exception {
        String sellRequestJson = String.format("""
            {
                "flight": "%s",
                "day": "%s"
            }
            """, flight, day);

        mockMvc.perform(post("/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sellRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.transactionId").isNotEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            ", 2025-11-15",
            "AA1234,",
            ","
    })
    void sellEndpointShouldReturnBadRequestWhenMissingParameters(String flight, String day) throws Exception {
        mockMvc.perform(post("/sell")
                        .param("flight", flight)
                        .param("day", day))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGenerateDifferentTransactionIdsForMultipleSells() throws Exception {
        String sellRequestJson = """
            {
                "flight": "LA8084",
                "day": "2025-11-15"
            }
            """;

        String firstTransactionId = mockMvc.perform(post("/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sellRequestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondTransactionId = mockMvc.perform(post("/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sellRequestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertNotEquals(firstTransactionId, secondTransactionId);
    }
}