package com.leadera.leadera.repository;

import com.leadera.leadera.model.Agente;
import com.leadera.leadera.model.EstadoLead;
import com.leadera.leadera.model.Interaccion;
import com.leadera.leadera.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    // 1. FUNDAMENTALES + FILTRO AGENTE
    List<Lead> findByEstadoAndAgenteEmail(EstadoLead estado, String email);

    // Estos son los que usa tu Dashboard:
    List<Lead> findByUltimoContactoIsNullAndAgenteEmail(String email);
    List<Lead> findByUltimoContactoAfterAndAgenteEmail(LocalDateTime fecha, String email);
    List<Lead> findByAgenteEmail(String email);
    long countByAgenteIdAndEstadoNot(Long agenteId, EstadoLead estado);

    // 2. PRIORITARIOS + FILTRO AGENTE
    List<Lead> findByEstadoAndUltimoContactoBeforeAndAgenteEmail(EstadoLead estado, LocalDateTime fechaLimite, String email);

    // 3. LA QUERY MAESTRA PARA SEGUIMIENTOS (Actualizada con el AND l.agente.email)
    @Query("SELECT l FROM Lead l WHERE l.fechaProximoSeguimiento <= :finHoy " +
            "AND (l.ultimoContacto < :inicioHoy OR l.ultimoContacto IS NULL) " +
            "AND l.estado != 'CERRADO' " +
            "AND l.agente.email = :email") // <--- AGREGAMOS ESTO
    List<Lead> findSeguimientosPendientes(@Param("inicioHoy") LocalDateTime inicioHoy,
                                          @Param("finHoy") LocalDateTime finHoy,
                                          @Param("email") String email); // <--- Y ESTO

    // 4. OTROS MÉTODOS (Si los usás, agregales el filtro también)
    List<Lead> findByUltimoContactoBeforeAndUltimoContactoIsNotNullAndAgenteEmail(LocalDateTime fecha, String email);


    // Cuenta todos los leads de un agente
    long countByAgenteId(Long agenteId);

    // Cuenta leads por agente y estado específico
    long countByAgenteIdAndEstado(Long agenteId, EstadoLead estado);

    // Leads que ENTRARON este mes (basado en fecha de creación)
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.agente.id = :agenteId " +
            "AND MONTH(l.fechaEntrada) = MONTH(CURRENT_DATE) " +
            "AND YEAR(l.fechaEntrada) = YEAR(CURRENT_DATE)")
    long countIngresosDelMes(@Param("agenteId") Long agenteId);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.agente.id = :agenteId " +
            "AND l.estado = 'GANADO' " +
            "AND MONTH(l.ultimoContacto) = MONTH(CURRENT_DATE) " +
            "AND YEAR(l.ultimoContacto) = YEAR(CURRENT_DATE)")
    long countGanadosDelMes(@Param("agenteId") Long agenteId);

    // Leads del agente con su fechaEntrada (para calcular tiempo de respuesta)
    @Query("SELECT l FROM Lead l WHERE l.agente.id = :agenteId AND l.fechaEntrada IS NOT NULL")
    List<Lead> findLeadsConFechaEntrada(@Param("agenteId") Long agenteId);

    boolean existsByAgenteEmailAndTelefonoAndIdNot(String agenteEmail, String telefono, Long id);
    boolean existsByAgenteEmailAndEmailAndIdNot(String agenteEmail, String email, Long id);

}
