package com.aluralatam.arulalatam.repository;

import java.util.List;
import com.aluralatam.arulalatam.model.Libro;
import com.aluralatam.arulalatam.model.Autor;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface LibroRepository extends CrudRepository<Libro, Long> {
    Iterable<Libro> findByIdioma(String idioma);
    List<Libro> findByAutor(Autor autor);
    Optional<Libro> findByTituloAndIdioma(String titulo, String idioma); // Agrega este método
}
