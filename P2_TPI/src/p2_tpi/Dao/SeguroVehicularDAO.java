package p2_tpi.Dao;

import p2_tpi.Config.DatabaseConnection;
import p2_tpi.Models.TipoCobertura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoCoberturaDAO {

    // ============================================================
    // INSERT
    // ============================================================
    public Long insert(TipoCobertura tc) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return insertTx(tc, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando TipoCobertura: " + e.getMessage(), e);
        }
    }

    public Long insertTx(TipoCobertura tc, Connection con) {
        String sql = "INSERT INTO TipoCobertura (codigo, nombre, eliminado, orden) VALUES (?,?,0,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tc.getCodigo());
            ps.setString(2, tc.getNombre());
            if (tc.getOrden() != null) {
                ps.setInt(3, tc.getOrden());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    tc.setId(id);
                    tc.setEliminado(false);
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando TipoCobertura (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public int update(TipoCobertura tc) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return updateTx(tc, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando TipoCobertura: " + e.getMessage(), e);
        }
    }

    public int updateTx(TipoCobertura tc, Connection con) {
        String sql = "UPDATE TipoCobertura SET codigo=?, nombre=?, orden=? WHERE id=? AND eliminado=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tc.getCodigo());
            ps.setString(2, tc.getNombre());
            if (tc.getOrden() != null) {
                ps.setInt(3, tc.getOrden());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setLong(4, tc.getId());

            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando TipoCobertura (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // SOFT DELETE
    // ============================================================
    public int softDelete(Long id) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return softDeleteTx(id, con);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando TipoCobertura: " + e.getMessage(), e);
        }
    }

    public int softDeleteTx(Long id, Connection con) {
        String sql = "UPDATE TipoCobertura SET eliminado=1 WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando TipoCobertura (Tx): " + e.getMessage(), e);
        }
    }

    // ============================================================
    // FINDERS
    // ============================================================
    public TipoCobertura findById(Long id) {
        String sql = "SELECT id, codigo, nombre, eliminado, orden FROM TipoCobertura WHERE id=?";
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
            throw new RuntimeException("Error buscando TipoCobertura por id: " + e.getMessage(), e);
        }
    }

    public TipoCobertura findByCodigo(String codigo) {
        String sql = "SELECT id, codigo, nombre, eliminado, orden FROM TipoCobertura WHERE codigo=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando TipoCobertura por código: " + e.getMessage(), e);
        }
    }

    public boolean existsByCodigo(String codigo) {
        String sql = "SELECT 1 FROM TipoCobertura WHERE codigo=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando código de TipoCobertura: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // VALIDACIÓN EXCLUYENDO ID (para update)
    // ============================================================
    public boolean existsByCodigoExcludingId(String codigo, Long id) {
        // Si no hay id (alta), reutilizamos el existsByCodigo normal
        if (id == null) {
            return existsByCodigo(codigo);
        }

        String sql = "SELECT 1 FROM TipoCobertura WHERE codigo=? AND eliminado=0 AND id<>?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setLong(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando código de TipoCobertura excluyendo id: " + e.getMessage(), e);
        }
    }

    public List<TipoCobertura> findAllActivas() {
        String sql = "SELECT id, codigo, nombre, eliminado, orden " +
                     "FROM TipoCobertura WHERE eliminado=0 ORDER BY orden, nombre";
        List<TipoCobertura> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando Tipos de Cobertura: " + e.getMessage(), e);
        }
    }

    // Método que matchea lo que usa el Service
    public List<TipoCobertura> findAll() {
        return findAllActivas();
    }

    // ============================================================
    // MAPEO
    // ============================================================
    private TipoCobertura map(ResultSet rs) throws SQLException {
        TipoCobertura tc = new TipoCobertura();
        tc.setId(rs.getLong("id"));
        tc.setCodigo(rs.getString("codigo"));
        tc.setNombre(rs.getString("nombre"));
        tc.setEliminado(rs.getBoolean("eliminado"));
        Object ordenObj = rs.getObject("orden");
        tc.setOrden(ordenObj != null ? rs.getInt("orden") : null);
        return tc;
    }
}
