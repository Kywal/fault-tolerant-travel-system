package br.ufrn.imd.fidelity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.ufrn.imd.fidelity.fault.FaultSimulator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({FidelityController.class, FaultSimulator.class})
class FidelityControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvSource({
            "joao.silva, 450",
            "maria.santos, 1200",
            "pedro.oliveira, 750",
            "ana.costa, 300",
            "carlos.ferreira, 2000",

            // Tests with differents user name formats
            "user.with.dots, 100",
            "user_with_underscores, 200",
            "user-with-dashes, 300",
            "user123, 400",
            "UserWithCapitals, 500",

            // Tests with differents bonus numbers
            "test.user, 0",
            "test.user, 1",
            "test.user, 9999",
            "test.user, 50000"
    })
    void shouldRegisterBonusSuccessfully(String user, int bonus) throws Exception {
        String bonusRequestJson = String.format("""
            {
                "user": "%s",
                "bonus": %d
            }
            """, user, bonus);

        mockMvc.perform(post("/bonus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bonusRequestJson))
                .andExpect(status().isOk());
    }

}