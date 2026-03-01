package com.winga.security;

import java.security.Principal;

/**
 * Principal for STOMP WebSocket sessions. getName() returns the user email
 * so that @MessageMapping methods receive Principal and use principal.getName().
 */
public record StompPrincipal(String name) implements Principal {

    @Override
    public String getName() {
        return name;
    }
}
