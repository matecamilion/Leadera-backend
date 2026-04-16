package com.leadera.leadera.controller;

import com.leadera.leadera.model.Agente;
import com.leadera.leadera.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registrar(@RequestBody Agente agente) {
        try{
            String respuesta = authService.registrar(agente);
            return ResponseEntity.ok(respuesta);
        }catch(Exception e){
            return ResponseEntity.badRequest().body("Error al registrar: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Agente agente) {
        try{
            String token = authService.login(agente.getEmail(),  agente.getPassword());
            return ResponseEntity.ok(token);
        } catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(401).body("Error real: " + e.getMessage());
        }
    }
}
