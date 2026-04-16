package com.leadera.leadera.controller;

import com.leadera.leadera.model.*;
import com.leadera.leadera.service.LeadService;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/leads")
public class LeadController {
    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    //Crear lead
    @PostMapping
    public Lead crearLead(@RequestBody Lead lead) {
        return leadService.crearLead(lead);
    }

    //Obtener todos los leads
    @GetMapping
    public List<Lead> listarLeads() {
        return leadService.obtenerLeads();
    }

    //Obtener lead por id
    @GetMapping("/{id}")
    public Lead obtenerLeadPorId(@PathVariable Long id) {
        try{
             return leadService.obtenerLeadsPorId(id);
        } catch (RuntimeException e) {
                throw new RuntimeException(e);
        }
    }

    //Obtener leads por estado
    @GetMapping("/estado/{estado}")
    public List<Lead> obtenerLeadsPorEstado(@PathVariable EstadoLead estado) {
        return leadService.obtenerLeadsPorEstado(estado);
    }

    //Obtener leads que nunca fueron contactados
    @GetMapping("/sin-contactar")
    public List<Lead> obtenerLeadsSinContacto() {
        return leadService.obtenerLeadsSinContacto();
    }

    @GetMapping("/{id}/interacciones")
    public List<Interaccion> obtenerInteraccionesPorId(@PathVariable Long id) {
        return leadService.obtenerHistorialInteracciones(id);
    }

    @GetMapping("/inactivos")
    public List<Lead> obtenerLeadsInactivosPorDias(@RequestParam Integer dias) {
        return leadService.obtenerLeadsInactivos(dias);
    }

    @GetMapping("/prioritarios")
    public List<Lead>  obtenerLeadsPrioritarios(@RequestParam Integer dias) {
        return leadService.obtenerLeadsPrioritarios(dias);
    }

    //Actualizar estado del lead
    @PutMapping("/{id}/estado")
    public Lead cambiarEstado(@PathVariable Long id, @RequestParam EstadoLead nuevoEstado) {
        return leadService.cambiarEstado(id, nuevoEstado);
    }

    @GetMapping("/hoy")
    public LeadsHoyResponse obtenerLeadsDeHoy() {
        return leadService.obtenerLeadsDeHoy();
    }

    @PutMapping("/{id}/busqueda")
    public ResponseEntity<Lead> guardarBusqueda(@PathVariable Long id, @RequestBody Busqueda nuevaBusqueda) {
        try {
            Lead leadActualizado = leadService.guardarBusqueda(id, nuevaBusqueda);
            return ResponseEntity.ok(leadActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
