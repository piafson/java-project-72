package hexlet.code;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

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
            Response response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains("<p class=\"lead\">Бесплатно проверяйте сайты на SEO пригодность</p>");
        }));
    }
}
