package com.tanhua.server.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtils {

    @Value("${tanhua.secret}")
    private String secret;

    /**
     * 生成JWT
     * @param phone
     * @param userId
     * @return
     */
    public String createJwt(String phone,Long userId){
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("mobile",phone);
        claims.put("id",userId);
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder().setClaims(claims).setIssuedAt(now).signWith(SignatureAlgorithm.HS256, secret);
        return builder.compact();
    }
}
