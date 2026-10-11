package cr.ac.una.eif509.demo.presentation.web;

import cr.ac.una.eif509.demo.business.CatalogoService;
import cr.ac.una.eif509.demo.business.ProductoSalida;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/productos")
public class ProductoWebController {
    private static final String LISTA = "productos/lista";
    private static final String FORMULARIO = "productos/form";
    private final CatalogoService catalogoService;

    public ProductoWebController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public String listar(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        model.addAttribute("pagina", catalogoService.productos(null, null, pageable));
        return LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new ProductoForm());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("form") ProductoForm form,
            BindingResult errores,
            RedirectAttributes flash
    ) {
        if (errores.hasErrors()) {
            return FORMULARIO;
        }
        try {
            catalogoService.crearProducto(form.aEntrada());
        } catch (DataIntegrityViolationException exception) {
            errores.rejectValue("nombre", "producto.duplicado",
                    "No se pudo guardar: revise que el nombre no esté registrado.");
            return FORMULARIO;
        }
        flash.addFlashAttribute("ok", "El producto veterinario se creó correctamente.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/{id}")
    public String editar(@PathVariable Long id, Model model) {
        ProductoSalida producto = catalogoService.producto(id);
        model.addAttribute("form", ProductoForm.desde(producto));
        model.addAttribute("productoId", id);
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ProductoForm form,
            BindingResult errores,
            Model model,
            RedirectAttributes flash
    ) {
        model.addAttribute("productoId", id);
        if (errores.hasErrors()) {
            return FORMULARIO;
        }
        try {
            catalogoService.actualizarProducto(id, form.aEntrada());
        } catch (DataIntegrityViolationException exception) {
            errores.rejectValue("nombre", "producto.duplicado",
                    "No se pudo guardar: revise que el nombre no esté registrado.");
            return FORMULARIO;
        }
        flash.addFlashAttribute("ok", "El producto veterinario se actualizó correctamente.");
        return "redirect:/admin/productos";
    }
}
