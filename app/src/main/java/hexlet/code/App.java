package hexlet.code;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.postgresql.Driver;
import java.sql.SQLException;
import java.util.stream.Collectors;

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

    private static InputStream getFile(String fileName) {
        var classLoader = App.class.getClassLoader();
        var inputStream = classLoader.getResourceAsStream(fileName);
        return inputStream;
    }

    private static String getContent(InputStream is) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public static Javalin getApp() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        String jbcUrl = "jdbc:h2:mem:piafson;DB_CLOSE_DELAY=-1;";
        if (System.getenv("JDBC_DATABASE_URL") != null) {
            hikariConfig.setDriverClassName(Driver.class.getCanonicalName());
            jbcUrl = System.getenv("JDBC_DATABASE_URL");
            hikariConfig.setUsername(System.getenv("JDBC_DATABASE_USERNAME"));
            hikariConfig.setPassword(System.getenv("JDBC_DATABASE_PASSWORD"));
        }
        hikariConfig.setJdbcUrl(jbcUrl);
        var dataSource = new HikariDataSource(hikariConfig);
        var sql = getContent(getFile("schema.sql"));
        try (var connection = dataSource.getConnection();
             var stmt = connection.createStatement()) {
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
        app.post(NamedRoutes.urlCheckPath("{id}"), UrlController.check);

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
