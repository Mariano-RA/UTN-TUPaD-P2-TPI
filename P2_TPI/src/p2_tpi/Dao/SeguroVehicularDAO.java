package p2_tpi.Dao;

import p2_tpi.Config.DatabaseConnection;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeguroVehicularDAO {

    // ============================================================
    // INSERT
    // ============================================================
    public Long insert(SeguroVehicular s) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return insertTx(s, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando SeguroVehicular: " + e.getMessage(), e);
        }
    }

    public Long insertTx(SeguroVehicular s, Connection con) {
        String sql = "INSERT INTO SeguroVehicular " +
                "(aseguradora, nro_poliza, tipo_cobertura_id, vencimiento, eliminado) " +
                "VALUES (?,?,?,?,0)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getAseguradora());
            ps.setString(2, s.getNroPoliza());
            ps.setLong(3, s.getTipoCobertura().getId());
            ps.setDate(4, Date.valueOf(s.getVencimiento()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    s.setId(id);
                    s.setEliminado(false);
                    return id;
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error insertando SeguroVehicular (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public int update(SeguroVehicular s) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return updateTx(s, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando SeguroVehicular: " + e.getMessage(), e);
        }
    }

    public int updateTx(SeguroVehicular s, Connection con) {
        String sql = "UPDATE SeguroVehicular " +
                "SET aseguradora=?, nro_poliza=?, tipo_cobertura_id=?, vencimiento=? " +
                "WHERE id=? AND eliminado=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getAseguradora());
            ps.setString(2, s.getNroPoliza());
            ps.setLong(3, s.getTipoCobertura().getId());
            ps.setDate(4, Date.valueOf(s.getVencimiento()));
            ps.setLong(5, s.getId());

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando SeguroVehicular (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // SOFT DELETE
    // ============================================================
    public int softDelete(Long id) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return softDeleteTx(id, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando SeguroVehicular: " + e.getMessage(), e);
        }
    }

    public int softDeleteTx(Long id, Connection con) {
        String sql = "UPDATE SeguroVehicular SET eliminado=1 WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando SeguroVehicular (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // FINDERS
    // ============================================================
    public SeguroVehicular findById(Long id) {
        String sql = "SELECT id, aseguradora, nro_poliza, tipo_cobertura_id, vencimiento, eliminado " +
                     "FROM SeguroVehicular WHERE id=?";
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
            throw new RuntimeException("Error buscando SeguroVehicular por id: " + e.getMessage(), e);
        }
    }

    public boolean existsByNroPoliza(String nroPoliza) {
        String sql = "SELECT 1 FROM SeguroVehicular WHERE nro_poliza=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nroPoliza);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error verificando nro_poliza de SeguroVehicular: " + e.getMessage(), e);
        }
    }

    public boolean existsByNroPolizaExcludingId(String nroPoliza, Long id) {
        if (id == null) {
            return existsByNroPoliza(nroPoliza);
        }

        String sql = "SELECT 1 FROM SeguroVehicular " +
                     "WHERE nro_poliza=? AND eliminado=0 AND id<>?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nroPoliza);
            ps.setLong(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error verificando nro_poliza excluyendo id: " + e.getMessage(), e);
        }
    }

    public List<SeguroVehicular> findAllActivos() {
        String sql = "SELECT id, aseguradora, nro_poliza, tipo_cobertura_id, vencimiento, eliminado " +
                     "FROM SeguroVehicular WHERE eliminado=0 ORDER BY vencimiento DESC";
        List<SeguroVehicular> list = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Error listando Seguros Vehiculares: " + e.getMessage(), e);
        }
    }

    public List<SeguroVehicular> findAll() {
        return findAllActivos();
    }

    // ============================================================
    // MAPEO
    // ============================================================
    private SeguroVehicular map(ResultSet rs) throws SQLException {
        SeguroVehicular s = new SeguroVehicular();

        s.setId(rs.getLong("id"));
        s.setAseguradora(rs.getString("aseguradora"));
        s.setNroPoliza(rs.getString("nro_poliza"));
        Date vto = rs.getDate("vencimiento");
        LocalDate vtoLocal = (vto != null ? vto.toLocalDate() : null);
        s.setVencimiento(vtoLocal);
        s.setEliminado(rs.getBoolean("eliminado"));

        TipoCobertura tc = new TipoCobertura();
        tc.setId(rs.getLong("tipo_cobertura_id"));
        s.setTipoCobertura(tc);

        return s;
    }
}
