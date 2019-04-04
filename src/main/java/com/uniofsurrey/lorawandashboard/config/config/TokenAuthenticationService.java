package com.uniofsurrey.lorawandashboard.config.config;

import com.uniofsurrey.lorawandashboard.config.entities.User;
import com.uniofsurrey.lorawandashboard.config.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static java.util.Collections.emptyList;

class TokenAuthenticationService {
    static final int EXPIRATIONTIME = 365*24*60*60*1000;
    static final String SECRET = "comgroup04bookshop";
    static private final Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);

    static void addAuthentication(HttpServletResponse res, User user) {
        String JWT = Jwts.builder()
                .setSubject(user.getUsername())
                .setId(user.getId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        Cookie cookie = new Cookie("jwt", JWT);
        cookie.setPath("/");
        cookie.setMaxAge(EXPIRATIONTIME);
        res.addCookie(cookie);
    }

    static Authentication getAuthentication(UserRepository userRepository, HttpServletRequest request) {
        String token = null;
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> jwtCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("jwt")).findFirst();
            if (jwtCookie.isPresent()) token = jwtCookie.get().getValue();
        }
        try {
            if (token != null) {
                // parse the token.
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET)
                        .parseClaimsJws(token)
                        .getBody();
                String username = claims.getSubject();
                String idStr = claims.getId();

                if (username == null || idStr == null) return null;
                Long id = Long.valueOf(claims.getId());

                User user = userRepository.findByUsername(username);
                if (user != null && user.getId().equals(id)) {
                    return new UsernamePasswordAuthenticationToken(user, null, emptyList());
                }
                return null;
            }
        } catch (SignatureException e) {
            logger.warn("Signature Exception", e);
            return null;
        } catch (Exception e) {
            logger.warn("Expired Jwt Exception", e);
            return null;
        }
        return null;
    }
}