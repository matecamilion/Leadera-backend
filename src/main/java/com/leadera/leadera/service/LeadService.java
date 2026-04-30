package com.leadera.leadera.service;

import com.leadera.leadera.model.*;
import com.leadera.leadera.repository.AgenteRepository;
import com.leadera.leadera.repository.InteraccionRepository;
import com.leadera.leadera.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeadService {
    private final LeadRepository leadRepository;
    private final AgenteRepository agenteRepository;
    private final InteraccionRepository interaccionRepository;

    public LeadService(LeadRepository leadRepository, AgenteRepository agenteRepository, InteraccionRepository interaccionRepository) {
        this.leadRepository = leadRepository;
        this.agenteRepository = agenteRepository;
        this.interaccionRepository = interaccionRepository;
    }


    public Lead crearLead(Lead lead, String email) {

        Agente agente = agenteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agente no encontrado"));

        lead.setAgente(agente);

        if (lead.getFechaEntrada() == null) {
            lead.setFechaEntrada(LocalDateTime.now());
        }

        return leadRepository.save(lead);
    }

    public List<Lead> obtenerLeadsPorAgente(String email) {
        return leadRepository.findByAgenteEmail(email);
    }

    public List<Lead> obtenerLeads() {
        return leadRepository.findAll();
    }

    public List<Lead> obtenerLeadsPorEstado(EstadoLead estado, String email) { // <--- Agregamos email
        // Usamos el nuevo método del repository que filtra por ambas cosas
        return leadRepository.findByEstadoAndAgenteEmail(estado, email);
    }

    public List<Lead> obtenerLeadsSinContacto(String email) { // <--- Agregamos email
        // Este es clave para la sección de "Nuevos"
        return leadRepository.findByUltimoContactoIsNullAndAgenteEmail(email);
    }

    public List<Lead> obtenerLeadsInactivos(int dias, String email) { // <--- Agregamos email
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        // Usamos el nuevo método del repo con filtro de email
        return leadRepository.findByUltimoContactoBeforeAndUltimoContactoIsNotNullAndAgenteEmail(fechaLimite, email);
    }


    public Lead obtenerLeadsPorId(Long id, String email) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el lead con el id: " + id));

        // VALIDACIÓN DE SEGURIDAD:
        // Si el email del agente del lead no coincide con el del que está logueado, rebotamos.
        if (!lead.getAgente().getEmail().equals(email)) {
            throw new RuntimeException("No tenés permiso para ver este lead.");
        }

        return lead;
    }

    public List<Lead> obtenerLeadsPrioritarios(int dias, String email) { // <--- Agregamos email
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        // Usamos el nuevo método del repo con filtro de email
        return leadRepository.findByEstadoAndUltimoContactoBeforeAndAgenteEmail(EstadoLead.CALIENTE, fechaLimite, email);
    }

    public List<Interaccion> obtenerHistorialInteracciones(Long leadId) {

            Lead lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new RuntimeException("No existe el lead con el id: " + leadId));

        return lead.getInteracciones();
    }



    public Lead cambiarEstado(Long id,EstadoLead nuevoEstado) {
        //Busca el lead por ID. Si no lo encuentra tira excepcion
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado"));

        //Cambia el estado del lead en caso de que lo haya encontrado
        lead.setEstado(nuevoEstado);
        return leadRepository.save(lead);
    }


    public LeadsHoyResponse obtenerLeadsDeHoy(String email) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioHoy = ahora.toLocalDate().atStartOfDay();
        LocalDateTime finHoy = ahora.toLocalDate().atTime(23, 59, 59);

        // Agregamos el "email" a todas las llamadas del repository:
        List<Lead> nuevos = leadRepository.findByUltimoContactoIsNullAndAgenteEmailAndEstadoNot(email, EstadoLead.GANADO);

        // Para prioritarios, usá el método de abajo o el nuevo del repository
        LocalDateTime fechaLimitePrioritarios = LocalDateTime.now().minusDays(7);
        List<Lead> prioritarios = leadRepository.findByEstadoAndUltimoContactoBeforeAndAgenteEmail(EstadoLead.CALIENTE, fechaLimitePrioritarios, email);

        List<Lead> seguimientos = leadRepository.findSeguimientosPendientes(
                ahora,
                email,
                EstadoLead.GANADO,
                EstadoLead.INACTIVO
        );

        List<Lead> yaContactados = leadRepository.findByUltimoContactoAfterAndAgenteEmail(inicioHoy, email);


        int totalTareas = nuevos.size() + prioritarios.size() + seguimientos.size() + yaContactados.size();
        int completadas = yaContactados.size();

        return new LeadsHoyResponse(nuevos, prioritarios, seguimientos, yaContactados, totalTareas, completadas);
    }

    public Lead guardarBusqueda(Long id, Busqueda nuevaBusqueda) {
        // Buscamos el lead, si no existe lanzamos una excepción o manejamos el error
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado con id: " + id));

        // Actualizamos la relación
        lead.setBusqueda(nuevaBusqueda);

        // Guardamos (esto hace un update en la DB si el ID ya existe)
        return leadRepository.save(lead);
    }

    public Lead establecerLeadInactivo(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado"));

         lead.setEstado(EstadoLead.INACTIVO);
         return leadRepository.save(lead);
    }

    public AgenteDashboardDTO obtenerEstadisticasAgente(Long agenteId){
        long activos = leadRepository.countByAgenteIdAndEstadoNot(agenteId, EstadoLead.INACTIVO);
        long calientes = leadRepository.countByAgenteIdAndEstado(agenteId, EstadoLead.CALIENTE);
        long tibios = leadRepository.countByAgenteIdAndEstado(agenteId, EstadoLead.TIBIO);
        long frios = leadRepository.countByAgenteIdAndEstado(agenteId, EstadoLead.FRIO);
        long ganadosMes = leadRepository.countGanadosDelMes(agenteId);
        long nuevosDelMes = leadRepository.countIngresosDelMes(agenteId);
        long perdidos = leadRepository.countByAgenteIdAndEstado(agenteId, EstadoLead.INACTIVO);

        long interacciones7d = interaccionRepository.countInteraccionesDesde(
                agenteId, LocalDateTime.now().minusDays(7)
        );

        double tasaConversion = nuevosDelMes > 0
                ? Math.round((double) ganadosMes / nuevosDelMes * 100 * 10.0) / 10.0
                : 0.0;

        List<Lead> leads = leadRepository.findLeadsConFechaEntrada(agenteId);
        double tiempoRespuesta = leads.stream()
                .mapToLong(lead -> {
                    LocalDateTime primera = interaccionRepository.findPrimeraInteraccion(lead.getId());
                    if (primera == null) return -1L;
                    return ChronoUnit.HOURS.between(lead.getFechaEntrada(), primera);
                })
                .filter(h -> h >= 0)
                .average()
                .orElse(0.0);
        double tiempoRespuestaDias = Math.round(tiempoRespuesta / 24 * 10.0) / 10.0;

        return new AgenteDashboardDTO(activos, calientes, tibios, frios, ganadosMes,
                nuevosDelMes, perdidos, interacciones7d,
                tasaConversion, tiempoRespuestaDias);
    }

    public Lead editarInfoContacto(Long leadId, String nuevoTelefono, String nuevoEmail, String emailAgente) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado"));

        if (!lead.getAgente().getEmail().equals(emailAgente)) {
            throw new RuntimeException("No tenés permiso para editar este lead.");
        }

        // Verificar teléfono duplicado
        boolean telefonoDuplicado = leadRepository.existsByAgenteEmailAndTelefonoAndIdNot(emailAgente, nuevoTelefono, leadId);
        if (telefonoDuplicado) {
            throw new RuntimeException("Ya tenés un lead con ese teléfono.");
        }

        // Verificar email duplicado
        boolean emailDuplicado = leadRepository.existsByAgenteEmailAndEmailAndIdNot(emailAgente, nuevoEmail, leadId);
        if (emailDuplicado) {
            throw new RuntimeException("Ya tenés un lead con ese email.");
        }

        lead.setTelefono(nuevoTelefono);
        lead.setEmail(nuevoEmail);

        return leadRepository.save(lead);
    }

}
