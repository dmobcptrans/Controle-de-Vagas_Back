package com.cptrans.petrocarga.infrastructure.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

public record UserAuthenticated(UUID id, UserDetails userDetails) {
    
}
