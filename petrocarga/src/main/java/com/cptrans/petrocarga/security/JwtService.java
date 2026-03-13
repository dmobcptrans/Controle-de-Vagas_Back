package com.cptrans.petrocarga.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.models.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationMs;

    /**
     * Retorna a chave secreta a ser usada para assinar os tokens JWT.
     * A chave é gerada a partir do segredo armazenado no arquivo application.yml.
     * @return a chave secreta para assinar os tokens JWT
    */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Gera um token JWT com base nos dados do usuario.
     * O token JWT gerado contem as seguintes informações:
     *  - id: o ID do usuario
     *  - nome: o nome do usuario
     *  - email: o email do usuario
     *  - permissao: a permissão do usuario
     * O token JWT também tem uma data de expiração que é definida pela propriedade jwt.expiration.
     * @param usuario o usuario a ter o token JWT gerado
     * @return o token JWT gerado com base nos dados do usuario
     */
    public String gerarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", usuario.getId().toString());
        claims.put("nome", usuario.getNome());
        claims.put("email", usuario.getEmail());
        claims.put("permissao", usuario.getPermissao().name());

        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims) 
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Valida se um token JWT é válido.
     * O token JWT é considerado válido se a data de expiração do token for posterior a data atual.
     * @param token o token JWT a ser validado
     * @return true caso o token seja válido, false caso contrário
     */
    public boolean validarToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /** 
     * Retorna as claims do token JWT informado.
     * As claims incluem informações como o email do usuário, o nome do usuário e a permissão do usuário.
     * @param token o token JWT a ter as claims recuperadas
     * @return as claims do token JWT informado
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Retorna o email do usuário contido no token JWT informado.
     * 
     * @param token o token JWT a ter o email do usuário recuperado
     * @return o email do usuário contido no token JWT informado
    */
   public String getEmailDoToken(String token) {
       return getClaims(token).getSubject();
    }
    
    /**
     * Retorna o id do usuário contido no token JWT informado.
     * 
     * @param token o token JWT a ter o id do usuário recuperado
     * @return o id do usuário contido no token JWT informado
    */
    public UUID getIdDoToken(String token) {
        return UUID.fromString(getClaims(token).get("id").toString());
    }
}