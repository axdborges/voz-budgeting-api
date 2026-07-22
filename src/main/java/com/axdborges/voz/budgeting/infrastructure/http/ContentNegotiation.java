package com.axdborges.voz.budgeting.infrastructure.http;

import org.springframework.http.MediaType;

// Compartilhado entre controllers que negociam manualmente áudio vs. JSON (em vez de duas
// @RequestMapping com "produces" diferentes no mesmo path, que resolvem "Accept: */*" de forma
// ambígua). Só considera "quer áudio" quando o Accept tiver um tipo concreto audio/* — nunca o
// wildcard */* (padrão do curl/testes) — evitando servir áudio bruto por engano.
final class ContentNegotiation {

    private ContentNegotiation() {
    }

    static boolean wantsRawAudio(String accept) {
        return MediaType.parseMediaTypes(accept).stream()
                .anyMatch(mediaType -> !mediaType.isWildcardType() && "audio".equalsIgnoreCase(mediaType.getType()));
    }
}
