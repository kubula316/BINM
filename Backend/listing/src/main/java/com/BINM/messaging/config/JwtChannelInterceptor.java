package com.BINM.messaging.config;

import com.BINM.user.service.AppUserDetailsService;
import com.BINM.user.service.CustomUserDetails;
import com.BINM.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final AppUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization header: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String email = jwtUtil.extractEmail(jwt);

                if (email != null) {
                    CustomUserDetails userDetails = (CustomUserDetails) this.userDetailsService.loadUserByUsername(email);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        
                        // Tworzymy nasz własny Principal, który przechowuje userId
                        WebSocketUserPrincipal principal = new WebSocketUserPrincipal(userDetails.getUserId());
                        
                        // Ustawiamy go jako użytkownika sesji WebSocket
                        accessor.setUser(principal);
                        log.info("Authenticated user with ID {} for WebSocket session", principal.getName());
                    }
                }
            }
        }
        return message;
    }
}
