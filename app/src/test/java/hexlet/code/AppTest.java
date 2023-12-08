package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import java.sql.Timestamp;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    private Javalin app;

    @BeforeEach
    public final void setApp() throws IOException, SQLException {
        app = App.getApp();
    }

    @Test
    public void testShowMainPage() {
        JavalinTest.test(app, ((server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains("<p class=\"lead\">Бесплатно проверяйте сайты на SEO пригодность</p>");
        }));
    }

    @Test
    public void testCreateUrl() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/urls", "url=https://google.com/12345");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains("<a href=\"/urls/1\">https://google.com</a>");

            var response2 = client.post("/urls", "url=https://google.com/12345");
            assertThat(response2.code()).isEqualTo(200);
            assertThat(response2.body().string())
                    .contains("<a class=\"navbar-brand\" href=\"/\">Анализатор страниц</a>");
        });
    }

    @Test
    public void testListUrls() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls");
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testShow() {
        JavalinTest.test(app, (server, client) -> {
            var date = new Date();
            var createdAt = new Timestamp(date.getTime());
            var url = new Url(1, "https://google.com", createdAt);
            UrlRepository.save(url);
            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testUrlNotFound() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/25");
            assertThat(response.code()).isEqualTo(404);
        });
    }
}
