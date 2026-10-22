package cr.ac.una.eif509.demo.business;

import java.util.List;

public record PlanSanitarioSalida(Long id, String nombre, String descripcion,
                                  Integer edadObjetivoMeses, Integer intervaloMinimoDias,
                                  List<Long> productosIds) { }
