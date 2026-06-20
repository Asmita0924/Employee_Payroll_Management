package com.payroll.dao;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class ConnectionFactory {

    private final DataSource dataSource;

    public ConnectionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Gets a connection from the HikariCP connection pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
