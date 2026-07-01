package com.utn.tpi.subasta.exception;

import java.net.URI;

public final class ProblemTypes {

    public static final URI BASE = URI.create("https://api.subastas.utn.edu.ar/problems");

    public static final URI BUSINESS = URI.create(BASE + "/business");
    public static final URI VALIDATION = URI.create(BASE + "/validation");
    public static final URI AUTHENTICATION = URI.create(BASE + "/authentication");
    public static final URI AUTHORIZATION = URI.create(BASE + "/authorization");
    public static final URI CONFLICT = URI.create(BASE + "/conflict");
    public static final URI INTERNAL = URI.create(BASE + "/internal");

    private ProblemTypes() {
    }
}
