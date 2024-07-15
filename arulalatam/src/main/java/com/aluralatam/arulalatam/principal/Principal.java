package com.aluralatam.arulalatam.principal;

import com.aluralatam.arulalatam.model.Libro;
import com.aluralatam.arulalatam.model.Autor;
import com.aluralatam.arulalatam.repository.AutorRepository;
import com.aluralatam.arulalatam.repository.LibroRepository;
import com.aluralatam.arulalatam.service.ConsumoAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;


@Component
public class Principal {
    private static final String GUTENDEX_URL = "https://gutendex.com/books/";

    private final ConsumoAPI consumoAPI;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final ObjectMapper mapper;

    public Principal(ConsumoAPI consumoAPI, LibroRepository libroRepository, AutorRepository autorRepository) {
        this.consumoAPI = consumoAPI;
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.mapper = new ObjectMapper(); // Inicializar ObjectMapper aquí
    }

    public void muestraElMenu() {
        Scanner scanner = new Scanner(System.in);
        int option = 0;

        while (option != 6) {
            System.out.println("Bienvenido al sistema de gestión de libros.");
            System.out.println("1. Buscar Libro por título");
            System.out.println("2. Listar todos los libros registrados");
            System.out.println("3. Buscar autores registrados");
            System.out.println("4. Listar autores vivos en determinado año");
            System.out.println("5. Buscar libros por idioma");
            System.out.println("6. Salir");
            System.out.print("Por favor, seleccione una opción: ");

            try {
                option = scanner.nextInt();
                scanner.nextLine(); // Limpiar el buffer de entrada

                switch (option) {
                    case 1:
                        LibroPorTitulo();
                        break;
                    case 2:
                        TodosLosLibros();
                        break;
                    case 3:
                        BuscarAutores();
                        break;
                    case 4:
                        AutoresVivos();
                        break;
                    case 5:
                        LibroPorIdioma();
                        break;
                    case 6:
                        cerrar();
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine(); // Limpiar el buffer de entrada en caso de error
            }
        }
        scanner.close();
    }

    private void LibroPorTitulo() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el título del libro:");
        String title = scanner.nextLine();

        try {
            String url = GUTENDEX_URL + "?search=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
            String response = consumoAPI.obtenerDatos(url);

            JsonNode jsonNode = mapper.readTree(response);

            JsonNode results = jsonNode.get("results");
            if (results.isArray() && !results.isEmpty()) {
                List<String> librosEncontrados = new ArrayList<>();

                for (JsonNode bookNode : results) {
                    String bookTitle = bookNode.get("title").asText();
                    JsonNode authorsNode = bookNode.get("authors");
                    if (authorsNode != null && !authorsNode.isEmpty()) {
                        JsonNode authorNode = authorsNode.get(0);
                        String authorName = authorNode.get("name").asText();
                        int birthYear = authorNode.has("birth_year") ? authorNode.get("birth_year").asInt() : 0;
                        int deathYear = authorNode.has("death_year") ? authorNode.get("death_year").asInt() : 0;
                        String language = bookNode.get("languages").get(0).asText();
                        int downloadCount = bookNode.get("download_count").asInt();

                        // Guardar información del libro en una cadena formateada
                        String libroInfo = """
                        -------------------
                              LIBRO
                        -------------------
                        Título: %s
                        Autor: %s
                        Idioma: %s
                        Número de Descargas: %d
                        --------------------
                        """.formatted(bookTitle, authorName, language, downloadCount);

                        librosEncontrados.add(libroInfo);
                    }
                }

                // Imprimir todos los libros encontrados
                librosEncontrados.forEach(System.out::println);

                // Mostrar el nuevo menú
                mostrarMenuLibrosEncontrados();
            } else {
                System.out.println("No se encontraron libros con el título proporcionado.");
            }
        } catch (IOException e) {
            System.out.println("Error al buscar el libro: " + e.getMessage());
        }
    }

    private void mostrarMenuLibrosEncontrados() {
        Scanner scanner = new Scanner(System.in);
        int option = 0;

        while (option != 3) {
            System.out.println("Seleccione una opción:");
            System.out.println("1. Buscar nuevo libro");
            System.out.println("2. Buscar un libro específico y guardar en la base de datos");
            System.out.println("3. Volver al menú anterior");
            System.out.print("Por favor, seleccione una opción: ");

            try {
                option = scanner.nextInt();
                scanner.nextLine(); // Limpiar el buffer de entrada

                switch (option) {
                    case 1:
                        LibroPorTitulo();
                        break;
                    case 2:
                        buscarYGuardarLibroEspecifico();
                        break;
                    case 3:
                        // Volver al menú anterior
                        return;
                    default:
                        System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine(); // Limpiar el buffer de entrada en caso de error
            }
        }
    }

    private void buscarYGuardarLibroEspecifico() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Escriba el nombre completo del libro que desea agregar a la base de datos:");
        String title = scanner.nextLine();

        try {
            String url = GUTENDEX_URL + "?search=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
            String response = consumoAPI.obtenerDatos(url);

            JsonNode jsonNode = mapper.readTree(response);

            JsonNode results = jsonNode.get("results");
            if (results.isArray() && !results.isEmpty()) {
                JsonNode bookNode = results.get(0); // Tomar el primer libro que coincida
                String bookTitle = bookNode.get("title").asText();
                JsonNode authorsNode = bookNode.get("authors");
                if (authorsNode != null && !authorsNode.isEmpty()) {
                    JsonNode authorNode = authorsNode.get(0);
                    String authorName = authorNode.get("name").asText();
                    int birthYear = authorNode.has("birth_year") ? authorNode.get("birth_year").asInt() : 0;
                    int deathYear = authorNode.has("death_year") ? authorNode.get("death_year").asInt() : 0;
                    String language = bookNode.get("languages").get(0).asText();
                    int downloadCount = bookNode.get("download_count").asInt();

                    // Verificar si el libro ya está en la base de datos
                    Libro existingLibro = libroRepository.findByTituloAndIdioma(bookTitle, language).orElse(null);

                    if (existingLibro != null) {
                        System.out.println("""
                        -----------------------------------
                             EL LIBRO YA ESTÁ GUARDADO
                        -----------------------------------
                        """);
                    } else {
                        // Buscar o crear el autor en la base de datos
                        Autor autor = autorRepository.findByNombre(authorName).orElseGet(() -> {
                            Autor nuevoAutor = new Autor();
                            nuevoAutor.setNombre(authorName);
                            nuevoAutor.setAnoNacimiento(birthYear);
                            nuevoAutor.setAnoFallecimiento(deathYear);
                            return autorRepository.save(nuevoAutor);
                        });

                        // Crear y guardar el libro
                        Libro libro = new Libro();
                        libro.setTitulo(bookTitle);
                        libro.setAutor(autor);
                        libro.setIdioma(language);
                        libro.setNumeroDescargas(downloadCount);
                        libroRepository.save(libro);

                        System.out.println("El libro ha sido guardado en la base de datos.");
                    }
                }
            } else {
                System.out.println("No se encontraron libros con el título proporcionado.");
            }
        } catch (IOException e) {
            System.out.println("Error al buscar el libro: " + e.getMessage());
        }
    }



    private void TodosLosLibros() {
        // Utiliza un HashSet para asegurar que no hay duplicados
        Set<Libro> librosUnicos = new HashSet<>();
        Iterable<Libro> libros = libroRepository.findAll();
        for (Libro libro : libros) {
            librosUnicos.add(libro);
        }

        // Imprime los libros en el formato deseado

        for (Libro libro : librosUnicos) {
            System.out.print(libro); // Utiliza el método toString de Libro
        }
        System.out.println("""
                -------------------
                        FIN
                -------------------""");
    }

    private void BuscarAutores() {
        Iterable<Autor> autores = autorRepository.findAll();
        System.out.println("""
                -------------------
                      AUTORES\s
                -------------------""");
        for (Autor autor : autores) {
            System.out.println("Autor: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getAnoNacimiento());
            System.out.println("Fecha de fallecimiento: " + autor.getAnoFallecimiento());

            // Obtener los libros asociados al autor
            List<Libro> libros = libroRepository.findByAutor(autor);
            if (!libros.isEmpty()) {
                System.out.println("Libros:");
                for (Libro libro : libros) {
                    System.out.println("  - " + libro.getTitulo());
                }
            } else {
                System.out.println("Libros: No tiene libros registrados.");
            }
            System.out.println("--------------------");
        }
    }


    private void AutoresVivos() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el año para buscar autores vivos:");
        int ano = scanner.nextInt();
        scanner.nextLine(); // Limpiar el buffer de entrada

        // Buscar autores vivos según los criterios definidos
        Iterable<Autor> autores = autorRepository.findByAnoFallecimientoGreaterThanAndAnoNacimientoLessThanEqual(ano, ano);

        if (autores.iterator().hasNext()) {
            System.out.println("-----------------------------------\n" +
                    "     AUTORES VIVOS EN " + ano +".\n" +
                    "-----------------------------------");
            for (Autor autor : autores) {
                // Verificar y mostrar información del autor
                System.out.println("AUTOR: " + autor.getNombre());
                System.out.println("FECHA DE NACIMIENTO: " + autor.getAnoNacimiento());

                // Mostrar la fecha de fallecimiento solo si el autor ha fallecido
                if (autor.getAnoFallecimiento() > 0) {
                    System.out.println("FECHA DE FALLECIMIENTO: " + autor.getAnoFallecimiento());
                }

                // Obtener los libros asociados al autor
                List<Libro> libros = libroRepository.findByAutor(autor);
                if (!libros.isEmpty()) {
                    System.out.println("LIBROS:");
                    for (Libro libro : libros) {
                        System.out.println("  - " + libro.getTitulo());
                    }
                } else {
                    System.out.println("Libros: No tiene libros registrados.");
                }
                System.out.println("--------------------");
            }
        } else {
            System.out.println("No se encontraron autores vivos en el año " + ano);
        }
    }



    private void LibroPorIdioma() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Seleccione el idioma para buscar libros:");
        System.out.println("1. es - Español");
        System.out.println("2. en - Inglés");
        System.out.println("3. fr - Francés");
        System.out.println("4. pt - Portugués");
        System.out.print("Por favor, seleccione una opción: ");

        String idioma = "";
        int option = scanner.nextInt();
        scanner.nextLine(); // Limpiar el buffer de entrada

        switch (option) {
            case 1:
                idioma = "es";
                break;
            case 2:
                idioma = "en";
                break;
            case 3:
                idioma = "fr";
                break;
            case 4:
                idioma = "pt";
                break;
            default:
                System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                return;
        }

        Iterable<Libro> iterableLibros = libroRepository.findByIdioma(idioma);
        Set<Libro> librosUnicos = new HashSet<>();
        iterableLibros.forEach(librosUnicos::add);

        System.out.println("Lista de libros en el idioma " + idioma + ":");
        for (Libro libro : librosUnicos) {
            System.out.printf("""
            -------------------
                  LIBRO
            -------------------
            TITULO: %s
            AUTOR: %s
            IDIOMA: %s
            NUMERO DE DESCARGAS: %d
            --------------------
            """, libro.getTitulo(), libro.getAutor().getNombre(), libro.getIdioma(), libro.getNumeroDescargas());
        }

        System.out.println("Cantidad de libros en el idioma " + idioma + ": " + librosUnicos.size());
    }


    private void cerrar() {
        System.out.println("""
                ------------------
                     CERRANDO \s
                ------------------

                ..........
                 .      .
                  .    .
                   .  .
                    ..
                    ..
                   .  .
                  .    .
                 .      .
                ..........
                """);

        System.out.println("""
                --------------------
                     CERRANDO... \s
                --------------------""");

        System.out.println("Gracias por usar el sistema de gestión de libros.");
        System.out.println("--------------------------------------------------");
        System.out.println("                                                   ");
        System.exit(0);

    }
}
