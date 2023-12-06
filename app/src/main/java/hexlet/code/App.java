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

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.rendering.template.JavalinJte;
import gg.jte.resolve.ResourceCodeResolver;

@Slf4j
public class App {
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.valueOf(port);
    }

    private static String readResourceFile(String fileName) throws IOException {
        var path = Paths.get("src", "main", "resources", fileName);
        return Files.readString(path);
    }

    public static Javalin getApp() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        String jbcUrl = System.getenv("JDBC_DATABASE_URL");

        if (System.getenv("JDBC_DATABASE_URL") == null) {
            jbcUrl = "jdbc:h2:mem:piafson";
            hikariConfig.setUsername(System.getenv("JDBC_DATABASE_USERNAME"));
            hikariConfig.setPassword(System.getenv("JDBC_DATABASE_PASSWORD"));
        }
        hikariConfig.setJdbcUrl(jbcUrl);

        var sql = readResourceFile("schema.sql");
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        var app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
            JavalinJte.init(createTemplateEngine());
        });

        app.get("/", ctx -> ctx.render("index.jte"));

        return app;
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Javalin app = getApp();
        app.start(getPort());
    }
}
