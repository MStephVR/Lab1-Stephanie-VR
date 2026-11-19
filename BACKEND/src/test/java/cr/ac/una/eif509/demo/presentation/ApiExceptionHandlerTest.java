package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.domain.InventarioEntity;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    void errorInesperadoDevuelveProblemDetailSinExponerElMensajeInterno() throws Exception {
        mockMvc.perform(get("/test/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.type").value("https://sisganado.local/problems/error-interno"))
                .andExpect(jsonPath("$.detail").value(
                        "Ocurrió un error inesperado al procesar la solicitud."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("detalle secreto"))));
    }

    @Test
    void conflictoOptimistaDevuelve409ComoProblemDetail() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type")
                        .value("https://sisganado.local/problems/conflicto-de-concurrencia"));
    }

    @RestController
    static class FailingController {
        @GetMapping("/test/failure")
        String failure() {
            throw new IllegalStateException("detalle secreto");
        }

        @GetMapping("/test/conflict")
        String conflict() {
            throw new ObjectOptimisticLockingFailureException(InventarioEntity.class, 1L);
        }
    }
}
