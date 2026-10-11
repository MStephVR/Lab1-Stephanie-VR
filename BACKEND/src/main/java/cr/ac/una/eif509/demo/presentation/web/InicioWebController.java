package cr.ac.una.eif509.demo.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioWebController {
    @GetMapping("/")
    public String inicio() {
        return "redirect:/admin/productos";
    }
}
