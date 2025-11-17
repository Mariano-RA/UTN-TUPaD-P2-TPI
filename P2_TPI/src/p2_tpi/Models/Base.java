package p2_tpi.Models;

public abstract class Base {
    protected Long id;
    protected boolean eliminado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }
}
