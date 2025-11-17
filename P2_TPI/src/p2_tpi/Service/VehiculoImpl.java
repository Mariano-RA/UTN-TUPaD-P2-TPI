// prog2int/Service/VehiculoServiceImpl.java
package prog2int.Service;

import prog2int.Config.DatabaseConnection;
import prog2int.Config.TransactionManager;
import prog2int.Dao.SeguroVehicularDAO;
import prog2int.Dao.VehiculoDAO;
import prog2int.Models.SeguroVehicular;
import prog2int.Models.Vehiculo;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class VehiculoImpl {

    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final SeguroVehicularDAO seguroDAO = new SeguroVehicularDAO();

    public Long crearVehiculoConSeguro(Vehiculo v) {
        validarObligatorios(v);
        if (vehiculoDAO.existsByDominio(v.getDominio())) {
            throw new RuntimeException("Ya existe un vehículo con ese dominio.");
        }

        SeguroVehicular s = v.getSeguro();
        if (s == null) {
            throw new RuntimeException("Debe asociar un seguro.");
        }
        if (s.getVencimiento() == null || s.getVencimiento().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de vencimiento no puede estar vencida.");
        }
        // Nota: tu SQL deja nro_poliza NULL, pero lo exigimos aquí (regla de negocio)
        if (s.getNroPoliza() == null || s.getNroPoliza().trim().isEmpty()) {
            throw new RuntimeException("El número de póliza es obligatorio.");
        }
        if (seguroDAO.existsByNroPoliza(s.getNroPoliza())) {
            throw new RuntimeException("La póliza ya existe: " + s.getNroPoliza());
        }

        try (Connection con = DatabaseConnection.getConnection(); TransactionManager tx = new TransactionManager()) {

            Long idSeguro = seguroDAO.insertTx(s, con);
            s.setId(idSeguro);

            Long idVehiculo = vehiculoDAO.insertTx(v, con);

            tx.commit();
            return idVehiculo;

        } catch (Exception e) {
            throw new RuntimeException("Error creando vehículo con seguro: " + e.getMessage(), e);
        }
    }

    public int actualizar(Vehiculo v) {
        if (v.getId() == null) {
            throw new RuntimeException("Falta id de vehículo.");
        }
        validarObligatorios(v);
        if (vehiculoDAO.existsByDominioExcludingId(v.getDominio(), v.getId())) {
            throw new RuntimeException("El dominio ya pertenece a otro vehículo.");
        }
        return vehiculoDAO.update(v);
    }

    public int eliminar(Long idVehiculo) {
        if (idVehiculo == null) {
            throw new RuntimeException("Falta id de vehículo.");
        }
        Vehiculo v = vehiculoDAO.findById(idVehiculo);
        if (v == null || v.isEliminado()) {
            throw new RuntimeException("Vehículo no encontrado.");
        }

        try (TransactionManager tx = new TransactionManager()) {

            vehiculoDAO.softDelete(idVehiculo);
            seguroDAO.softDelete(v.getSeguro().getId());
            tx.commit();
            return 1;

        } catch (Exception e) {
            throw new RuntimeException("Error eliminando vehículo/seguro: " + e.getMessage(), e);
        }
    }

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
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
