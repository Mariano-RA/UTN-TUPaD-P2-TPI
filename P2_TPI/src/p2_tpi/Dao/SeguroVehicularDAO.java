package p2_tpi.Dao;

import p2_tpi.Config.DatabaseConnection;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeguroVehicularDAO {

    // INSERT autónomo
    public Long insert(SeguroVehicular s) {
        String sql = "INSERT INTO SeguroVehicular " +
                "(aseguradora, nro_poliza, tipo_cobertura, vencimiento, eliminado) " +
                "VALUES (?,?,?,?,0)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParamsInsertOrUpdate(ps, s);
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
            throw new RuntimeException("Error insertando SeguroVehicular: " + e.getMessage(), e);
        }
    }

    // INSERT dentro de transacción
    public Long insertTx(SeguroVehicular s, Connection con) {
        String sql = "INSERT INTO SeguroVehicular " +
                "(aseguradora, nro_poliza, tipo_cobertura, vencimiento, eliminado) " +
                "VALUES (?,?,?,?,0)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParamsInsertOrUpdate(ps, s);
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

    private void setParamsInsertOrUpdate(PreparedStatement ps, SeguroVehicular s) throws SQLException {
        ps.setString(1, s.getAseguradora());
        ps.setString(2, s.getNroPoliza());
        if (s.getTipoCobertura() == null || s.getTipoCobertura().getId() == null) {
            throw new SQLException("tipo_cobertura (FK) no puede ser null");
        } else {
            ps.setLong(3, s.getTipoCobertura().getId());
        }
        if (s.getVencimiento() != null) {
            ps.setDate(4, Date.valueOf(s.getVencimiento()));
        } else {
            throw new SQLException("vencimiento no puede ser null");
        }
    }

    // UPDATE
    public int update(SeguroVehicular s) {
        String sql = "UPDATE SeguroVehicular " +
                "SET aseguradora=?, nro_poliza=?, tipo_cobertura=?, vencimiento=? " +
                "WHERE id=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            setParamsInsertOrUpdate(ps, s);
            ps.setLong(5, s.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando SeguroVehicular: " + e.getMessage(), e);
        }
    }

    // SOFT DELETE
    public int softDelete(Long id) {
        String sql = "UPDATE SeguroVehicular SET eliminado=1 WHERE id=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error haciendo soft delete de SeguroVehicular: " + e.getMessage(), e);
        }
    }

    // SOFT DELETE dentro de Tx
    public int softDeleteTx(Long id, Connection con) {
        String sql = "UPDATE SeguroVehicular SET eliminado=1 WHERE id=? AND eliminado=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error haciendo soft delete de SeguroVehicular (Tx): " + e.getMessage(), e);
        }
    }

    // BASE SELECT con join a TipoCobertura
    private static final String BASE_SELECT = "SELECT " +
            "s.id          AS s_id, " +
            "s.aseguradora AS aseguradora, " +
            "s.nro_poliza  AS nro_poliza, " +
            "s.vencimiento AS vencimiento, " +
            "s.eliminado   AS s_eliminado, " +
            "tc.id         AS tc_id, " +
            "tc.codigo     AS codigo, " +
            "tc.nombre     AS nombre, " +
            "tc.orden      AS orden, " +
            "tc.eliminado  AS tc_eliminado " +
            "FROM SeguroVehicular s " +
            "JOIN TipoCobertura tc ON tc.id = s.tipo_cobertura ";

    public SeguroVehicular findById(Long id) {
        String sql = BASE_SELECT + "WHERE s.id=? AND s.eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando SeguroVehicular por id: " + e.getMessage(), e);
        }
    }

    public SeguroVehicular findByNroPoliza(String poliza) {
        String sql = BASE_SELECT + "WHERE s.nro_poliza=? AND s.eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, poliza);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando SeguroVehicular por póliza: " + e.getMessage(), e);
        }
    }

    public List<SeguroVehicular> findAllActivos() {
        String sql = BASE_SELECT + "WHERE s.eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            List<SeguroVehicular> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(map(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando seguros: " + e.getMessage(), e);
        }
    }

    // EXISTENCIA / UNICIDAD
    public boolean existsByNroPoliza(String poliza) {
        String sql = "SELECT COUNT(*) FROM SeguroVehicular WHERE nro_poliza=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, poliza);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
              throw new RuntimeException("Error verificando existencia de póliza: " + e.getMessage(), e);
        }
    }

    public boolean existsByNroPolizaExcludingId(String poliza, Long id) {
        String sql = "SELECT COUNT(*) FROM SeguroVehicular " +
                "WHERE nro_poliza=? AND id<>? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, poliza);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando unicidad de póliza: " + e.getMessage(), e);
        }
    }

    // MAPEO
    private SeguroVehicular map(ResultSet rs) throws SQLException {
        TipoCobertura tc = new TipoCobertura();
        tc.setId(rs.getLong("tc_id"));
        tc.setCodigo(rs.getString("codigo"));
        tc.setNombre(rs.getString("nombre"));
        tc.setEliminado(rs.getBoolean("tc_eliminado"));
        Object ordenObj = rs.getObject("orden");
        tc.setOrden(ordenObj != null ? rs.getInt("orden") : null);

        SeguroVehicular s = new SeguroVehicular();
        s.setId(rs.getLong("s_id"));
        s.setAseguradora(rs.getString("aseguradora"));
        s.setNroPoliza(rs.getString("nro_poliza"));
        Date vto = rs.getDate("vencimiento");
        s.setVencimiento(vto != null ? vto.toLocalDate() : null);
        s.setEliminado(rs.getBoolean("s_eliminado"));
        s.setTipoCobertura(tc);

        return s;
    }
}
