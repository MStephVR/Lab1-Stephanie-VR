package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class JornadaSanitariaFactory {

    public JornadaSanitariaEntity crearJornada(
            PlanificarJornadaEntrada entrada,
            int cantidadAnimales,
            LoteEntity lote,
            PlanSanitarioEntity plan
    ) {
        return new JornadaSanitariaEntity(
                entrada.fecha(),
                entrada.descripcion(),
                cantidadAnimales,
                BigDecimal.ZERO,
                null,
                lote,
                plan
        );
    }

    public AplicacionSanitariaEntity crearAplicacion(
            AnimalEntity animal,
            ProductoVeterinarioEntity producto,
            JornadaSanitariaEntity jornada,
            LocalDate fecha,
            double dosisMl,
            String observacion
    ) {
        return new AplicacionSanitariaEntity(
                animal,
                producto,
                jornada,
                fecha,
                dosisMl,
                fecha.plusDays(7),
                observacion
        );
    }
}
