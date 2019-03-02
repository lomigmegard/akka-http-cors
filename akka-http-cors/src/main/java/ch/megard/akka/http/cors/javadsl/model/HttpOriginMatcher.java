package ch.megard.akka.http.cors.javadsl.model;

import akka.http.impl.util.Util;
import akka.http.javadsl.model.headers.HttpOrigin;
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher$;

public abstract class HttpOriginMatcher {

    public abstract boolean matches(HttpOrigin origin);

    public static HttpOriginMatcher ALL = ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher.$times$.MODULE$;

    public static HttpOriginMatcher create(HttpOrigin... origins) {
        return HttpOriginMatcher$.MODULE$.apply(Util.<HttpOrigin, akka.http.scaladsl.model.headers.HttpOrigin>convertArray(origins));
    }

    public static HttpOriginMatcher strict(HttpOrigin... origins) {
        return HttpOriginMatcher$.MODULE$.strict(Util.<HttpOrigin, akka.http.scaladsl.model.headers.HttpOrigin>convertArray(origins));
    }

}
