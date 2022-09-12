package com.springboot.jwtaunthetication.configuration.jwt;


import com.springboot.jwtaunthetication.dto.AuthenticationResponse;
import com.springboot.jwtaunthetication.entity.User;
import com.springboot.jwtaunthetication.exception.RequestException;
import com.springboot.jwtaunthetication.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    public static final Logger LOGGER = LogManager.getLogger(JwtTokenUtil.class);

    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    public static final String JWT_SCOPE = "scopes";


    @Autowired
    private UserRepository userRepository;

    @Value(value = "${jwt.access.token.validity}")
    public long jwtTokenValidity;

    @Value(value = "${jwt.refresh.token.validity}")
    public long jwtRefreshTokenValidity;

    private static final String JWT_TOKEN_TYPE = "Bearer ";

    @Value("${jwt.secret}")
    private String secret;

    /**
     * get user name from jwt token
     *
     * @param token - jwt token
     * @return - username
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * get issued date from token
     *
     * @param token - jwt token
     * @return - issued date
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * get expiration date from jwt token
     *
     * @param token - jwt token
     * @return - expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * get claims from jwt token
     *
     * @param token          - jwt token
     * @param claimsResolver - claim resolveer
     * @return - jwt claims
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * get all claims of jwt token using jwt secret
     *
     * @param token - jwt token
     * @return - jwt claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * check JWT token has expired or not
     *
     * @param token - JWT token
     * @return - return true if jwt token has expired else false
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean ignoreTokenExpiration() {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    /**
     * generate new access token and refresh token when user login
     *
     * @param userDetails - authenticated user object
     * @return - jwt tokens and scopes
     */
    public AuthenticationResponse generateToken(UserDetails userDetails) {
        String access = createAccessJwtToken(userDetails, 1000);
        String refresh = createRefreshToken(userDetails, 2000);
        List<String> roles = userDetails.getAuthorities().stream().map(Object::toString).collect(Collectors.toList());

        Optional<User> user = userRepository.findByEmail(userDetails.getUsername());
        if (user.isPresent()) {
            AuthenticationResponse response = new AuthenticationResponse();
            response.setId(user.get().getId());
            response.setFirstName(user.get().getUsername());
            response.setJwttoken(access);
            response.setRefreshToken(refresh);
            response.setRoles(roles);
            response.setTokenType(JWT_TOKEN_TYPE);
            response.setExpire(getExpirationDateFromToken(access));
            return response;
        } else {
            return null;
        }
    }

    /**
     * generate access token from refresh token
     *
     * @param userDetails
     * @return
     */
    public AuthenticationResponse generateTokenFromRefreshToken(UserDetails userDetails, String refreshToken) {
        LOGGER.debug("get new access token from refresh token");
        String access = createAccessJwtToken(userDetails, 1000);

        List<String> roles = userDetails.getAuthorities().stream().map(s -> s.toString()).collect(Collectors.toList());
        Optional<User> user = userRepository.findByEmail(userDetails.getUsername());
        if (user.isPresent()) {
            AuthenticationResponse response = new AuthenticationResponse();
            response.setId(user.get().getId());
            response.setFirstName(user.get().getUsername());

            response.setJwttoken(access);
            response.setRefreshToken(refreshToken);
            response.setRoles(roles);
            response.setTokenType(JWT_TOKEN_TYPE);
            response.setExpire(getExpirationDateFromToken(access));
            return response;
        }
        return null;
    }

    /**
     * generate access token
     *
     * @param userDetails - user details object that contains user information
     * @param miliSecond
     * @return - returns access token
     */
    public String createRefreshToken(UserDetails userDetails, int miliSecond) {
        LOGGER.info("Generate refresh token for authenticated user: {} ", userDetails.getUsername());

        if (userDetails.getUsername().isEmpty()) {
            throw new IllegalArgumentException("cannot.create.token.without.username");
        }
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        // set refresh token in scope claim

        claims.put(JWT_SCOPE, Arrays.asList(REFRESH_TOKEN));

        return Jwts.builder().setClaims(claims).setIssuer(userDetails.getUsername()).setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshTokenValidity * miliSecond))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    /**
     * generate refresh token
     *
     * @param userDetails - user details object that contains user information
     * @param miliSecond
     * @return - returns refresh token
     */
    public String createAccessJwtToken(UserDetails userDetails, int miliSecond) {
        LOGGER.info("Generate access token for authenticated user: {} ", userDetails.getUsername());
        if (userDetails.getUsername().isEmpty())
            throw new IllegalArgumentException("cannot.create.token.without.username");
        if (userDetails.getAuthorities() == null || userDetails.getAuthorities().isEmpty())
            throw new IllegalArgumentException("user.has.no.privileges");

        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        claims.put(JWT_SCOPE,
                userDetails.getAuthorities().stream().map(s -> s.toString()).collect(Collectors.toList()));
        return Jwts.builder().setClaims(claims).setIssuer(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtTokenValidity * miliSecond))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    /**
     * check token can be refreshed or not
     *
     * @param token - JWT token
     * @return - return true if token can be refresh else false
     */
    public Boolean canTokenBeRefreshed(String token) {
        return (!isTokenExpired(token) || ignoreTokenExpiration());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * validate refresh token that refresh has expired or not and check that jwt
     * token contains refresh scope or not
     *
     * @param token - JWT token
     * @return - true if refresh token is valid
     */
    public boolean validateRefreshToken(String token) {
        LOGGER.info("validating refresh token");

        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            LOGGER.info("get scopes from JWT refresh token");
            List<String> scopes = claims.get(JWT_SCOPE, List.class);
            boolean isTokenBeRefreshed = canTokenBeRefreshed(token);
            if (!isTokenBeRefreshed) {
                throw new RequestException("refresh.token.is.expired");
            }
            LOGGER.info("JWT refresh token socpe: {} ", scopes);
            if (scopes == null || scopes.isEmpty() || scopes.stream().noneMatch(REFRESH_TOKEN::equals)) {
                throw new RequestException("invalid.refresh.token");
            }

        } catch (ExpiredJwtException exception) {
            LOGGER.error(exception.getMessage());
            throw new RequestException("refresh.token.is.expired");
        }

        return true;

    }

}
