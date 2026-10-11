package cr.ac.una.eif509.demo.presentation.web;

import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackageClasses = WebErrorHandler.class)
public class WebErrorHandler {
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String recursoNoEncontrado(RecursoNoEncontradoException exception, Model model) {
        model.addAttribute("mensaje", exception.getMessage());
        return "error/404";
    }
}
