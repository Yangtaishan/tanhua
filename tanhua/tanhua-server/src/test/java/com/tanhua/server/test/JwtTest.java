package com.tanhua.server.test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    /**
     * 生成秘钥
     */
    @Test
    public void createSecret(){
        System.out.println(DigestUtils.md5Hex("tanhua"));
//        62a0344c8c6a4c715d04bc895a1c94d2
    }
    @Test
    public void testJwt(){
        String secret="62a0344c8c6a4c715d04bc895a1c94d2";

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("mobile","123456");
        claims.put("id","2");

        //生成token
        String jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS256, secret).compact();
        System.out.println(jwt);

        //通过token解析数据
       Map<String,Object> body = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getBody();
        System.out.println(body);
    }
}
