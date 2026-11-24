package mx.edu.backendacademico.config;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        // 1. Conserva CSRF: un navegador también puede enviar HTTP Basic automáticamente.
        http.csrf(withDefaults());
        // 2. La primera regla coincidente determina el acceso.
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/saludo", "/openapi.yaml", "/error").permitAll()
            .requestMatchers("/api/v1/csrf").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/v1/alumnos/**").hasAnyRole("ALUMNO", "DOCENTE", "ADMIN")
            .requestMatchers("/api/v1/**").hasAnyRole("DOCENTE", "ADMIN")
            .anyRequest().denyAll());
        // 3. Basic autentica credenciales; TLS protege su transporte fuera de localhost.
        http.httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder) {
        // Cuentas públicas de laboratorio; no contienen credenciales reales.
        return new InMemoryUserDetailsManager(
            User.withUsername("alumno").password(encoder.encode("alumno123")).roles("ALUMNO").build(),
            User.withUsername("docente").password(encoder.encode("docente123")).roles("DOCENTE").build(),
            User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build());
    }
}
