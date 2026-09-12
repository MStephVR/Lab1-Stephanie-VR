# ADR-002 · Presupuesto pre-campaña y Asistente Inteligente de Optimización

**Estado:** Aceptada · **Fecha:** 12/08/2026 · **Responsables:** María Stephanie Vargas Ramírez

## Contexto
El sistema debe permitir que el ganadero calcule el costo de una campaña sanitaria ANTES de ejecutarla, y además, necesita sugerencias de optimización: qué marcas alternativas podrían usar para ahorrar dinero sin comprometer la calidad sanitaria. Este es un requisito clave para el negocio: las ganancias en ganadería son estrechas, y cada ahorro en medicamentos impacta directamente la rentabilidad.

Sin embargo, el equipo es pequeño (1 persona) y el tiempo es limitado (un ciclo académico). Implementar un motor de recomendación sofisticado o depender de APIs externas en tiempo real sería inviable en este plazo. Se necesitaba algo pragmático pero útil.

## Decisión
El asistente:
1. Genera un presupuesto estimado ANTES de ejecutar la campaña (nuevas entidades: `PresupuestoJornada` y `AlternativaDeProducto`).
2. Compara el producto actual con alternativas catalogadas localmente (no APIs externas).
3. Aplica filtros de negocio: solo recomienda si eficacia ≥ 95%, plazo de entrega ≤ 3 días, ahorro ≥ 5%.
4. Devuelve un reporte JSON con tabla comparativa: producto actual vs. alternativas + ahorro potencial.

La "inteligencia" es lógica determinista basada en criterios claros, no predicción.

## Alternativas consideradas

1. **Motor predictivo con dependencias externas:**
   - Generar predicciones de mejores productos.

2. **Integración con APIs de proveedores reales (HTTP calls a sistemas externos):**
   -  Descartado: proveedores no exponen APIs públicas para esto; requeriría negociaciones comerciales, manejo de errores de conectividad, costos adicionales. La fuente de datos sería frágil e impredecible en un entorno educativo.

3. **Asistente basado en reglas:**
   - Almacenar alternativas en base de datos local (`AlternativaDeProducto`).
   - Aplicar reglas de negocio simples y claras.
   - Admin puede cargar/actualizar alternativas manualmente desde un CSV o admin panel.


## Consecuencias

### Positivas
- **Pragmatismo:** funciona en el plazo del curso con recursos limitados.
- **Transparencia:** las reglas son explícitas y documentadas; el ganadero entiende por qué se sugiere un producto.
- **Mantenibilidad:** sin APIs externas ni modelos predictivos que mantener.
- **Testabilidad:** lógica determinista, fácil de probar con casos de uso.
- **Ahorro real:** el ganadero ve sugerencias inmediatas (presupuesto antes de comprar).

### Negativas
- **Escalabilidad limitada:** requiere que el admin ingrese manualmente las alternativas y precios. Si hay 100 productos con 5 alternativas cada uno, es trabajo considerable.
- **Actualización manual:** los precios en la tabla `AlternativaDeProducto` se quedarán obsoletos. Necesitaría un job schedulado o integración manual cada cierto tiempo.
- **No aprende:** no captura patrones históricos. Si hace 2 meses la alternativa X era barata y hoy es cara, el sistema no lo sabe.

### Neutras
- Se trabaja con lógica de negocio pura, sin dependencias externas ni overhead de conectividad.
- Futura mejora evidente: si en Laboratorio 5+ hay presupuesto, se puede integrar una API REST de precios o un job batch que sincronice.

## Referencias
- Requisito del negocio: "quiero saber el costo antes de actuar".
- Restricción del curso: 1 ciclo, equipo de 1 persona.
- Laboratorio 2: MongoDB para bitácoras (documentos flexibles de precios históricos).
