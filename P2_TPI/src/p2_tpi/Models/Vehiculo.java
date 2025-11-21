package p2_tpi.Models;

public class Vehiculo extends Base {

    private String dominio;            // UNIQUE
    private String marca;
    private String modelo;
    private Integer anio;
    private String nroChasis;          // UNIQUE opcional
    private SeguroVehicular seguro;    // 1→1

    // ==========================
    // GETTERS / SETTERS
    // ==========================

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getNroChasis() {
        return nroChasis;
    }

    public void setNroChasis(String nroChasis) {
        this.nroChasis = nroChasis;
    }

    public SeguroVehicular getSeguro() {
        return seguro;
    }

    public void setSeguro(SeguroVehicular seguro) {
        this.seguro = seguro;
    }

    @Override
    public String toString() {
        return "Vehiculo{" +
                "id=" + getId() +
                ", dominio='" + dominio + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", anio=" + anio +
                ", nroChasis='" + nroChasis + '\'' +
                ", eliminado=" + isEliminado() +
                ", seguro=" + (seguro != null ? seguro.getNroPoliza() : null) +
                '}';
    }
}

