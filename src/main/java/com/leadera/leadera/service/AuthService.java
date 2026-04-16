package com.leadera.leadera.service;

import com.leadera.leadera.model.Agente;
import com.leadera.leadera.repository.AgenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AgenteRepository agenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public String login(String email, String password) {
        // 1. Imprimimos qué llega de Postman
        System.out.println(">>> DEBUG: Intentando login con email: [" + email + "]");
        System.out.println(">>> DEBUG: Intentando login con password: [" + password + "]");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Agente agente = agenteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agente no encontrado con email: " + email));


        return jwtService.generarToken(agente);

    }
    public String registrar(Agente agente) {
        //Encriptamos contraseña antes de guardar
        agente.setPassword(passwordEncoder.encode(agente.getPassword()));
        agenteRepository.save(agente);
        return "Agente registrado con exito";
    }


}
