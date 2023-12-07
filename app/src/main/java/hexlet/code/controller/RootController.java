package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import io.javalin.http.Handler;

import java.util.Collections;

public class RootController {

    public static Handler showMainPage = ctx -> {
        BasePage page = new BasePage();
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.sessionAttribute("flashType"));
        ctx.render("index.jte", Collections.singletonMap("page", page));
    };
}
