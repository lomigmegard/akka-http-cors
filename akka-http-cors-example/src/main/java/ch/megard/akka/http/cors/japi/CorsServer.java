package ch.megard.akka.http.cors.japi;


import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.HttpOrigin;
import akka.http.javadsl.model.headers.HttpOriginRange;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.util.NoSuchElementException;

import static ch.megard.akka.http.cors.japi.CorsDirectives.*;

/**
 * Example of a Java HTTP server using the CORS directive.
 */
public class CorsServer extends AllDirectives {

    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create();
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final CorsServer app = new CorsServer();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        http.bindAndHandle(routeFlow, ConnectHttp.toHost("127.0.0.1", 9000), materializer);
    }

    private final CorsSettings settings = CorsSettings.defaultSettings()
            .withAllowedOrigins(HttpOriginRange.create(HttpOrigin.parse("http://example.com")));

    private Route createRoute() {

        return cors(settings, () -> route(
            path("ping", () -> complete("pong")),
            path("pong", () -> failWith(new NoSuchElementException("pong not found, try with ping")))
        ));
    }

}
