package cr.ac.una.eif509.demo.presentation.web;

import cr.ac.una.eif509.demo.business.CatalogoService;
import cr.ac.una.eif509.demo.business.ProveedorSalida;
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/admin/proveedores")
public class ProveedorWebController {
    private static final String LISTA = "proveedores/lista";
    private static final String FORMULARIO = "proveedores/form";
    private final CatalogoService catalogoService;

    public ProveedorWebController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public String listar(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        model.addAttribute("pagina", catalogoService.proveedores(pageable));
        return LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new ProveedorForm());
        return FORMULARIO;
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("form") ProveedorForm form,
            BindingResult errores,
            RedirectAttributes flash
    ) {
        if (errores.hasErrors()) {
            return FORMULARIO;
        }
        catalogoService.crearProveedor(form.aEntrada());
        flash.addFlashAttribute("ok", "El proveedor se creó correctamente.");
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/{id}")
    public String editar(@PathVariable Long id, Model model) {
        ProveedorSalida proveedor = catalogoService.proveedor(id);
        model.addAttribute("form", ProveedorForm.desde(proveedor));
        model.addAttribute("proveedorId", id);
        return FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ProveedorForm form,
            BindingResult errores,
            Model model,
            RedirectAttributes flash
    ) {
        model.addAttribute("proveedorId", id);
        if (errores.hasErrors()) {
            return FORMULARIO;
        }
        catalogoService.actualizarProveedor(id, form.aEntrada());
        flash.addFlashAttribute("ok", "El proveedor se actualizó correctamente.");
        return "redirect:/admin/proveedores";
    }
}
