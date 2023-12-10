package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    private Javalin app;

    private static Path getFixturePath(String fileName) {
        return Paths.get("src", "test", "resources", "fixtures", fileName).toAbsolutePath().normalize();
    }

    private static String readFixture(String fileName) throws IOException {
        Path filePath = getFixturePath(fileName);
        return Files.readString(filePath).trim();
    }

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
            assertThat(UrlRepository.checkUrlExist("https://google.com")).isTrue();
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
            var url = new Url("https://google.com");
            UrlRepository.save(url);
            var newUrl = UrlRepository.find("https://google.com");
            var id = newUrl.get().getId();
            var response = client.get("/urls/" + id);
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testUrlNotFound() {
        JavalinTest.test(app, (server, client) -> {
            UrlRepository.destroy(25);
            var response = client.get("/urls/25");
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    public void testCheckUrl() throws IOException, SQLException {
        var mockServer = new MockWebServer();
        var mockUrl = mockServer.url("/").toString();
        var mockResponse = new MockResponse().setBody(readFixture("index.html"));
        mockServer.enqueue(mockResponse);

        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + mockUrl;
            var response = client.post("/urls", requestBody);
            assertThat(response.code()).isEqualTo(200);
        });

        var formattedName = String.format("%s://%s", mockServer.url("/").url().getProtocol(),
                mockServer.url("/").url().getAuthority());
        var addUrl = UrlRepository.find(formattedName).orElse(null);
        assertThat(addUrl).isNotNull();
        assertThat(addUrl.getName()).isEqualTo(formattedName);
    }
}
