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
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

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
        var urls = UrlRepository.findAll();
        var urlChecks = UrlCheckRepository.getLastCheck();
        var page = new UrlsPage(urls, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("allUrls.jte", Collections.singletonMap("page", page));
    };

    public static Handler show = ctx -> {
        var urlId = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(urlId).orElseThrow(() -> new NotFoundResponse("Url not found"));
        var urlChecks = UrlCheckRepository.findByUrlId(urlId);
        var page = new UrlPage(url, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("show.jte", Collections.singletonMap("page", page));
    };

    public static Handler check = ctx -> {
        long urlId = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(urlId).get().getName();
        HttpResponse<String> response;
        try {
            response = Unirest.get(url).asString();
            var body = response.getBody();
            Document doc = Jsoup.parse(body);
            var statusCode = response.getStatus();
            var title = doc.title();
            Element h1Temp = doc.selectFirst("h1");
            String h1 = h1Temp == null ? null : h1Temp.text();
            Element descriptionTemp = doc.selectFirst("meta[name=description]");
            String description = descriptionTemp == null ? null : descriptionTemp.attr("content");

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description);
            urlCheck.setUrlId(urlId);
            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", "alert-success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Connect to " + url + " failed");
            ctx.sessionAttribute("flashType", "alert-danger");
            ctx.redirect(NamedRoutes.urlPath(urlId));
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flashType", "alert-danger");
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));
    };
}
