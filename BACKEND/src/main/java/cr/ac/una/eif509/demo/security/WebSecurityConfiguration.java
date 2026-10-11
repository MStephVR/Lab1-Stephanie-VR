package cr.ac.una.eif509.demo.security;

import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class WebSecurityConfiguration {
    @Bean
    @Order(2)
    SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/admin/**", "/login", "/logout", "/css/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/admin/productos", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?salida")
                        .permitAll())
                .build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/actuator/health", "/", "/error").permitAll()
                        .anyRequest().permitAll())
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> usuarioRepository.findByUsername(username)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .map(WebSecurityConfiguration::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña inválidos."));
    }

    private static UserDetails toUserDetails(UsuarioEntity usuario) {
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol())
                .build();
    }
}
