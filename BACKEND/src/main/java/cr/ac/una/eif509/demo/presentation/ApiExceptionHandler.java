package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.exception.JornadaNoEjecutableException;
import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import cr.ac.una.eif509.demo.business.exception.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

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

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception exception) {
        LOGGER.error("Error inesperado al atender la solicitud.", exception);
        ProblemDetail body = problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado al procesar la solicitud.", "error-interno");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, List<String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        error -> error.getField(),
                        LinkedHashMap::new,
                        Collectors.mapping(error -> error.getDefaultMessage(), Collectors.toList())));
        List<String> globalErrors = exception.getBindingResult().getGlobalErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList();
        String detail = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining("; "));
        if (!globalErrors.isEmpty()) {
            String globalDetail = String.join("; ", globalErrors);
            detail = detail.isBlank() ? globalDetail : detail + "; " + globalDetail;
        }
        ProblemDetail body = problem(status, detail.isBlank()
                ? "Uno o más campos no cumplen las reglas de validación." : detail, "solicitud-invalida");
        body.setProperty("errors", errors);
        if (!globalErrors.isEmpty()) {
            body.setProperty("globalErrors", globalErrors);
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return ResponseEntity.status(status).headers(responseHeaders).body(body);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        if (body instanceof ProblemDetail problem) {
            if (problem.getStatus() == 0) {
                problem.setStatus(status.value());
            }
            if (problem.getTitle() == null) {
                problem.setTitle(HttpStatus.valueOf(status.value()).getReasonPhrase());
            }
            if (problem.getType() == null || problem.getType().equals(ProblemDetail.forStatus(status).getType())) {
                problem.setType(URI.create("https://sisganado.local/problems/error-http"));
            }
            responseHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        }
        return super.createResponseEntity(body, responseHeaders, status, request);
    }

    private ProblemDetail problem(HttpStatusCode status, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://sisganado.local/problems/" + type));
        problem.setTitle(HttpStatus.valueOf(status.value()).getReasonPhrase());
        return problem;
    }
}
