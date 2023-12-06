package hexlet.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;

@Slf4j
public class App {
    public static Javalin getApp() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        String jbcUrl = System.getenv("JDBC_DATABASE_URL");
        if (System.getenv("JDBC_DATABASE_URL") == null) {
            jbcUrl = "jdbc:h2:mem:piafson";
            hikariConfig.setUsername(System.getenv("JDBC_DATABASE_USERNAME"));
            hikariConfig.setPassword(System.getenv("JDBC_DATABASE_PASSWORD"));
        }
        hikariConfig.setJdbcUrl(jbcUrl);

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        var dbPath =  Paths.get("src", "main", "resources", "schema.sql");
        var sql = Files.readString(dbPath);
        var app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        });
        try (var connection = dataSource.getConnection();
                var stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        app.get("/", ctx -> ctx.result("Hello World"));

        return app;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Javalin app = getApp();
        app.start(7070);
    }
}
