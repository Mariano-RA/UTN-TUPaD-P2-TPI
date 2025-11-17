package p2_tpi.Models;


import java.time.LocalDate;

public class SeguroVehicular extends Base {

    private String aseguradora;
    private String nroPoliza;          // UNIQUE
    private TipoCobertura tipoCobertura;
    private LocalDate vencimiento;

    public SeguroVehicular() {
    }

    public SeguroVehicular(Long id, boolean eliminado,
                           String aseguradora,
                           String nroPoliza,
                           TipoCobertura tipoCobertura,
                           LocalDate vencimiento) {
        this.setId(id);
        this.setEliminado(eliminado);
        this.aseguradora = aseguradora;
        this.nroPoliza = nroPoliza;
        this.tipoCobertura = tipoCobertura;
        this.vencimiento = vencimiento;
    }

    public String getAseguradora() {
        return aseguradora;
    }

    public void setAseguradora(String aseguradora) {
        this.aseguradora = aseguradora;
    }

    public String getNroPoliza() {
        return nroPoliza;
    }

    public void setNroPoliza(String nroPoliza) {
        this.nroPoliza = nroPoliza;
    }

    public TipoCobertura getTipoCobertura() {
        return tipoCobertura;
    }

    public void setTipoCobertura(TipoCobertura tipoCobertura) {
        this.tipoCobertura = tipoCobertura;
    }

    public LocalDate getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(LocalDate vencimiento) {
        this.vencimiento = vencimiento;
    }

    @Override
    public String toString() {
        return "SeguroVehicular{" +
                "id=" + getId() +
                ", aseguradora='" + aseguradora + '\'' +
                ", nroPoliza='" + nroPoliza + '\'' +
                ", tipoCobertura=" + (tipoCobertura != null ? tipoCobertura.getCodigo() : null) +
                ", vencimiento=" + vencimiento +
                ", eliminado=" + isEliminado() +
                '}';
    }
}
