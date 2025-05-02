package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import entidades.Temperaturas;

public class TemperaturaServicio {

    public static List<Temperaturas> getDatos(String nombreArchivo) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");
        try (Stream<String> lineas = Files.lines(Paths.get(nombreArchivo))) {
            return lineas.skip(1)
                    .map(linea -> linea.split(","))
                    .map(textos -> new Temperatura(textos[0],LocalDate.parse(textos[1], formatoFecha),
                            Double.parseDouble(textos[2])))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<String> getCiudades(List<Temperaturas> datos) {
        return datos.stream()
                .map(Temperaturas::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<Temperaturas> filtrar(String ciudad, LocalDate desde, LocalDate hasta,
                                            List<Temperaturas> datos) {
        return datos.stream()
                .filter(dato -> dato.getCiudad().equals(ciudad)
                        && !dato.getFecha().isBefore(desde)
                        && !dato.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    public static Par<List<LocalDate>, List<Double>> extraer(List<Temperaturas> datos) {
        var datosOrdenados = datos.stream()
                .sorted(Comparator.comparing(Temperaturas::getFecha))
                .collect(Collectors.toList());

        var fechas = datosOrdenados.stream().map(Temperaturas::getFecha).collect(Collectors.toList());

        var temperaturas = datosOrdenados.stream().map(Temperaturas::getTemperatura).collect(Collectors.toList());

        return new Par<>(fechas, temperaturas);
    }

    public static double getPromedio(List<Double> valores) {
        return valores.isEmpty() ? 0 : valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double getMaximo(List<Double> valores) {
        return valores.isEmpty() ? 0 : valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    public static double getMinimo(List<Double> valores) {
        return valores.isEmpty() ? 0 : valores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    public static double getModa(List<Double> valores) {
        return valores.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0.0);
    }

    public static Map<String, Double> getEstadisticas(String ciudad, LocalDate desde, LocalDate hasta,
                                                      List<Temperaturas> datos) {
        var datosFiltrados = filtrar(ciudad, desde, hasta, datos);
        var temperaturas = datosFiltrados.stream()
                .map(Temperaturas::getTemperatura)
                .collect(Collectors.toList());

        Map<String, Double> estadisticas = new LinkedHashMap<>();
        estadisticas.put("Promedio", getPromedio(temperaturas));
        estadisticas.put("Máximo", getMaximo(temperaturas));
        estadisticas.put("Mínimo", getMinimo(temperaturas));

        return estadisticas;
    }
}
