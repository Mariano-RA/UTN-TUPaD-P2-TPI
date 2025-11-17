package p2_tpi.Models;


public class TipoCobertura extends Base {

    private String codigo;
    private String nombre;
    private Integer orden;   // puede ser null

    public TipoCobertura() {
    }

    public TipoCobertura(Long id, boolean eliminado, String codigo, String nombre, Integer orden) {
        this.setId(id);
        this.setEliminado(eliminado);
        this.codigo = codigo;
        this.nombre = nombre;
        this.orden = orden;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    @Override
    public String toString() {
        return "TipoCobertura{" +
                "id=" + getId() +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", eliminado=" + isEliminado() +
                ", orden=" + orden +
                '}';
    }
}
