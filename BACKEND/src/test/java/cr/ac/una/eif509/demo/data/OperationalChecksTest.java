package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OperationalChecksTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private ProductoVeterinarioRepository productoRepository;

    @Test
    void actuatorHealthRespondeUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void inventarioNoPermiteGuardarUnaVersionObsoleta() {
        ProductoVeterinarioEntity producto = productoRepository.saveAndFlush(new ProductoVeterinarioEntity(
                "Producto concurrencia", "Vacuna", "ml", 1.0, 5.0, BigDecimal.valueOf(12.50)));
        InventarioEntity creado = inventarioRepository.saveAndFlush(new InventarioEntity(
                producto, "VERSION-TEST", LocalDate.now().plusDays(30), 10, BigDecimal.valueOf(8.00)));
        InventarioEntity versionObsoleta = inventarioRepository.findById(creado.getId()).orElseThrow();
        InventarioEntity versionActual = inventarioRepository.findById(creado.getId()).orElseThrow();

        versionActual.setCantidadDisponible(9);
        inventarioRepository.saveAndFlush(versionActual);
        versionObsoleta.setCantidadDisponible(8);

        assertThatThrownBy(() -> inventarioRepository.saveAndFlush(versionObsoleta))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(inventarioRepository.findById(creado.getId()).orElseThrow().getCantidadDisponible())
                .isEqualTo(9);
    }
}
