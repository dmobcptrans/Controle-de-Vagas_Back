package com.cptrans.petrocarga.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

public record UserAuthenticated(UUID id, String nome, Integer criptoVersion, Integer hashVersion, UserDetails userDetails) {
    
}