package p2_tpi.Service;

import p2_tpi.Dao.TipoCoberturaDAO;
import p2_tpi.Models.TipoCobertura;

import java.util.List;

public class TipoCoberturaImpl {

    private final TipoCoberturaDAO dao = new TipoCoberturaDAO();

    public Long crear(TipoCobertura t) {
        if (t == null) {
            throw new RuntimeException("Falta el objeto cobertura.");
        }
        if (isBlank(t.getCodigo())) {
            throw new RuntimeException("El código es obligatorio.");
        }
        if (isBlank(t.getNombre())) {
            throw new RuntimeException("El nombre es obligatorio.");
        }
        if (dao.existsByCodigo(t.getCodigo())) {
            throw new RuntimeException("El código ya existe: " + t.getCodigo());
        }
        t.setEliminado(false);
        return dao.insert(t);
    }

    public int actualizar(TipoCobertura t) {
        if (t == null || t.getId() == null) {
            throw new RuntimeException("Falta id para actualizar.");
        }
        if (isBlank(t.getCodigo())) {
            throw new RuntimeException("El código es obligatorio.");
        }
        if (isBlank(t.getNombre())) {
            throw new RuntimeException("El nombre es obligatorio.");
        }
        if (dao.existsByCodigoExcludingId(t.getCodigo(), t.getId())) {
            throw new RuntimeException("El código ya está usado por otra cobertura.");
        }
        return dao.update(t);
    }

    public int eliminar(Long id) {
        if (id == null) {
            throw new RuntimeException("Falta id para eliminar.");
        }
        return dao.softDelete(id);
    }

    public TipoCobertura obtener(Long id) {
        if (id == null) {
            throw new RuntimeException("Falta id.");
        }
        TipoCobertura t = dao.findById(id);
        if (t == null || t.isEliminado()) {
            throw new RuntimeException("Cobertura no encontrada.");
        }
        return t;
    }

    public TipoCobertura buscarPorCodigo(String codigo) {
        if (isBlank(codigo)) {
            throw new RuntimeException("Falta código.");
        }
        TipoCobertura t = dao.findByCodigo(codigo.trim());
        if (t == null || t.isEliminado()) {
            throw new RuntimeException("No existe cobertura con ese código.");
        }
        return t;
    }

    public List<TipoCobertura> listar() {
        return dao.findAll();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
