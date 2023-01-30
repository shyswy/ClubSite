package com.example.clubsite.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJws;
import lombok.extern.log4j.Log4j2;

import java.time.ZonedDateTime;
import java.util.Date;

@Log4j2
public class JWTUtil {

    private String secretKey="shyswy12345678";

    private long expire=6*24*30; // 1달간 유효
    //JWT 문자열이 노출되면 누구나 모든 내용 확인 가능하기에, 유효기간 설정

    public String generateToken(String content) throws Exception{ //JWT 토큰 생성하기.
        return Jwts.builder()
                .setIssuedAt(new Date()) //시작점
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(expire).toInstant())) //만료 설정
                //.setExpiration(Date.from(ZonedDateTime.now().plusSeconds(1).toInstant()))
                .claim("sub",content) //name, value의 claim 쌍 여기에 이메일 등 저장할 정보를 "sub"라는 이름으로 추가
                .signWith(SignatureAlgorithm.HS256,secretKey.getBytes("UTF-8")) //알고리즘, 비밀키 설정
                .compact();
    }

    public String validateAndExtract(String tokenStr)throws Exception{ //인코딩된 문자열에서 원하는 값 추출
        //"sub"의 이름으로 들어갔던 Content의 값을 추출한다.
        String contentValue=null;

        try{ //DefaultJws를 구하는 과정에서, 유효기간이 만료되었다면 Exception을 트리거한다.
            DefaultJws defaultJws =(DefaultJws) Jwts.parser() //입력으로 들어온 인코딩된 String 해독하기.
                    .setSigningKey(secretKey.getBytes("UTF-8")) //해독에 필요한 비밀 키 넣기
                    .parseClaimsJws(tokenStr);  //입력으로 들어온 인코딩된 String 넣기
            log.info(defaultJws);
            log.info(defaultJws.getBody().getClass());
            DefaultClaims claims=(DefaultClaims) defaultJws.getBody(); //name,value의 claim쌍 추출
            log.info("---------------------------------");
            contentValue=claims.getSubject(); //content 값 추출
        }catch (Exception e){
            e.printStackTrace();;
            log.error(e.getMessage());
            contentValue=null; //초기화
        }
        return contentValue;
    }



}
