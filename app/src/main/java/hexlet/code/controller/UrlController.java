package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;

public class UrlController {

    public static Handler createUrl = ctx -> {
        String receivedUrl = ctx.formParam("url");
        URL url;
        try {
            url = new URL(receivedUrl);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flashType", "alert-danger");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }
        String name = String.format("%s://%s", url.getProtocol(), url.getAuthority());

        if (UrlRepository.checkUrlExist(name)) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flashType", "alert-danger");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        Url newUrl = new Url(name);
        UrlRepository.save(newUrl);
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flashType", "alert-success");
        ctx.redirect(NamedRoutes.urlsPath());
    };

    public static Handler listUrls = ctx -> {
        var urls = UrlRepository.getEntities();
        var urlChecks = UrlCheckRepository.getLastCheck();
        var page = new UrlsPage(urls, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("allUrls.jte", Collections.singletonMap("page", page));
    };

    public static Handler show = ctx -> {
        var urlId = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(urlId).orElseThrow(() -> new NotFoundResponse("Url not found"));
        var urlChecks = UrlCheckRepository.getEntities(urlId);
        var page = new UrlPage(url, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("show.jte", Collections.singletonMap("page", page));
    };

    public static Handler check = ctx -> {
        var urlCheck = new UrlCheck();
        long urlId = ctx.pathParamAsClass("id", Long.class).get();
        var date = new Date();
        var createdAt = new Timestamp(date.getTime());
        urlCheck.setUrlId(urlId);
        urlCheck.setCreatedAt(createdAt);
        UrlCheckRepository.save(urlCheck);
        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flashType", "alert-success");
        ctx.redirect(NamedRoutes.urlPath(urlId));
    };
}
