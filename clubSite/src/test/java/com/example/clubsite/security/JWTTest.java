package com.example.clubsite.security;

import com.example.clubsite.security.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JWTTest {

    private JWTUtil jwtUtil;

    @BeforeEach
    public void beforeTest(){
        jwtUtil=new JWTUtil();
    }

    @Test
    public void testEncode() throws Exception{
        String email="user10@naver.com";
        String jwtString=jwtUtil.generateToken(email);
        System.out.println("encode: "+jwtString);
        String ans=jwtUtil.validateAndExtract(jwtString);
        System.out.println("decode: "+ans);
    }

    @Test
    public void testValidateExpire() throws Exception{
        String email="user10@naver.com";
        String jwtString=jwtUtil.generateToken(email);
        Thread.sleep(5000);// 시간의 흐름
        System.out.println(jwtUtil.validateAndExtract(jwtString));
    }
}
