# Propuesta de Dominio

## Sistema elegido

**SisGanado v1.0 - Control Sanitario y de Costos para Ganado**, para **Finca
San Isidro**, dedicada a la crianza y venta de ganado vacuno para carne.

Actualmente la finca lleva sus campañas de vacunación y desparasitación en
cuadernos, lo que causa pérdida de información, animales no protegidos y
cálculos imprecisos de costos por jornada sanitaria. SisGanado resuelve esto
permitiendo registrar animales por lotes, programar jornadas sanitarias,
calcular dosis exactas según el peso del ganado, rastrear inventario de
productos veterinarios y generar reportes de costos por jornada.

## Actores

1. **Administrador de finca:** acceso total. Crea usuarios, define productos y
   proveedores, autoriza jornadas.
2. **Ganadero/Operario:** registra animales y lotes, ejecuta aplicaciones
   sanitarias, reporta existencias.
3. **Veterinario (consultor):** consulta el estado sanitario de los lotes,
   recomienda productos, valida planes.
4. **Contador/Gestor administrativo:** revisa costos por jornada, genera
   reportes de gastos, reconcilia con proveedores.

## Entidades de negocio

1. **Animal**
   *Propósito:* Registro individual de cada animal de la finca.
   *Datos principales:* ID, nombre/código, raza, peso, sexo, fecha de
   nacimiento, lote actual, estado sanitario.

2. **Lote**
   *Propósito:* Agrupar animales para campañas sanitarias en bloque.
   *Datos principales:* ID, nombre, cantidad de animales, descripción, estado
   (activo/cerrado).

3. **ProductoVeterinario**
   *Propósito:* Catálogo de medicamentos, vacunas y antiparasitarios
   disponibles.
   *Datos principales:* ID, nombre, principio activo, dosis por kg,
   presentación (ml/vial), proveedor, precio unitario.

4. **Inventario**
   *Propósito:* Control de existencias, vencimientos y reorden.
   *Datos principales:* ID, producto, cantidad disponible, lote de compra,
   fecha de vencimiento, cantidad mínima para alerta.

5. **JornadaSanitaria**
   *Propósito:* Registro de cada campaña de vacunación/desparasitación con
   fecha, lote y costos.
   *Datos principales:* ID, fecha, lote, tipo (vacunación/desparasitación),
   estado (planificada/ejecutada), costo total, costo promedio por animal.

6. **AplicacionSanitaria**
   *Propósito:* Detalle de cada aplicación dentro de una jornada (qué producto
   a qué animal).
   *Datos principales:* ID, jornada, animal, producto, dosis calculada (ml),
   fecha/hora aplicación, operario.

7. **PlanSanitario**
   *Propósito:* Calendario anual de vacunaciones obligatorias según protocolo.
   *Datos principales:* ID, nombre, productos requeridos, frecuencia, próxima
   fecha sugerida.

8. **Proveedor**
   *Propósito:* Registro de distribuidores de productos veterinarios.
   *Datos principales:* ID, nombre, contacto, productos suministrados, plazo
   de entrega.

9. **Compra**
   *Propósito:* Registro de adquisiciones para reabastecer inventario.
   *Datos principales:* ID, proveedor, fecha, productos y cantidades, total,
   fecha de recepción.

10. **Usuario**
    *Propósito:* Control de acceso por rol.
    *Datos principales:* ID, nombre, email, contraseña (hasheada), rol,
    activo/inactivo.

11. **AlternativaDeProducto**
    *Propósito:* Almacenar alternativas de marcas/productos equivalentes para
    el mismo propósito sanitario, con sus precios actuales del mercado.
    *Datos principales:* ID, producto original, marca alternativa, precio
    actual, proveedor alternativo, eficacia (%), disponibilidad, última
    actualización.

12. **PresupuestoJornada**
    *Propósito:* Registro de presupuestos estimados ANTES de ejecutar una
    jornada, con opciones de optimización sugeridas.
    *Datos principales:* ID, jornada, costo estimado (opción actual), costo
    optimizado (mejor precio), ahorro potencial, alternativas sugeridas,
    estado (estimado/ejecutado).

Con estas doce entidades se supera el mínimo requerido de seis entidades
relacionadas.

## Relaciones entre entidades

- Un **Lote** agrupa muchos **Animales**.
- Un **Animal** pertenece a un **Lote** y participa en muchas
  **AplicacionesSanitarias**.
- Una **JornadaSanitaria** es de un **Lote** y contiene muchas
  **AplicacionesSanitarias**.
- Una **JornadaSanitaria** tiene un **PresupuestoJornada** (estimación de
  costos antes de ejecutar).
- Una **AplicacionSanitaria** registra la aplicación de un
  **ProductoVeterinario** a un **Animal** en una **JornadaSanitaria**.
- Un **PlanSanitario** define qué **ProductosVeterinarios** se deben usar y
  con qué frecuencia.
- Un **ProductoVeterinario** tiene muchas **AlternativasDeProducto** (opciones
  de mercado más económicas).
- Un **ProductoVeterinario** viene de un **Proveedor** y se controla en
  **Inventario**.
- Una **Compra** es de un **Proveedor** y reabastece **Inventario**.
- Un **Usuario** con un rol (admin, ganadero, vet, contador) ejecuta acciones
  en el sistema.

## Subdominio documental candidato

**Historial y Bitácora:** cada **JornadaSanitaria** y **AplicacionSanitaria**
deberá generar registros auditables (quién, cuándo, qué). Se considerará usar
MongoDB (Laboratorio 2) para almacenar:
- Logs de operaciones por usuario.
- Historial de cambios en precios de productos.
- Observaciones y notas del veterinario (formato flexible, sin estructura
  fija).
- Reportes PDF generados.

## Procesos de negocio

### Proceso 1: Planificación y Ejecución de Jornada Sanitaria

**Descripción:** el ganadero consulta el plan sanitario, crea una jornada,
calcula dosis según peso, aplica productos y registra costos.

**Pasos:**
1. Seleccionar un **Lote** y consultar el **PlanSanitario** (ej: vacunación
   antiaftosa).
2. Verificar que hay suficiente producto en **Inventario** (VALIDACIÓN:
   cantidad disponible ≥ cantidad requerida).
3. Calcular dosis total: peso promedio del lote × dosis por kg del producto
   (CÁLCULO).
4. Si hay déficit: crear aviso de reorden automático (REGLA).
5. Registrar la **JornadaSanitaria** como "planificada".
6. En el día de ejecución, registrar cada **AplicacionSanitaria** (animal,
   producto, dosis, hora).
7. Validar que todos los animales del lote fueron tratados (VALIDACIÓN:
   cantidad aplicaciones = animales en lote).
8. Marcar jornada como "ejecutada" y actualizar **Inventario** (reducir
   existencias).

**Reglas:**
- No se puede ejecutar una jornada si hay déficit de producto.
- Cada animal solo se aplica una vez por jornada.
- La dosis se ajusta al peso individual si la información existe.

**Cálculos:**
- Dosis total = Suma(dosis por animal) = Suma(peso animal × dosis por kg
  producto).
- Costo de producto = dosis total en ml ÷ contenido por unidad × precio
  unitario.

**Validaciones:**
- Inventario disponible ≥ dosis calculada.
- Producto no vencido.
- Lote activo.
- Usuario autenticado.

### Proceso 2: Cálculo de Costo Total de Jornada Sanitaria ⭐ (TRANSACCIÓN)

**Descripción:** al finalizar una jornada, se calcula el costo total
integrando múltiples rubros (producto, mano de obra, transporte, veterinario)
y se genera un resumen por animal. **Este proceso debe ser transaccional: o
se registran todos los costos y se actualiza inventario, o nada.**

**Pasos:**
1. Recopilar datos de la **JornadaSanitaria** ejecutada: animales tratados,
   producto usado, dosis.
2. Calcular costos componentes (CÁLCULOS):
   - **Costo de producto** = (dosis total en ml ÷ ml por unidad) × precio
     unitario.
   - **Costos adicionales** = mano de obra + transporte + honorarios
     veterinario + otros gastos.
   - **Costo total** = costo producto + costos adicionales.
   - **Costo promedio por animal** = costo total ÷ cantidad de animales en
     lote.
3. Actualizar el registro de **JornadaSanitaria** con los totales
   (ESCRITURA 1).
4. Decrementar **Inventario** del producto usado (ESCRITURA 2).
5. Crear un registro en la **Bitácora** (documento flexible) con detalles de
   costos, operario y timestamp (ESCRITURA 3).
6. Generar resumen y devolverlo al usuario.

**Reglas:**
- No se puede procesar si la jornada no está marcada como "ejecutada".
- Los costos adicionales deben ser mayores o iguales a cero.
- Si algún cálculo falla (ej: división por cero), se revierte toda la
  operación.

**Cálculos:**
- Dosis total (ml) = Suma(peso animal × dosis por kg).
- Unidades consumidas = dosis total ÷ contenido ml por unidad (redondeado
  hacia arriba).
- Costo producto = unidades consumidas × precio unitario.
- Costo total = costo producto + (mano obra + transporte + vet + otros).
- Costo por animal = costo total ÷ cantidad animales.

**Validaciones:**
- Jornada está en estado "ejecutada".
- Cantidad animales > 0 (no división por cero).
- Inventario tiene suficiente cantidad para decrementar.
- Todos los valores de entrada son positivos.

**Por qué es transacción:** si el cálculo de costo falla a mitad de camino o
una escritura se interrumpe, no queremos quedarnos con inventario decrementado
pero sin registro de costo, o viceversa. O se procesan las 3 escrituras
juntas, o ninguna.

### Proceso 3: Presupuesto y Asistencia Inteligente de Optimización de Costos

**Descripción:** ANTES de ejecutar la campaña, el sistema genera un
presupuesto estimado e identifica alternativas más económicas usando un
asistente inteligente que compara marcas y precios del mercado, recomendando
ahorros sin comprometer la eficacia sanitaria.

**Pasos:**
1. El ganadero selecciona un **Lote** y un **PlanSanitario** (ej:
   desparasitación en mes 6).
2. El sistema consulta el **ProductoVeterinario** actualmente asignado y su
   precio en **Inventario**.
3. **Cálculo del presupuesto actual (CÁLCULO 1):**
   - Dosis total (ml) = Suma(peso animal × dosis por kg).
   - Unidades requeridas = dosis total ÷ contenido ml por unidad (redondeo
     arriba).
   - Costo producto actual = unidades × precio unitario actual.
   - Costo total estimado (opción 1) = costo producto + costos adicionales
     (mano obra, vet, transporte fijos).
4. **Búsqueda de alternativas (Asistente inteligente):**
   - El sistema consulta **AlternativaDeProducto** para el producto
     seleccionado.
   - Filtra opciones con eficacia ≥ 95% (REGLA: no bajar calidad).
   - Ordena por precio ascendente.
   - Calcula el costo total con cada alternativa (CÁLCULO 2).
5. **Comparativa y recomendación (CÁLCULO 3):**
   - Genera tabla: alternativa | precio unitario | costo total | ahorro $ |
     ahorro %.
   - Resalta la opción con mejor relación costo-eficacia.
   - Calcula: ahorro potencial = costo actual - costo optimizado.
6. Crear registro **PresupuestoJornada** con ambas opciones (actual vs.
   optimizada).
7. Presentar reporte al ganadero/contador con sugerencias accionables.
8. Opcionalmente, el asistente sugiere cambiar proveedor o negociar mejor
   precio con el actual.

**Reglas:**
- No se recomiendan productos con eficacia < 95% (margen de seguridad
  sanitaria).
- Las alternativas deben venir de proveedores con plazo de entrega ≤ 3 días.
- No se recomienda un producto si su inventario disponible es < dosis
  requerida.
- El asistente solo sugiere opciones con ahorro verificable (≥ 5% de
  descuento).

**Cálculos:**
- **Costo opción actual** = (dosis total ÷ ml por unidad) × precio actual +
  costos adicionales.
- **Costo opción alternativa** = (dosis total ÷ ml por unidad) × precio
  alternativa + costos adicionales.
- **Ahorro por unidad** = precio actual - precio alternativa.
- **Ahorro total** = ahorro por unidad × cantidad unidades = (precio actual -
  precio alternativa) × (dosis total ÷ ml por unidad).
- **% ahorro** = (costo actual - costo alternativa) ÷ costo actual × 100.

**Validaciones:**
- Lote activo y con animales.
- Plan sanitario válido.
- Productos con eficacia documentada ≥ 95%.
- Fecha de presupuesto debe ser ≤ 7 días antes de ejecución (validación:
  relevancia temporal).
- Precios de alternativas no mayores a 30% por encima del actual (validación:
  sanidad económica).

**Salida (Reporte de Presupuesto):**
```json
{
  "idPresupuesto": "PRE-2024-001",
  "lote": "Lote A (50 animales)",
  "plan": "Desparasitación Q6",
  "dosis_total_ml": 200,
  "fecha_sugerida": "2024-09-15",
  "opcion_actual": {
    "producto": "Paramax Plus (Proveedor X)",
    "precio_unitario": 15000,
    "cantidad_unidades": 20,
    "costo_producto": 300000,
    "costos_adicionales": 80000,
    "costo_total": 380000,
    "costo_por_animal": 7600
  },
  "alternativas_recomendadas": [
    {
      "posicion": 1,
      "producto": "Parasitol Forte (Proveedor Y)",
      "precio_unitario": 12500,
      "eficacia": "98%",
      "cantidad_unidades": 20,
      "costo_producto": 250000,
      "costo_total": 330000,
      "ahorro_total": 50000,
      "ahorro_porcentaje": "13.2%",
      "plazo_entrega": "2 días",
      "recomendacion": "✓ Mejor relación costo-eficacia"
    },
    {
      "posicion": 2,
      "producto": "Antihelmix Vet (Proveedor Z)",
      "precio_unitario": 11000,
      "eficacia": "96%",
      "cantidad_unidades": 20,
      "costo_producto": 220000,
      "costo_total": 300000,
      "ahorro_total": 80000,
      "ahorro_porcentaje": "21.1%",
      "plazo_entrega": "5 días",
      "recomendacion": "⚠️ Máximo ahorro, pero plazo más largo"
    }
  ],
  "ahorro_maximo_potencial": 80000,
  "asistente_sugerencia": "Negociar con Proveedor X una reducción de 10% en Paramax Plus (podrían competir con Parasitol Forte). Esto preservaría la relación comercial actual ahorrando ₡38,000."
}
```

## Alcance

### Dentro del alcance (Lo que SÍ construiremos)

- Registro y consulta de animales y lotes.
- Catálogo de productos veterinarios con proveedores.
- Control de inventario: existencias, vencimientos, alertas de bajo stock.
- Programación y ejecución de jornadas sanitarias.
- Cálculo automático de dosis según peso y reglas sanitarias.
- Presupuesto estimado ANTES de ejecutar campaña (nueva funcionalidad).
- Asistente inteligente con alternativas de productos y precios del mercado
  (nueva funcionalidad).
- Cálculo de costos totales y promedio por animal por jornada.
- Comparativa de opciones económicas con recomendaciones de ahorro.
- Consulta de historial de aplicaciones por animal/lote.
- Control de acceso por rol (admin, ganadero, vet, contador).
- API REST con validaciones de negocio.
- Pruebas unitarias e integración.
- Base de datos relacional (JPA/Hibernate en Lab 3).
- Documentos flexibles para bitácora/auditoría (MongoDB en Lab 2).
- Transaccionalidad en procesos críticos (Lab 4).
- Datos de mercado y alternativas de productos (fuente simulada o
  integración básica).

### Fuera del alcance (Lo que NO construiremos)

- App móvil (solo web).
- Facturación electrónica ante Hacienda.
- Integración con sistemas de pago o banca.
- Gestión de ventas de ganado (solo crianza y sanitario).
- Reportes impresos PDF avanzados (solo JSON/HTML básico).
- Sincronización con laboratorios veterinarios externos.
- Machine learning real o predicción con modelos entrenados.
- Geolocalización GPS de animales.
- Notificaciones por SMS/email automáticas.
- Sistema de usuarios y autenticación OAuth/LDAP (solo login local).
- Integración con APIs de proveedores reales (fuente de precios simulada).

**Nota:** el "asistente inteligente" en este contexto es lógica heurística
basada en reglas (eficacia ≥ 95%, plazo ≤ 3 días, precios históricos), NO
machine learning. Ver [ADR-002](adr/ADR-002-presupuesto-y-asistente-inteligente.md).