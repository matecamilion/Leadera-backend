package com.leadera.leadera.service;

import com.leadera.leadera.model.*;
import com.leadera.leadera.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeadService {
    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }


    public Lead crearLead(Lead lead) {
        if(lead.getEstado() == null) {
            lead.setEstado(EstadoLead.FRIO);
        }

        lead.setFechaEntrada(LocalDateTime.now());
        return leadRepository.save(lead);
    }

    public List<Lead> obtenerLeads() {
        return leadRepository.findAll();
    }

    public List<Lead> obtenerLeadsPorEstado(EstadoLead estado) {
        return leadRepository.findByEstado(estado);
    }

    public List<Lead> obtenerLeadsSinContacto() {
        return leadRepository.findByUltimoContactoIsNull();
    }

    public List<Lead> obtenerLeadsInactivos(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);

        return leadRepository.findByUltimoContactoBeforeAndUltimoContactoIsNotNull(fechaLimite);
    }

    public Lead obtenerLeadsPorId(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el lead con el id: " + id));
    }

    public List<Lead> obtenerLeadsPrioritarios(int dias){
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        return leadRepository.findByEstadoAndUltimoContactoBefore(EstadoLead.CALIENTE, fechaLimite);
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

    public Lead registrarContacto(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado"));
        lead.setUltimoContacto(LocalDateTime.now());
        return leadRepository.save(lead);
    }

    public LeadsHoyResponse obtenerLeadsDeHoy() {
        // Definimos el rango de "hoy" para la lógica de negocio
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioHoy = ahora.toLocalDate().atStartOfDay();
        LocalDateTime finHoy = ahora.toLocalDate().atTime(23, 59, 59);

        // 1. NUEVOS: Leads que NUNCA fueron contactados.
        // No importa si entraron hoy o hace una semana; si no los tocaste, son nuevos.
        List<Lead> nuevos = leadRepository.findByUltimoContactoIsNull();

        // 2. PRIORITARIOS: Leads "Calientes" que no se tocan hace más de 7 días.
        List<Lead> prioritarios = obtenerLeadsPrioritarios(7);

        // 3. SEGUIMIENTOS: Los que programaste para hoy (o que te quedaron pendientes de ayer)
        // Usamos la nueva Query del Repository que es mucho más robusta.
        List<Lead> seguimientos = leadRepository.findSeguimientosPendientes(inicioHoy, finHoy);

        // 4. YA CONTACTADOS: Lo que ya laburaste hoy.
        // Esto es lo que llena la barra de progreso.
        List<Lead> yaContactados = leadRepository.findByUltimoContactoAfter(inicioHoy);

        // 5. MÉTRICAS:
        // El total de tareas es lo que tenés pendiente + lo que ya hiciste.
        int totalTareas = nuevos.size() + prioritarios.size() + seguimientos.size() + yaContactados.size();
        int completadas = yaContactados.size();

        return new LeadsHoyResponse(
                nuevos,
                prioritarios,
                seguimientos,
                yaContactados,
                totalTareas,
                completadas
        );
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
}
