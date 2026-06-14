package edu.franklin.cecas.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public final class MySqlTestcontainers {

    private MySqlTestcontainers() {
        // Private constructor to prevent instantiation
    }

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    public static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("cecas_test")
            .withUsername("testuser")
            .withPassword("testpass");
}
