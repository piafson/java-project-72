package hexlet.code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
        HikariConfig hikariConfig = new HikariConfig();
        String jbcUrl = "jdbc:h2:mem:piafson";

        if (System.getenv("JDBC_DATABASE_URL") != null) {
            jbcUrl = System.getenv("JDBC_DATABASE_URL");
            hikariConfig.setUsername(System.getenv("JDBC_DATABASE_USERNAME"));
            hikariConfig.setPassword(System.getenv("JDBC_DATABASE_PASSWORD"));
        }
        hikariConfig.setJdbcUrl(jbcUrl);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile("schema.sql");

        log.info(sql);
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        var app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
            JavalinJte.init(createTemplateEngine());
        });

        app.get(NamedRoutes.rootPath(), RootController.showMainPage);
        app.post(NamedRoutes.urlsPath(), UrlController.createUrl);
        app.get(NamedRoutes.urlsPath(), UrlController.listUrls);
        app.get(NamedRoutes.urlPath("{id}"),  UrlController.show);

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
        app.start();
    }
}
