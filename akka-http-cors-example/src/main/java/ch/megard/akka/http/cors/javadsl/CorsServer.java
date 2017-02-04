package ch.megard.akka.http.cors.javadsl;


import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.HttpOrigin;
import akka.http.javadsl.model.headers.HttpOriginRange;
import akka.http.javadsl.server.*;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;
import static ch.megard.akka.http.cors.javadsl.CorsDirectives.corsRejectionHandler;

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

    private Route createRoute() {

        // Your CORS settings
        final CorsSettings settings = CorsSettings.defaultSettings()
                .withAllowedOrigins(HttpOriginRange.create(HttpOrigin.parse("http://example.com")));

        // Your rejection handler
        final RejectionHandler rejectionHandler = corsRejectionHandler().withFallback(RejectionHandler.defaultHandler());

        // Your exception handler
        final ExceptionHandler exceptionHandler = ExceptionHandler.newBuilder()
                .match(NoSuchElementException.class, ex -> complete(StatusCodes.NOT_FOUND, ex.getMessage()))
                .build();

        // Combining the two handlers only for convenience
        final Function<Supplier<Route>, Route> handleErrors = inner -> Directives.allOf(
                s -> handleExceptions(exceptionHandler, s),
                s -> handleRejections(rejectionHandler, s),
                inner
        );

        // Note how rejections and exceptions are handled *before* the CORS directive (in the inner route).
        // This is required to have the correct CORS headers in the response even when an error occurs.
        return handleErrors.apply(() -> cors(settings, () -> handleErrors.apply(() -> route(
                path("ping", () ->
                        complete("pong")),
                path("pong", () ->
                        failWith(new NoSuchElementException("pong not found, try with ping")))
        ))));
    }

}
