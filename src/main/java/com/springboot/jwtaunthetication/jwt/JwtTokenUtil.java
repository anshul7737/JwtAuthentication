package com.springboot.jwtaunthetication.jwt;

import com.springboot.jwtaunthetication.userapi.User;
import io.jsonwebtoken.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.logging.Logger;

@Component
public class JwtTokenUtil {
    private static final org.slf4j.Logger LOGGER=LoggerFactory.getLogger(JwtTokenUtil.class);

    private static  final long EXPIRE_DURATION = 24 * 60 * 60 * 1000;
     @Value("${app.jwt.secret}")
    private String secretKey;

     public String generateAccessToken(User user){
         return Jwts.builder()
                 .setSubject(user.getId() + ","+ user.getEmail())
                 .setIssuer("Anshul")
                 .setIssuedAt(new Date())
                 .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_DURATION))
                 .signWith(SignatureAlgorithm.HS512,secretKey)
                 .compact();
     }

     public boolean validateAccessToken(String token){
         try {
               Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
               return true;

         }catch (ExpiredJwtException e) {
             LOGGER.error("JWT expired", e);
         }catch (IllegalArgumentException e) {
             LOGGER.error("Token is null", e);
         }catch (MalformedJwtException e) {
             LOGGER.error("JWT is invalid", e);
         }catch (UnsupportedJwtException e) {
             LOGGER.error("JWT is not supported", e);
         }catch (SignatureException e){
             LOGGER.error("Signature validation failed",e);
             }
          return false;
     }
     public String getSubject(String token){
         return parseClaims(token).getSubject();
     }
     private Claims parseClaims(String token){
         return Jwts.parser()
                 .setSigningKey(secretKey)
                 .parseClaimsJws(token)
                 .getBody();

     }

}
