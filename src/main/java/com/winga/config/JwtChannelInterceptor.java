package com.winga.config;

import com.winga.security.JwtService;
import com.winga.security.StompPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Intercepts STOMP CONNECT: validates JWT from header and sets Principal.
 * Client must send token in CONNECT frame header, e.g. "Authorization: Bearer &lt;token&gt;" or "token: &lt;token&gt;".
 * If token is missing or invalid, CONNECT is rejected (message dropped).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String token = getToken(accessor);
        if (token == null || token.isBlank()) {
            log.debug("WebSocket CONNECT rejected: no token");
            return null;
        }

        try {
            String email = jwtService.extractUsername(token);
            if (email == null || email.isBlank()) {
                return null;
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(token, userDetails)) {
                log.debug("WebSocket CONNECT rejected: invalid token");
                return null;
            }
            accessor.setUser(new StompPrincipal(userDetails.getUsername()));
            return message;
        } catch (Exception e) {
            log.debug("WebSocket CONNECT rejected: {}", e.getMessage());
            return null;
        }
    }

    private static String getToken(StompHeaderAccessor accessor) {
        String auth = accessor.getFirstNativeHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        String t = accessor.getFirstNativeHeader("token");
        return t != null ? t.trim() : null;
    }
}
