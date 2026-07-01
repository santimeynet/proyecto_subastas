package com.utn.tpi.subasta.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex, HttpServletRequest request) {
        URI type = ex.getStatus() == HttpStatus.CONFLICT ? ProblemTypes.CONFLICT : ProblemTypes.BUSINESS;
        String title = ex.getStatus() == HttpStatus.CONFLICT ? "Conflicto de concurrencia" : "Error de negocio";
        return problemResponse(ex.getStatus(), type, title, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validacion fallida");
        problem.setTitle("Datos invalidos");
        problem.setType(ProblemTypes.VALIDATION);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return problemResponse(HttpStatus.UNAUTHORIZED, ProblemTypes.AUTHENTICATION,
                "Autenticacion fallida", "Credenciales invalidas", request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ProblemDetail> handleLocked(LockedException ex, HttpServletRequest request) {
        return problemResponse(HttpStatus.FORBIDDEN, ProblemTypes.AUTHENTICATION,
                "Cuenta bloqueada", "Usuario bloqueado", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return problemResponse(HttpStatus.FORBIDDEN, ProblemTypes.AUTHORIZATION,
                "Acceso denegado", "No tiene permisos para esta operacion", request);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockException ex, HttpServletRequest request) {
        return problemResponse(HttpStatus.CONFLICT, ProblemTypes.CONFLICT,
                "Conflicto de concurrencia",
                "Otro usuario actualizo la subasta. Actualice el monto minimo e intente nuevamente.",
                request);
    }

    // Esta es la excepcion que realmente tira Spring Data JPA cuando el save()
    // detecta que el @Version de la entidad ya no coincide con el de la fila
    // en la base (es decir, otra puja se guardo primero). Con bloqueo
    // pesimista esto practicamente nunca se daba; con bloqueo optimista puro
    // es el caso normal de "dos pujas chocaron".
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleObjectOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                                      HttpServletRequest request) {
        return problemResponse(HttpStatus.CONFLICT, ProblemTypes.CONFLICT,
                "Conflicto de concurrencia",
                "Otro usuario actualizo la subasta justo antes que usted. Actualice el monto minimo e intente nuevamente.",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return problemResponse(HttpStatus.CONFLICT, ProblemTypes.CONFLICT,
                "Conflicto de datos",
                "La operacion entro en conflicto con datos existentes. Actualice e intente nuevamente.",
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMessageNotReadable(HttpMessageNotReadableException ex,
                                                                    HttpServletRequest request) {
        return problemResponse(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION,
                "Datos invalidos", "Formato de fecha u otro campo invalido en el JSON", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex, HttpServletRequest request) {
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, ProblemTypes.INTERNAL,
                "Error interno", "Error interno del servidor", request);
    }

    private ResponseEntity<ProblemDetail> problemResponse(HttpStatus status, URI type, String title,
                                                            String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(type);
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
