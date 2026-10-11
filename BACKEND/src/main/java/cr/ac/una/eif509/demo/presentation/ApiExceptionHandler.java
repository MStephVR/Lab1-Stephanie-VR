package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.exception.JornadaNoEjecutableException;
import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import cr.ac.una.eif509.demo.business.exception.ReglaNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail notFound(RecursoNoEncontradoException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage(), "recurso-no-encontrado");
    }

    @ExceptionHandler(JornadaNoEjecutableException.class)
    ProblemDetail conflict(JornadaNoEjecutableException exception) {
        return problem(HttpStatus.CONFLICT, exception.getMessage(), "conflicto-de-estado");
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ProblemDetail ruleViolation(ReglaNegocioException exception) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), "regla-de-negocio");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    ProblemDetail validation(Exception exception) {
        String detail = exception instanceof MethodArgumentNotValidException invalid
                ? invalid.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "))
                : "Uno o más parámetros no cumplen el formato requerido.";
        return problem(HttpStatus.BAD_REQUEST, detail, "solicitud-invalida");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail dataConflict() {
        return problem(HttpStatus.CONFLICT, "La operación viola una restricción de integridad.",
                "conflicto-de-datos");
    }

    @ExceptionHandler(PropertyReferenceException.class)
    ProblemDetail invalidSort(PropertyReferenceException exception) {
        return problem(HttpStatus.BAD_REQUEST,
                "El campo de ordenamiento '" + exception.getPropertyName() + "' no existe.",
                "ordenamiento-invalido");
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail badCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.", "credenciales-invalidas");
    }

    private ProblemDetail problem(HttpStatus status, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://sisganado.local/problems/" + type));
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }
}
