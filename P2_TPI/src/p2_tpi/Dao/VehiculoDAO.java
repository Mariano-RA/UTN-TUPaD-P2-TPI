package p2_tpi.Dao;

import p2_tpi.Config.DatabaseConnection;
import p2_tpi.Models.SeguroVehicular;
import p2_tpi.Models.TipoCobertura;
import p2_tpi.Models.Vehiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    public Long insert(Vehiculo v) {
        String sql = "INSERT INTO Vehiculo (dominio, marca, modelo, anio, nro_chasis, seguro_id, eliminado) "
                + "VALUES (?,?,?,?,?,?,0)";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            if (v.getAnio() != null) {
                ps.setInt(4, v.getAnio());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, v.getNroChasis());
            ps.setLong(6, v.getSeguro().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando Vehículo: " + e.getMessage(), e);
        }
    }

    public Long insertTx(Vehiculo v, Connection con) {
        String sql = "INSERT INTO Vehiculo (dominio, marca, modelo, anio, nro_chasis, seguro_id, eliminado) "
                + "VALUES (?,?,?,?,?,?,0)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            if (v.getAnio() != null) {
                ps.setInt(4, v.getAnio());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, v.getNroChasis());
            ps.setLong(6, v.getSeguro().getId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando Vehículo (Tx): " + e.getMessage(), e);
        }
    }

    public int update(Vehiculo v) {
        String sql = "UPDATE Vehiculo SET dominio=?, marca=?, modelo=?, anio=?, nro_chasis=?, seguro_id=? "
                + "WHERE id=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            if (v.getAnio() != null) {
                ps.setInt(4, v.getAnio());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, v.getNroChasis());
            ps.setLong(6, v.getSeguro().getId());
            ps.setLong(7, v.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando Vehículo: " + e.getMessage(), e);
        }
    }

    public int updateTx(Vehiculo v, Connection con) {
        String sql = "UPDATE Vehiculo SET dominio=?, marca=?, modelo=?, anio=?, nro_chasis=?, seguro_id=? "
                + "WHERE id=? AND eliminado=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getDominio());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            if (v.getAnio() != null) {
                ps.setInt(4, v.getAnio());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, v.getNroChasis());
            ps.setLong(6, v.getSeguro().getId());
            ps.setLong(7, v.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando Vehículo (Tx): " + e.getMessage(), e);
        }
    }

    public int softDelete(Long id) {
        String sql = "UPDATE Vehiculo SET eliminado=1 WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando Vehículo: " + e.getMessage(), e);
        }
    }

    public int softDeleteTx(Long id, Connection con) {
        String sql = "UPDATE Vehiculo SET eliminado=1 WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando Vehículo (Tx): " + e.getMessage(), e);
        }
    }

    public Vehiculo findById(Long id) {
        String sql = """
            SELECT v.id, v.dominio, v.marca, v.modelo, v.anio, v.nro_chasis, v.eliminado,
                   s.id as s_id, s.aseguradora, s.nro_poliza, s.vencimiento, s.eliminado as s_eliminado,
                   tc.id as tc_id, tc.codigo, tc.nombre, tc.eliminado as tc_eliminado
            FROM Vehiculo v
            JOIN SeguroVehicular s ON s.id = v.seguro_id
            JOIN TipoCobertura tc ON tc.id = s.tipo_cobertura
            WHERE v.id=?;
        """;
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando Vehículo por id: " + e.getMessage(), e);
        }
    }

    public Vehiculo findByDominio(String dominio) {
        String sql = """
            SELECT v.id, v.dominio, v.marca, v.modelo, v.anio, v.nro_chasis, v.eliminado,
                   s.id as s_id, s.aseguradora, s.nro_poliza, s.vencimiento, s.eliminado as s_eliminado,
                   tc.id as tc_id, tc.codigo, tc.nombre, tc.eliminado as tc_eliminado
            FROM Vehiculo v
            JOIN SeguroVehicular s ON s.id = v.seguro_id
            JOIN TipoCobertura tc ON tc.id = s.tipo_cobertura
            WHERE v.dominio=?;
        """;
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dominio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando Vehículo por dominio: " + e.getMessage(), e);
        }
    }

    public boolean existsByDominio(String dominio) {
        String sql = "SELECT 1 FROM Vehiculo WHERE dominio=? AND eliminado=0";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dominio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando dominio: " + e.getMessage(), e);
        }
    }

    public boolean existsByDominioExcludingId(String dominio, Long idExcluir) {
        String sql = "SELECT 1 FROM Vehiculo WHERE dominio=? AND eliminado=0 AND id<>?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dominio);
            ps.setLong(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando dominio duplicado: " + e.getMessage(), e);
        }
    }

    public List<Vehiculo> findAll() {
        String sql = """
            SELECT v.id, v.dominio, v.marca, v.modelo, v.anio, v.nro_chasis, v.eliminado,
                   s.id as s_id, s.aseguradora, s.nro_poliza, s.vencimiento, s.eliminado as s_eliminado,
                   tc.id as tc_id, tc.codigo, tc.nombre, tc.eliminado as tc_eliminado
            FROM Vehiculo v
            JOIN SeguroVehicular s ON s.id = v.seguro_id
            JOIN TipoCobertura tc ON tc.id = s.tipo_cobertura
            WHERE v.eliminado=0
            ORDER BY v.marca, v.modelo;
        """;

        List<Vehiculo> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando Vehículos: " + e.getMessage(), e);
        }
    }

    private Vehiculo map(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getLong("id"));
        v.setDominio(rs.getString("dominio"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        Object anioObj = rs.getObject("anio");
        v.setAnio(anioObj != null ? rs.getInt("anio") : null);
        v.setNroChasis(rs.getString("nro_chasis"));
        v.setEliminado(rs.getBoolean("eliminado"));

        TipoCobertura tc = new TipoCobertura();
        tc.setId(rs.getLong("tc_id"));
        tc.setCodigo(rs.getString("codigo"));
        tc.setNombre(rs.getString("nombre"));
        tc.setEliminado(rs.getBoolean("tc_eliminado"));
        // (no seteamos descripcion ni orden porque no existen en el modelo Java/SQL usado)

        SeguroVehicular s = new SeguroVehicular();
        s.setId(rs.getLong("s_id"));
        s.setAseguradora(rs.getString("aseguradora"));
        s.setNroPoliza(rs.getString("nro_poliza"));
        Date vto = rs.getDate("vencimiento");
        s.setVencimiento(vto != null ? vto.toLocalDate() : null);
        s.setEliminado(rs.getBoolean("s_eliminado"));
        s.setTipoCobertura(tc);

        v.setSeguro(s);
        return v;
    }

}