package mx.edu.backendacademico.controller;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
    @GetMapping("/api/v1/csrf")
    public Map<String, String> token(CsrfToken token) {
        // Lee el token diferido y conserva la cookie de sesión de esta respuesta.
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }
}
