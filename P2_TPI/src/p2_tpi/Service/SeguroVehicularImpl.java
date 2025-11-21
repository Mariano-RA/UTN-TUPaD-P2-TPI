package p2_tpi.Service;

import p2_tpi.Dao.SeguroVehicularDAO;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;

import java.time.LocalDate;
import java.util.List;

public class SeguroVehicularImpl {

    private final SeguroVehicularDAO dao = new SeguroVehicularDAO();

    // ============================================================
    // MÉTODOS PÚBLICOS (CRUD)
    // ============================================================

    public Long crear(SeguroVehicular s) throws Exception {
        validarCrear(s);

        // Insert normal (sin transacción)
        Long id = dao.insert(s);
        return id;
    }

    public int actualizar(SeguroVehicular s) throws Exception {
        validarActualizar(s);
        return dao.update(s);
    }

    public int eliminar(Long id) throws Exception {
        if (id == null) {
            throw new Exception("ID del seguro no puede ser nulo.");
        }
        return dao.softDelete(id);
    }

    public SeguroVehicular buscarPorPoliza(String nroPoliza) throws Exception {
        if (nroPoliza == null || nroPoliza.isBlank()) {
            throw new Exception("El número de póliza no puede estar vacío.");
        }
        return dao.findByNroPoliza(nroPoliza);
    }

    public SeguroVehicular buscarPorId(Long id) throws Exception {
        if (id == null) {
            throw new Exception("ID del seguro no puede ser nulo.");
        }
        return dao.findById(id);
    }

    public List<SeguroVehicular> listar() {
        return dao.findAllActivos();
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================

    private void validarCrear(SeguroVehicular s) throws Exception {
        if (s == null) throw new Exception("Seguro no puede ser null.");

        validarAseguradora(s.getAseguradora());
        validarPolizaNueva(s.getNroPoliza());
        validarCobertura(s.getTipoCobertura());
        validarVencimiento(s.getVencimiento());
    }

    private void validarActualizar(SeguroVehicular s) throws Exception {
        if (s == null || s.getId() == null) {
            throw new Exception("Seguro o ID nulo.");
        }

        validarAseguradora(s.getAseguradora());
        validarPolizaExistente(s.getNroPoliza(), s.getId());
        validarCobertura(s.getTipoCobertura());
        validarVencimiento(s.getVencimiento());
    }

    private void validarAseguradora(String aseguradora) throws Exception {
        if (aseguradora == null || aseguradora.isBlank()) {
            throw new Exception("La aseguradora no puede estar vacía.");
        }
    }

    private void validarPolizaNueva(String poliza) throws Exception {
        if (poliza == null || poliza.isBlank()) {
            throw new Exception("El número de póliza no puede estar vacío.");
        }
        if (dao.existsByNroPoliza(poliza)) {
            throw new Exception("Ya existe un seguro con ese número de póliza.");
        }
    }

    private void validarPolizaExistente(String poliza, Long id) throws Exception {
        if (poliza == null || poliza.isBlank()) {
            throw new Exception("El número de póliza no puede estar vacío.");
        }
        if (dao.existsByNroPolizaExcludingId(poliza, id)) {
            throw new Exception("Otro seguro ya tiene ese número de póliza.");
        }
    }

    private void validarCobertura(TipoCobertura tc) throws Exception {
        if (tc == null || tc.getId() == null) {
            throw new Exception("Debe seleccionar un tipo de cobertura válido.");
        }
    }

    private void validarVencimiento(LocalDate vto) throws Exception {
        if (vto == null) {
            throw new Exception("La fecha de vencimiento no puede ser nula.");
        }
        if (vto.isBefore(LocalDate.now())) {
            throw new Exception("La fecha de vencimiento debe ser futura.");
        }
    }
}
