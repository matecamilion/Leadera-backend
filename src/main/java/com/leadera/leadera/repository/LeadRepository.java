package com.leadera.leadera.repository;

import com.leadera.leadera.model.EstadoLead;
import com.leadera.leadera.model.Interaccion;
import com.leadera.leadera.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    // 1. FUNDAMENTALES (Basados en Estado o Primer Contacto)
    List<Lead> findByEstado(EstadoLead estado);
    List<Lead> findByUltimoContactoIsNull(); // <--- Clave para "Nuevos"
    List<Lead> findByUltimoContactoAfter(LocalDateTime fecha); // <--- Clave para "Ya Gestionados"

    // 2. LÓGICA DE PRIORITARIOS (Calientes que no tocamos hace X días)
    List<Lead> findByEstadoAndUltimoContactoBefore(EstadoLead estado, LocalDateTime fechaLimite);

    // 3. LA QUERY MAESTRA PARA SEGUIMIENTOS
    // Trae los programados para hoy o atrasados, siempre que NO los hayamos contactado hoy todavía.
    @Query("SELECT l FROM Lead l WHERE l.fechaProximoSeguimiento <= :finHoy " +
            "AND (l.ultimoContacto < :inicioHoy OR l.ultimoContacto IS NULL) " +
            "AND l.estado != 'CERRADO'")
    List<Lead> findSeguimientosPendientes(@Param("inicioHoy") LocalDateTime inicioHoy,
                                          @Param("finHoy") LocalDateTime finHoy);

    // 4. MÉTRICAS PARA LA BARRA DE PROGRESO (Simplificadas)
    long countByFechaEntradaAfter(LocalDateTime fecha);
    long countByFechaEntradaAfterAndUltimoContactoIsNotNull(LocalDateTime fecha);

    // Mantenemos este por si se usa en otro lado, pero para la home ya tenemos la query de arriba
    List<Lead> findByFechaProximoSeguimientoBetween(LocalDateTime inicio, LocalDateTime fin);


    // Busca leads cuyo último contacto fue hace X días Y que NO sean nulos.
    List<Lead> findByUltimoContactoBeforeAndUltimoContactoIsNotNull(LocalDateTime fecha);
}
