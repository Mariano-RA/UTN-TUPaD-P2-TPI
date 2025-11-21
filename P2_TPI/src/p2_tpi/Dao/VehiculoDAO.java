package p2_tpi.Service;

import p2_tpi.Config.TransactionManager;
import p2_tpi.Dao.SeguroVehicularDAO;
import p2_tpi.Dao.VehiculoDAO;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.Vehiculo;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class VehiculoImpl {

    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final SeguroVehicularDAO seguroDAO = new SeguroVehicularDAO();

    // ============================================================
    // CREAR VEHÍCULO + SEGURO (TRANSACCIÓN)
    // ============================================================
    public Long crearVehiculoConSeguro(Vehiculo v) {
        validarObligatorios(v);

        // Dominio único
        if (vehiculoDAO.existsByDominio(v.getDominio())) {
            throw new RuntimeException("Ya existe un vehículo con ese dominio.");
        }

        // Validación de seguro asociado
        SeguroVehicular s = v.getSeguro();
        if (s == null) {
            throw new RuntimeException("Debe asociar un seguro al vehículo.");
        }
        if (s.getVencimiento() == null || s.getVencimiento().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de vencimiento del seguro no puede estar vencida.");
        }
        if (s.getNroPoliza() == null || s.getNroPoliza().trim().isEmpty()) {
            throw new RuntimeException("El número de póliza es obligatorio.");
        }
        if (seguroDAO.existsByNroPoliza(s.getNroPoliza())) {
            throw new RuntimeException("La póliza ya existe: " + s.getNroPoliza());
        }

        try (TransactionManager tx = new TransactionManager()) {
            Connection con = tx.getConnection();

            // 1) Insertar seguro
            Long idSeguro = seguroDAO.insertTx(s, con);
            s.setId(idSeguro);

            // 2) Insertar vehículo referenciando ese seguro
            Long idVehiculo = vehiculoDAO.insertTx(v, con);

            // 3) Confirmar transacción
            tx.commit();
            return idVehiculo;

        } catch (Exception e) {
            throw new RuntimeException("Error creando vehículo con seguro: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // ACTUALIZAR SOLO VEHÍCULO
    // ============================================================
    public int actualizar(Vehiculo v) {
        if (v.getId() == null) {
            throw new RuntimeException("Falta id de vehículo para actualizar.");
        }
        validarObligatorios(v);

        if (vehiculoDAO.existsByDominioExcludingId(v.getDominio(), v.getId())) {
            throw new RuntimeException("El dominio ya pertenece a otro vehículo.");
        }

        return vehiculoDAO.update(v);
    }

    // ============================================================
    // ELIMINAR VEHÍCULO + SEGURO (SOFT DELETE, TRANSACCIÓN)
    // ============================================================
    public int eliminar(Long idVehiculo) {
        if (idVehiculo == null) {
            throw new RuntimeException("Falta id de vehículo para eliminar.");
        }

        Vehiculo v = vehiculoDAO.findById(idVehiculo);
        if (v == null || v.isEliminado()) {
            throw new RuntimeException("Vehículo no encontrado.");
        }
        if (v.getSeguro() == null || v.getSeguro().getId() == null) {
            throw new RuntimeException("El vehículo no tiene seguro asociado válido.");
        }

        try (TransactionManager tx = new TransactionManager()) {
            Connection con = tx.getConnection();

            // 1) Baja lógica del vehículo
            vehiculoDAO.softDeleteTx(idVehiculo, con);

            // 2) Baja lógica del seguro asociado
            seguroDAO.softDeleteTx(v.getSeguro().getId(), con);

            // 3) Confirmar transacción
            tx.commit();
            return 1;

        } catch (Exception e) {
            throw new RuntimeException("Error eliminando vehículo y seguro: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // CONSULTAS
    // ============================================================
    public Vehiculo buscarPorDominio(String dominio) {
        if (dominio == null || dominio.trim().isEmpty()) {
            throw new RuntimeException("Falta dominio.");
        }
        Vehiculo v = vehiculoDAO.findByDominio(dominio.trim());
        if (v == null || v.isEliminado()) {
            throw new RuntimeException("No existe vehículo con ese dominio.");
        }
        return v;
    }

    public List<Vehiculo> listar() {
        return vehiculoDAO.findAll();
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================
    private void validarObligatorios(Vehiculo v) {
        if (v == null) {
            throw new RuntimeException("Falta el objeto vehículo.");
        }
        if (isBlank(v.getDominio())) {
            throw new RuntimeException("El dominio es obligatorio.");
        }
        if (isBlank(v.getMarca())) {
            throw new RuntimeException("La marca es obligatoria.");
        }
        if (isBlank(v.getModelo())) {
            throw new RuntimeException("El modelo es obligatorio.");
        }
        if (v.getAnio() <= 0) {
            throw new RuntimeException("El año del vehículo debe ser válido.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

