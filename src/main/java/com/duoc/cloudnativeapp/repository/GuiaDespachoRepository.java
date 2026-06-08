package com.duoc.cloudnativeapp.repository;

import com.duoc.cloudnativeapp.model.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Repositorio para consultar y guardar guias de despacho
public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {

    List<GuiaDespacho> findAllByOrderByIdAsc();

    List<GuiaDespacho> findByTransportistaContainingIgnoreCaseOrderByIdAsc(String transportista);

    List<GuiaDespacho> findByFechaGuiaOrderByIdAsc(LocalDate fechaGuia);

    List<GuiaDespacho> findByTransportistaContainingIgnoreCaseAndFechaGuiaOrderByIdAsc(String transportista,
                                                                                        LocalDate fechaGuia);

    Optional<GuiaDespacho> findByNumeroGuiaIgnoreCase(String numeroGuia);
}
