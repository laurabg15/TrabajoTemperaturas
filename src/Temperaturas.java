package entidades;

import java.time.LocalDate;

public class Temperaturas {

    private String ciudad;
    private LocalDate fecha;
    private double temperatura;

    public Temperaturas(String ciudad, LocalDate fecha, double temperatura) {
        this.ciudad = ciudad;
        this.fecha = fecha;
        this.temperatura = temperatura;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

}
