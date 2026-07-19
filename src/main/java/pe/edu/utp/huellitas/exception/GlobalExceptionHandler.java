package pe.edu.utp.huellitas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Manejador global de excepciones para controladores MVC.
 *
 * Captura excepciones no manejadas y las convierte en respuestas amigables.
 *
 * ┌──────────────────────────────────────┬────────────────────────────────┐
 * │ Excepción                            │ Respuesta                       │
 * ├──────────────────────────────────────┼────────────────────────────────┤
 * │ NegocioException                     │ 400 + error/negocio             │
 * │ IllegalArgumentException             │ 400 + error/negocio             │
 * │ IllegalStateException                │ 409 + error/conflicto           │
 * │ OptimisticLockingFailureException    │ 409 + error/conflicto-stock     │
 * │ AccessDeniedException                │ 403 + error/acceso-denegado     │
 * │ Exception (fallback)                 │ 500 + error/interno             │
 * └──────────────────────────────────────┴────────────────────────────────┘
 *
 * TODO (Fase 5): Crear las vistas en templates/error/:
 *   - negocio.html, conflicto.html, acceso-denegado.html, interno.html
 * Por ahora redirige al dashboard con flash de error.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Reglas de negocio violadas ────────────────────────────────────────────

    @ExceptionHandler({NegocioException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleNegocioException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Regla de negocio violada [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorView("error-negocio", ex.getMessage(), request.getRequestURI());
    }

    // ── Conflicto de estado (ej: venta ya anulada, stock concurrent) ──────────

    @ExceptionHandler({IllegalStateException.class, OptimisticLockingFailureException.class, org.springframework.dao.DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleConflictException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Conflicto de estado [{}]: {}", request.getRequestURI(), ex.getMessage());
        String mensaje = "La operación entra en conflicto con datos existentes (ej. el registro ya existe o está asociado a otros datos).";
        if (ex instanceof OptimisticLockingFailureException) {
            mensaje = "Otro usuario modificó el registro al mismo tiempo. Por favor reintenta.";
        } else if (ex instanceof IllegalStateException) {
            mensaje = ex.getMessage();
        }
        return buildErrorView("error-conflicto", mensaje, request.getRequestURI());
    }

    // ── Acceso denegado ───────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorView("error-acceso", "No tienes permiso para realizar esta acción.", request.getRequestURI());
    }

    // ── Fallback ──────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado [{}]", request.getRequestURI(), ex);
        return buildErrorView("error-interno",
                "Ocurrió un error inesperado. Por favor contacta al administrador.",
                request.getRequestURI());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ModelAndView buildErrorView(String viewName, String mensaje, String uri) {
        ModelAndView mav = new ModelAndView("error/" + viewName);
        mav.addObject("errorMsg", mensaje);
        mav.addObject("requestUri", uri);
        return mav;
    }
}
