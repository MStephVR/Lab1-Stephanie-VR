package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import cr.ac.una.eif509.demo.data.AnimalRepository;
import cr.ac.una.eif509.demo.data.CompraRepository;
import cr.ac.una.eif509.demo.data.BusinessSpecifications;
import cr.ac.una.eif509.demo.data.LoteRepository;
import cr.ac.una.eif509.demo.data.InventarioRepository;
import cr.ac.una.eif509.demo.data.PlanSanitarioRepository;
import cr.ac.una.eif509.demo.data.ProveedorRepository;
import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.data.ProductoVeterinarioRepository;
import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.CompraEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import cr.ac.una.eif509.demo.domain.ProveedorEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CatalogoService {
    private final AnimalRepository animalRepository;
    private final LoteRepository loteRepository;
    private final ProductoVeterinarioRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final PlanSanitarioRepository planRepository;
    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;

    public CatalogoService(AnimalRepository animalRepository, LoteRepository loteRepository,
                           ProductoVeterinarioRepository productoRepository,
                           InventarioRepository inventarioRepository,
                           PlanSanitarioRepository planRepository,
                           ProveedorRepository proveedorRepository,
                           CompraRepository compraRepository,
                           UsuarioRepository usuarioRepository) {
        this.animalRepository = animalRepository;
        this.loteRepository = loteRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.planRepository = planRepository;
        this.proveedorRepository = proveedorRepository;
        this.compraRepository = compraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<AnimalSalida> animales(String estado, String raza, Pageable pageable) {
        return animalRepository.findAll(BusinessSpecifications.animalesPorEstadoYRaza(estado, raza), pageable)
                .map(this::animalSalida);
    }

    @Transactional(readOnly = true)
    public AnimalSalida animal(Long id) {
        return animalSalida(animalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El animal")));
    }

    @Transactional
    public AnimalSalida crearAnimal(AnimalEntrada entrada) {
        LoteEntity lote = loteRepository.findById(entrada.loteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El lote"));
        AnimalEntity animal = new AnimalEntity(entrada.codigoArete(), entrada.raza(), entrada.sexo(),
                entrada.pesoKg(), entrada.fechaNacimiento(), entrada.estado(), lote);
        return animalSalida(animalRepository.save(animal));
    }

    @Transactional
    public AnimalSalida actualizarAnimal(Long id, AnimalEntrada entrada) {
        AnimalEntity animal = animalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El animal"));
        LoteEntity lote = loteRepository.findById(entrada.loteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El lote"));
        animal.setCodigoArete(entrada.codigoArete());
        animal.setRaza(entrada.raza());
        animal.setSexo(entrada.sexo());
        animal.setPesoKg(entrada.pesoKg());
        animal.setFechaNacimiento(entrada.fechaNacimiento());
        animal.setEstado(entrada.estado());
        animal.setLote(lote);
        return animalSalida(animalRepository.save(animal));
    }

    @Transactional
    public void eliminarAnimal(Long id) {
        if (!animalRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El animal");
        }
        animalRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<LoteSalida> lotes(Pageable pageable) {
        return loteRepository.findAll(pageable).map(this::loteSalida);
    }

    @Transactional(readOnly = true)
    public LoteSalida lote(Long id) {
        return loteSalida(loteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El lote")));
    }

    @Transactional
    public LoteSalida crearLote(LoteEntrada entrada) {
        LoteEntity lote = new LoteEntity(entrada.nombre(), entrada.ubicacion(), entrada.proposito());
        lote.setActivo(entrada.activo());
        return loteSalida(loteRepository.save(lote));
    }

    @Transactional
    public LoteSalida actualizarLote(Long id, LoteEntrada entrada) {
        LoteEntity lote = loteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El lote"));
        lote.setNombre(entrada.nombre());
        lote.setUbicacion(entrada.ubicacion());
        lote.setProposito(entrada.proposito());
        lote.setActivo(entrada.activo());
        return loteSalida(loteRepository.save(lote));
    }

    @Transactional
    public void eliminarLote(Long id) {
        if (!loteRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El lote");
        }
        loteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProductoSalida> productos(Double precioMinimo, Double precioMaximo, Pageable pageable) {
        return productoRepository.findAll(
                BusinessSpecifications.productosDentroDelRangoDePrecio(precioMinimo, precioMaximo), pageable)
                .map(this::productoSalida);
    }

    @Transactional(readOnly = true)
    public ProductoSalida producto(Long id) {
        return productoSalida(productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto")));
    }

    @Transactional
    public ProductoSalida crearProducto(ProductoEntrada entrada) {
        ProductoVeterinarioEntity producto = new ProductoVeterinarioEntity(
                entrada.nombre(), entrada.tipo(), entrada.unidadMedida(), entrada.dosisMinima(),
                entrada.dosisMaxima(), entrada.precioUnitario(), entrada.contenidoMlPorUnidad());
        return productoSalida(productoRepository.save(producto));
    }

    @Transactional
    public ProductoSalida actualizarProducto(Long id, ProductoEntrada entrada) {
        ProductoVeterinarioEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto"));
        producto.setNombre(entrada.nombre());
        producto.setTipo(entrada.tipo());
        producto.setUnidadMedida(entrada.unidadMedida());
        producto.setDosisMinima(entrada.dosisMinima());
        producto.setDosisMaxima(entrada.dosisMaxima());
        producto.setPrecioUnitario(entrada.precioUnitario());
        producto.setContenidoMlPorUnidad(entrada.contenidoMlPorUnidad());
        return productoSalida(productoRepository.save(producto));
    }

    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El producto");
        }
        productoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<InventarioSalida> inventarios(Pageable pageable) {
        return inventarioRepository.findAll(pageable).map(this::inventarioSalida);
    }

    @Transactional(readOnly = true)
    public InventarioSalida inventario(Long id) {
        return inventarioSalida(inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El inventario")));
    }

    @Transactional
    public InventarioSalida crearInventario(InventarioEntrada entrada) {
        var producto = productoRepository.findById(entrada.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto"));
        return inventarioSalida(inventarioRepository.save(new InventarioEntity(producto,
                entrada.loteFabricacion(), entrada.fechaVencimiento(),
                entrada.cantidadDisponible(), entrada.costoUnitario())));
    }

    @Transactional
    public InventarioSalida actualizarInventario(Long id, InventarioEntrada entrada) {
        InventarioEntity inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El inventario"));
        var producto = productoRepository.findById(entrada.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto"));
        inventario.setProducto(producto);
        inventario.setLoteFabricacion(entrada.loteFabricacion());
        inventario.setFechaVencimiento(entrada.fechaVencimiento());
        inventario.setCantidadDisponible(entrada.cantidadDisponible());
        inventario.setCostoUnitario(entrada.costoUnitario());
        return inventarioSalida(inventarioRepository.save(inventario));
    }

    @Transactional
    public void eliminarInventario(Long id) {
        if (!inventarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El inventario");
        }
        inventarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<PlanSanitarioSalida> planes(Pageable pageable) {
        return planRepository.findAll(pageable).map(this::planSalida);
    }

    @Transactional(readOnly = true)
    public PlanSanitarioSalida plan(Long id) {
        return planSalida(planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El plan sanitario")));
    }

    @Transactional
    public PlanSanitarioSalida crearPlan(PlanSanitarioEntrada entrada) {
        PlanSanitarioEntity plan = new PlanSanitarioEntity(
                entrada.nombre(), entrada.descripcion(), entrada.edadObjetivoMeses());
        plan.getProductos().addAll(obtenerProductos(entrada.productosIds()));
        return planSalida(planRepository.save(plan));
    }

    @Transactional
    public PlanSanitarioSalida actualizarPlan(Long id, PlanSanitarioEntrada entrada) {
        PlanSanitarioEntity plan = planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El plan sanitario"));
        plan.setNombre(entrada.nombre());
        plan.setDescripcion(entrada.descripcion());
        plan.setEdadObjetivoMeses(entrada.edadObjetivoMeses());
        plan.getProductos().clear();
        plan.getProductos().addAll(obtenerProductos(entrada.productosIds()));
        return planSalida(planRepository.save(plan));
    }

    @Transactional
    public void eliminarPlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El plan sanitario");
        }
        planRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProveedorSalida> proveedores(Pageable pageable) {
        return proveedorRepository.findAll(pageable).map(this::proveedorSalida);
    }

    @Transactional(readOnly = true)
    public ProveedorSalida proveedor(Long id) {
        return proveedorSalida(proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor")));
    }

    @Transactional
    public ProveedorSalida crearProveedor(ProveedorEntrada entrada) {
        return proveedorSalida(proveedorRepository.save(
                new ProveedorEntity(entrada.nombre(), entrada.telefono(), entrada.correo())));
    }

    @Transactional
    public ProveedorSalida actualizarProveedor(Long id, ProveedorEntrada entrada) {
        ProveedorEntity proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor"));
        proveedor.setNombre(entrada.nombre());
        proveedor.setTelefono(entrada.telefono());
        proveedor.setCorreo(entrada.correo());
        return proveedorSalida(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void eliminarProveedor(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El proveedor");
        }
        proveedorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<CompraSalida> compras(Pageable pageable) {
        return compraRepository.findAll(pageable).map(this::compraSalida);
    }

    @Transactional(readOnly = true)
    public CompraSalida compra(Long id) {
        return compraSalida(compraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La compra")));
    }

    @Transactional
    public CompraSalida crearCompra(CompraEntrada entrada) {
        ProveedorEntity proveedor = proveedorRepository.findById(entrada.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor"));
        return compraSalida(compraRepository.save(
                new CompraEntity(proveedor, entrada.fechaCompra(), entrada.total())));
    }

    @Transactional
    public CompraSalida actualizarCompra(Long id, CompraEntrada entrada) {
        CompraEntity compra = compraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La compra"));
        ProveedorEntity proveedor = proveedorRepository.findById(entrada.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor"));
        compra.setProveedor(proveedor);
        compra.setFechaCompra(entrada.fechaCompra());
        compra.setTotal(entrada.total());
        return compraSalida(compraRepository.save(compra));
    }

    @Transactional
    public void eliminarCompra(Long id) {
        if (!compraRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("La compra");
        }
        compraRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UsuarioSalida> usuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> new UsuarioSalida(usuario.getId(), usuario.getUsername(),
                        usuario.getNombreCompleto(), usuario.getRol(), usuario.getActivo()))
                .toList();
    }

    private List<cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity> obtenerProductos(List<Long> ids) {
        List<Long> distintos = ids.stream().distinct().toList();
        List<cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity> productos =
                productoRepository.findAllById(distintos);
        if (productos.size() != distintos.size()) {
            throw new RecursoNoEncontradoException("Uno o más productos del plan");
        }
        return productos;
    }

    private AnimalSalida animalSalida(AnimalEntity animal) {
        return new AnimalSalida(animal.getId(), animal.getCodigoArete(), animal.getRaza(), animal.getSexo(),
                animal.getPesoKg(), animal.getFechaNacimiento(), animal.getEstado(),
                animal.getLote() == null ? null : animal.getLote().getId());
    }

    private LoteSalida loteSalida(LoteEntity lote) {
        return new LoteSalida(lote.getId(), lote.getNombre(), lote.getUbicacion(),
                lote.getProposito(), lote.isActivo());
    }

    private ProductoSalida productoSalida(ProductoVeterinarioEntity producto) {
        return new ProductoSalida(producto.getId(), producto.getNombre(), producto.getTipo(),
                producto.getUnidadMedida(), producto.getDosisMinima(), producto.getDosisMaxima(),
                producto.getPrecioUnitario(), producto.getContenidoMlPorUnidad());
    }

    private InventarioSalida inventarioSalida(InventarioEntity inventario) {
        return new InventarioSalida(inventario.getId(), inventario.getProducto().getId(),
                inventario.getLoteFabricacion(), inventario.getFechaVencimiento(),
                inventario.getCantidadDisponible(), inventario.getCostoUnitario());
    }

    private PlanSanitarioSalida planSalida(PlanSanitarioEntity plan) {
        return new PlanSanitarioSalida(plan.getId(), plan.getNombre(), plan.getDescripcion(),
                plan.getEdadObjetivoMeses(),
                plan.getProductos().stream().map(ProductoVeterinarioEntity::getId).sorted().toList());
    }

    private ProveedorSalida proveedorSalida(ProveedorEntity proveedor) {
        return new ProveedorSalida(proveedor.getId(), proveedor.getNombre(),
                proveedor.getTelefono(), proveedor.getCorreo());
    }

    private CompraSalida compraSalida(CompraEntity compra) {
        return new CompraSalida(compra.getId(), compra.getProveedor().getId(),
                compra.getFechaCompra(), compra.getTotal());
    }
}
