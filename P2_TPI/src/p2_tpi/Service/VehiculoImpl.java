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
                "       s.aseguradora, s.nro_poliza, s.vencimiento, s.tipo_cobertura_id, s.eliminado AS seg_eliminado " +
                "FROM Vehiculo v " +
                "LEFT JOIN SeguroVehicular s ON v.seguro_id = s.id " +
                "WHERE v.dominio = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dominio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando Vehiculo por dominio: " + e.getMessage(), e);
        }
    }

    public boolean existsByDominio(String dominio) {
        String sql = "SELECT 1 FROM Vehiculo WHERE dominio=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dominio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error verificando dominio de Vehiculo: " + e.getMessage(), e);
        }
    }

    public boolean existsByDominioExcludingId(String dominio, Long id) {
        if (id == null) {
            return existsByDominio(dominio);
        }
        String sql = "SELECT 1 FROM Vehiculo WHERE dominio=? AND eliminado=0 AND id<>?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dominio);
            ps.setLong(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error verificando dominio excluyendo id: " + e.getMessage(), e);
        }
    }

    public List<Vehiculo> findAll() {
        String sql =
                "SELECT v.id, v.dominio, v.marca, v.modelo, v.anio, v.seguro_id, v.eliminado, " +
                "       s.aseguradora, s.nro_poliza, s.vencimiento, s.tipo_cobertura_id, s.eliminado AS seg_eliminado " +
                "FROM Vehiculo v " +
                "LEFT JOIN SeguroVehicular s ON v.seguro_id = s.id " +
                "WHERE v.eliminado=0 " +
                "ORDER BY v.dominio";
        List<Vehiculo> list = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Error listando Vehiculos: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // MAPEO
    // ============================================================
    private Vehiculo map(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getLong("id"));
        v.setDominio(rs.getString("dominio"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        v.setAnio(rs.getInt("anio"));
        v.setEliminado(rs.getBoolean("eliminado"));

        long seguroId = rs.getLong("seguro_id");
        if (!rs.wasNull() && seguroId > 0) {
            SeguroVehicular s = new SeguroVehicular();
            s.setId(seguroId);
            s.setAseguradora(rs.getString("aseguradora"));
            s.setNroPoliza(rs.getString("nro_poliza"));

            Date vto = rs.getDate("vencimiento");
            LocalDate vtoLocal = (vto != null ? vto.toLocalDate() : null);
            s.setVencimiento(vtoLocal);
            s.setEliminado(rs.getBoolean("seg_eliminado"));

            long tcId = rs.getLong("tipo_cobertura_id");
            if (!rs.wasNull() && tcId > 0) {
                TipoCobertura tc = new TipoCobertura();
                tc.setId(tcId);
                s.setTipoCobertura(tc);
            }

            v.setSeguro(s);
        }

        return v;
    }
}
