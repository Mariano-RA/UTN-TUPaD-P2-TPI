package p2_tpi.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager implements AutoCloseable {

    private final Connection conn;
    private boolean completed = false;

    public TransactionManager() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
        this.conn.setAutoCommit(false);
    }

    public Connection getConnection() {
        return conn;
    }

    public void commit() throws SQLException {
        conn.commit();
        completed = true;
    }

    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void close() {
        try {
            if (!completed) {
                rollback();
            }
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ignored) {
        }
    }
}
