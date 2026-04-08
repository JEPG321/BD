package com.laboratorio.msnotificaciones.repository;

import com.laboratorio.msnotificaciones.entity.RegistroNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroNotificacionRepository extends JpaRepository<RegistroNotificacion, Long> {
}
