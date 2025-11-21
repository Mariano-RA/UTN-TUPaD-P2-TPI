package p2_tpi.Dao;

import p2_tpi.Config.DatabaseConnection;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeguroVehicularDAO {

    // ==========================
    // INSERT
    // ==========================

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
            if (s.getVencimiento() != null) {
                ps.setDate(4, Date.valueOf(s.getVencimiento()));
            } else {
                ps.setNull(4, Types.DATE);
            }

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

    // ==========================
    // UPDATE
    // ==========================

    public int update(SeguroVehicular s) {
        String sql = "UPDATE SeguroVehicular " +
                "SET aseguradora=?, nro_poliza=?, tipo_cobertura_id=?, vencimiento=? " +
                "WHERE id=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getAseguradora());
            ps.setString(2, s.getNroPoliza());
            ps.setLong(3, s.getTipoCobertura().getId());
            if (s.getVencimiento() != null) {
                ps.setDate(4, Date.valueOf(s.getVencimiento()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setLong(5, s.getId());

            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando SeguroVehicular: " + e.getMessage(), e);
        }
    }

    // ==========================
    // SOFT DELETE
    // ==========================

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

    // ==========================
    // FINDERS
    // ==========================

    public SeguroVehicular findById(Long id) {
        String sql =
                "SELECT s.id, s.aseguradora, s.nro_poliza, s.vencimiento, s.eliminado AS s_eliminado, " +
                "       tc.id AS tc_id, tc.codigo, tc.nombre, tc.eliminado AS tc_eliminado, tc.orden " +
                "FROM SeguroVehicular s " +
                "JOIN TipoCobertura tc ON s.tipo_cobertura_id = tc.id " +
                "WHERE s.id = ?";
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

    public List<SeguroVehicular> findAll() {
        String sql =
                "SELECT s.id, s.aseguradora, s.nro_poliza, s.vencimiento, s.eliminado AS s_eliminado, " +
                "       tc.id AS tc_id, tc.codigo, tc.nombre, tc.eliminado AS tc_eliminado, tc.orden " +
                "FROM SeguroVehicular s " +
                "JOIN TipoCobertura tc ON s.tipo_cobertura_id = tc.id " +
                "WHERE s.eliminado = 0 " +
                "ORDER BY s.nro_poliza";
        List<SeguroVehicular> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando Seguros: " + e.getMessage(), e);
        }
    }

    public boolean existsByNroPoliza(String nro) {
        String sql = "SELECT 1 FROM SeguroVehicular WHERE nro_poliza=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando nro de póliza: " + e.getMessage(), e);
        }
    }

    public boolean existsByNroPolizaExcludingId(String nro, Long id) {
        String sql = "SELECT 1 FROM SeguroVehicular WHERE nro_poliza=? AND eliminado=0 AND id<>?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nro);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando nro de póliza (excluyendo id): " + e.getMessage(), e);
        }
    }

    // ==========================
    // MAPEO
    // ==========================

    private SeguroVehicular map(ResultSet rs) throws SQLException {
        SeguroVehicular s = new SeguroVehicular();
        s.setId(rs.getLong("id"));
        s.setAseguradora(rs.getString("aseguradora"));
        s.setNroPoliza(rs.getString("nro_poliza"));
        Date vto = rs.getDate("vencimiento");
        s.setVencimiento(vto != null ? vto.toLocalDate() : null);
        s.setEliminado(rs.getBoolean("s_eliminado"));

        Long tcId = rs.getLong("tc_id");
        if (!rs.wasNull()) {
            TipoCobertura tc = new TipoCobertura();
            tc.setId(tcId);
            tc.setCodigo(rs.getString("codigo"));
            tc.setNombre(rs.getString("nombre"));
            tc.setEliminado(rs.getBoolean("tc_eliminado"));
            Object ordenObj = rs.getObject("orden");
            tc.setOrden(ordenObj != null ? rs.getInt("orden") : null);
            s.setTipoCobertura(tc);
        }

        return s;
    }
}
