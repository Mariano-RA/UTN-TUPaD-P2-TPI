// prog2int/Service/SeguroVehicularServiceImpl.java
package p2_tpi.Service;

import p2_tpi.Dao.SeguroVehicularDAO;
import p2_tpi.Dao.TipoCoberturaDAO;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;

import java.time.LocalDate;
import java.util.List;

public class SeguroVehicularImpl {

    private final SeguroVehicularDAO seguroDAO = new SeguroVehicularDAO();
    private final TipoCoberturaDAO coberturaDAO = new TipoCoberturaDAO();

    public Long crear(SeguroVehicular s) {
        validarObligatorios(s); // <-- exige nroPoliza no nula/ni vacía
        if (seguroDAO.existsByNroPoliza(s.getNroPoliza())) {
            throw new RuntimeException("La póliza ya existe: " + s.getNroPoliza());
        }

        TipoCobertura tc = coberturaDAO.findById(s.getTipoCobertura().getId());
        if (tc == null || tc.isEliminado()) {
            throw new RuntimeException("Tipo de cobertura inexistente o eliminado.");
        }
        if (s.getVencimiento().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de vencimiento no puede estar vencida.");
        }

        s.setTipoCobertura(tc);
        s.setEliminado(false);
        return seguroDAO.insert(s);
    }

    public int actualizar(SeguroVehicular s) {
        if (s.getId() == null) {
            throw new RuntimeException("Falta id para actualizar.");
        }
        validarObligatorios(s);
        if (seguroDAO.existsByNroPolizaExcludingId(s.getNroPoliza(), s.getId())) {
            throw new RuntimeException("La póliza ya pertenece a otro registro.");
        }

        TipoCobertura tc = coberturaDAO.findById(s.getTipoCobertura().getId());
        if (tc == null || tc.isEliminado()) {
            throw new RuntimeException("Tipo de cobertura inexistente o eliminado.");
        }
        if (s.getVencimiento().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de vencimiento no puede estar vencida.");
        }

        s.setTipoCobertura(tc);
        return seguroDAO.update(s);
    }

    // ... obtener/buscar/listar igual que ya tenías
    private void validarObligatorios(SeguroVehicular s) {
        if (s == null) {
            throw new RuntimeException("Falta el objeto seguro.");
        }
        if (isBlank(s.getAseguradora())) {
            throw new RuntimeException("La aseguradora es obligatoria.");
        }
        if (isBlank(s.getNroPoliza())) {
            throw new RuntimeException("El número de póliza es obligatorio."); // <-- clave
        }
        if (s.getTipoCobertura() == null || s.getTipoCobertura().getId() == null) {
            throw new RuntimeException("Debe indicar un id de tipo de cobertura.");
        }
        if (s.getVencimiento() == null) {
            throw new RuntimeException("Debe indicar fecha de vencimiento.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
