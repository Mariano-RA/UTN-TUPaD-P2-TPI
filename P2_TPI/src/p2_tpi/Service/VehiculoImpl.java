package p2_tpi.Dao;

import p2_tpi.Config.DatabaseConnection;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;
import p2_tpi.Models.Vehiculo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    // ============================================================
    // INSERT
    // ============================================================
    public Long insert(Vehiculo v) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return insertTx(v, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando Vehiculo: " + e.getMessage(), e);
        }
    }

    public Long insertTx(Vehiculo v, Connection con) {
        String sql = "INSERT INTO Vehiculo " +
                "(dominio, marca, modelo, anio, seguro_id, eliminado) " +
                "VALUES (?,?,?,?,?,0)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAnio());
            ps.setLong(5, v.getSeguro().getId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    v.setId(id);
                    v.setEliminado(false);
                    return id;
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error insertando Vehiculo (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public int update(Vehiculo v) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return updateTx(v, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando Vehiculo: " + e.getMessage(), e);
        }
    }

    public int updateTx(Vehiculo v, Connection con) {
        String sql = "UPDATE Vehiculo " +
                "SET dominio=?, marca=?, modelo=?, anio=? " +
                "WHERE id=? AND eliminado=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAnio());
            ps.setLong(5, v.getId());

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando Vehiculo (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // SOFT DELETE
    // ============================================================
    public int softDelete(Long id) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return softDeleteTx(id, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando Vehiculo: " + e.getMessage(), e);
        }
    }

    public int softDeleteTx(Long id, Connection con) {
        String sql = "UPDATE Vehiculo SET eliminado=1 WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando Vehiculo (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // FINDERS
    // ============================================================
    public Vehiculo findById(Long id) {
        String sql =
                "SELECT v.id, v.dominio, v.marca, v.modelo, v.anio, v.seguro_id, v.eliminado, " +
                "       s.aseguradora, s.nro_poliza, s.vencimiento, s.tipo_cobertura_id, s.eliminado AS seg_eliminado " +
                "FROM Vehiculo v " +
                "LEFT JOIN SeguroVehicular s ON v.seguro_id = s.id " +
                "WHERE v.id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando Vehiculo por id: " + e.getMessage(), e);
        }
    }

    public Vehiculo findByDominio(String dominio) {
        String sql =
                "SELECT v.id, v.dominio, v.marca, v.modelo, v.anio, v.seguro_id, v.eliminado, " +

