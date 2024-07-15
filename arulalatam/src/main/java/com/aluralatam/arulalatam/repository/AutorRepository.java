package com.aluralatam.arulalatam.repository;

import com.aluralatam.arulalatam.model.Autor;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;


public interface AutorRepository extends CrudRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);
    Iterable<Autor> findByAnoFallecimientoGreaterThanAndAnoNacimientoLessThanEqual(int anoFallecimiento, int anoNacimiento);
}
