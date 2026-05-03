package com.leadera.leadera.repository;


import com.leadera.leadera.model.EstadoOperacion;
import com.leadera.leadera.model.Operacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OperacionRepository extends JpaRepository<Operacion, Integer> {
    List<Operacion> findByLeadIdAndAgenteEmail(Long leadId, String email);

    List<Operacion> findByLeadIdAndAgenteEmailAndEstadoOperacionNotIn(
            Long leadId,
            String email,
            List<EstadoOperacion> estadosExcluidos
    );

    Optional<Operacion> findByIdAndLeadIdAndAgenteEmail(
            Long operacionId,
            Long leadId,
            String email
    );
}
