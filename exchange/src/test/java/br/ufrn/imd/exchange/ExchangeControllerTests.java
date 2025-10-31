package br.ufrn.imd.exchange;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ExchangeController.class)
class ExchangeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnConversionRateSuccessfully() throws Exception {
        mockMvc.perform(get("/convert"))
                .andExpect(status().isOk());
    }

    @RepeatedTest(10)
    void shouldReturnConversionRateBetween5And6() throws Exception {
        String response = mockMvc.perform(get("/convert"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Double rate = objectMapper.readValue(response, Double.class);

        Assertions.assertNotNull(rate);
        Assertions.assertTrue(rate >= 5.0, "Rate should be >= 5.0, but was: " + rate);
        Assertions.assertTrue(rate <= 6.0, "Rate should be < 6.0, but was: " + rate);
    }

    @RepeatedTest(10)
    void shouldReturnRateWithMaxTwoDecimalPlaces() throws Exception {
        String response = mockMvc.perform(get("/convert"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Double rate = objectMapper.readValue(response, Double.class);
        String rateString = rate.toString();

        int decimalPlaces = 0;
        if (rateString.contains(".")) {
            decimalPlaces = rateString.split("\\.")[1].length();
        }

        Assertions.assertTrue(decimalPlaces <= 2,
                "Rate should have at most 2 decimal places, but had " + decimalPlaces + ": " + rate);
    }
}