package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.AnimalEntrada;
import cr.ac.una.eif509.demo.business.AnimalSalida;
import cr.ac.una.eif509.demo.business.CatalogoService;
import cr.ac.una.eif509.demo.business.LoteEntrada;
import cr.ac.una.eif509.demo.business.LoteSalida;
import cr.ac.una.eif509.demo.business.ProductoEntrada;
import cr.ac.una.eif509.demo.business.ProductoSalida;
import cr.ac.una.eif509.demo.business.InventarioEntrada;
import cr.ac.una.eif509.demo.business.InventarioSalida;
import cr.ac.una.eif509.demo.business.PlanSanitarioEntrada;
import cr.ac.una.eif509.demo.business.PlanSanitarioSalida;
import cr.ac.una.eif509.demo.business.ProveedorEntrada;
import cr.ac.una.eif509.demo.business.ProveedorSalida;
import cr.ac.una.eif509.demo.business.CompraEntrada;
import cr.ac.una.eif509.demo.business.CompraSalida;
import cr.ac.una.eif509.demo.business.UsuarioSalida;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class ApiResourceController {
    private final CatalogoService catalogoService;

    public ApiResourceController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/animales")
    public PagedModel<AnimalSalida> animales(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String raza,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return new PagedModel<>(catalogoService.animales(estado, raza, pageable));
    }

    @GetMapping("/animales/{id}")
    public AnimalSalida animal(@PathVariable Long id) {
        return catalogoService.animal(id);
    }

    @PostMapping("/animales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnimalSalida> crearAnimal(@Valid @RequestBody AnimalEntrada entrada) {
        AnimalSalida salida = catalogoService.crearAnimal(entrada);
        return creado(salida.id(), "/api/v1/animales/{id}", salida);
    }

    @PutMapping("/animales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AnimalSalida actualizarAnimal(@PathVariable Long id, @Valid @RequestBody AnimalEntrada entrada) {
        return catalogoService.actualizarAnimal(id, entrada);
    }

    @DeleteMapping("/animales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarAnimal(@PathVariable Long id) {
        catalogoService.eliminarAnimal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lotes")
    public PagedModel<LoteSalida> lotes(
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return new PagedModel<>(catalogoService.lotes(pageable));
    }

    @GetMapping("/lotes/{id}")
    public LoteSalida lote(@PathVariable Long id) {
        return catalogoService.lote(id);
    }

    @PostMapping("/lotes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoteSalida> crearLote(@Valid @RequestBody LoteEntrada entrada) {
        LoteSalida salida = catalogoService.crearLote(entrada);
        return creado(salida.id(), "/api/v1/lotes/{id}", salida);
    }

    @PutMapping("/lotes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LoteSalida actualizarLote(@PathVariable Long id, @Valid @RequestBody LoteEntrada entrada) {
        return catalogoService.actualizarLote(id, entrada);
    }

    @DeleteMapping("/lotes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarLote(@PathVariable Long id) {
        catalogoService.eliminarLote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/productos")
    public PagedModel<ProductoSalida> productos(
            @RequestParam(required = false) Double precioMinimo,
            @RequestParam(required = false) Double precioMaximo,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return new PagedModel<>(catalogoService.productos(precioMinimo, precioMaximo, pageable));
    }

    @GetMapping("/productos/{id}")
    public ProductoSalida producto(@PathVariable Long id) {
        return catalogoService.producto(id);
    }

    @PostMapping("/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoSalida> crearProducto(@Valid @RequestBody ProductoEntrada entrada) {
        ProductoSalida salida = catalogoService.crearProducto(entrada);
        return creado(salida.id(), "/api/v1/productos/{id}", salida);
    }

    @PutMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoSalida actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoEntrada entrada) {
        return catalogoService.actualizarProducto(id, entrada);
    }

    @DeleteMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        catalogoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inventarios")
    public PagedModel<InventarioSalida> inventarios(
            @ParameterObject @PageableDefault(size = 20, sort = "fechaVencimiento",
                    direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return new PagedModel<>(catalogoService.inventarios(pageable));
    }

    @GetMapping("/inventarios/{id}")
    public InventarioSalida inventario(@PathVariable Long id) {
        return catalogoService.inventario(id);
    }

    @PostMapping("/inventarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventarioSalida> crearInventario(@Valid @RequestBody InventarioEntrada entrada) {
        InventarioSalida salida = catalogoService.crearInventario(entrada);
        return creado(salida.id(), "/api/v1/inventarios/{id}", salida);
    }

    @PutMapping("/inventarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public InventarioSalida actualizarInventario(@PathVariable Long id,
                                                  @Valid @RequestBody InventarioEntrada entrada) {
        return catalogoService.actualizarInventario(id, entrada);
    }

    @DeleteMapping("/inventarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long id) {
        catalogoService.eliminarInventario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/planes-sanitarios")
    public PagedModel<PlanSanitarioSalida> planes(
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return new PagedModel<>(catalogoService.planes(pageable));
    }

    @GetMapping("/planes-sanitarios/{id}")
    public PlanSanitarioSalida plan(@PathVariable Long id) {
        return catalogoService.plan(id);
    }

    @PostMapping("/planes-sanitarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanSanitarioSalida> crearPlan(@Valid @RequestBody PlanSanitarioEntrada entrada) {
        PlanSanitarioSalida salida = catalogoService.crearPlan(entrada);
        return creado(salida.id(), "/api/v1/planes-sanitarios/{id}", salida);
    }

    @PutMapping("/planes-sanitarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlanSanitarioSalida actualizarPlan(@PathVariable Long id,
                                               @Valid @RequestBody PlanSanitarioEntrada entrada) {
        return catalogoService.actualizarPlan(id, entrada);
    }

    @DeleteMapping("/planes-sanitarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarPlan(@PathVariable Long id) {
        catalogoService.eliminarPlan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/proveedores")
    public PagedModel<ProveedorSalida> proveedores(
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return new PagedModel<>(catalogoService.proveedores(pageable));
    }

    @GetMapping("/proveedores/{id}")
    public ProveedorSalida proveedor(@PathVariable Long id) {
        return catalogoService.proveedor(id);
    }

    @PostMapping("/proveedores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProveedorSalida> crearProveedor(@Valid @RequestBody ProveedorEntrada entrada) {
        ProveedorSalida salida = catalogoService.crearProveedor(entrada);
        return creado(salida.id(), "/api/v1/proveedores/{id}", salida);
    }

    @PutMapping("/proveedores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProveedorSalida actualizarProveedor(@PathVariable Long id,
                                                @Valid @RequestBody ProveedorEntrada entrada) {
        return catalogoService.actualizarProveedor(id, entrada);
    }

    @DeleteMapping("/proveedores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Long id) {
        catalogoService.eliminarProveedor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compras")
    public PagedModel<CompraSalida> compras(
            @ParameterObject @PageableDefault(size = 20, sort = "fechaCompra",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new PagedModel<>(catalogoService.compras(pageable));
    }

    @GetMapping("/compras/{id}")
    public CompraSalida compra(@PathVariable Long id) {
        return catalogoService.compra(id);
    }

    @PostMapping("/compras")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompraSalida> crearCompra(@Valid @RequestBody CompraEntrada entrada) {
        CompraSalida salida = catalogoService.crearCompra(entrada);
        return creado(salida.id(), "/api/v1/compras/{id}", salida);
    }

    @PutMapping("/compras/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompraSalida actualizarCompra(@PathVariable Long id, @Valid @RequestBody CompraEntrada entrada) {
        return catalogoService.actualizarCompra(id, entrada);
    }

    @DeleteMapping("/compras/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCompra(@PathVariable Long id) {
        catalogoService.eliminarCompra(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/usuarios")
    public List<UsuarioSalida> usuarios() {
        return catalogoService.usuarios();
    }

    private <T> ResponseEntity<T> creado(Long id, String ruta, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(ruta).buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(body);
    }
}
