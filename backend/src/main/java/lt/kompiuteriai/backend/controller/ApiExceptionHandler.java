package lt.kompiuteriai.backend.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusError(
            ResponseStatusException exception) {
        String message = exception.getReason();

        if (message == null || message.isBlank()) {
            message = "Užklausos nepavyko įvykdyti";
        }

        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRequestValidationError(
            MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(
                        error -> {
                            String message = error.getDefaultMessage();

                            if (message == null) {
                                message = "Netinkama reikšmė.";
                            }

                            errors.putIfAbsent(error.getField(), message);
                        });

        return Map.of("message", "Patikrinkite įvestus duomenis.", "errors", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(
            HandlerMethodValidationException exception) {
        String message = "Netinkamos užklausos parametrų reikšmės.";

        if (exception.isForReturnValue()) {
            message = "Vidinė serverio klaida.";
        }

        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleParameterTypeError(
            MethodArgumentTypeMismatchException exception) {
        return Map.of("message", "Netinkamas parametro „" + exception.getName() + "“ formatas.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return Map.of("message", "Netinkamas JSON formatas arba laukų duomenų tipai.");
    }
}
