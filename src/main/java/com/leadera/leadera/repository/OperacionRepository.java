package com.leadera.leadera.repository;


import com.leadera.leadera.model.EstadoOperacion;
import com.leadera.leadera.model.Operacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperacionRepository extends JpaRepository<Operacion, Integer> {
    List<Operacion> findByLeadIdAndAgenteEmail(Long leadId, String email);

    List<Operacion> findByLeadIdAndAgenteEmailAndEstadoOperacionNot(
            Long leadId,
            String email,
            EstadoOperacion estadoOperacion
    );
}
