package com.example.clubsite.security.filter;

import com.example.clubsite.security.dto.ClubAuthMemberDTO;
import com.example.clubsite.security.handler.ApiLoginFailHandler;
import com.example.clubsite.security.util.JWTUtil;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Log4j2
public class ApiLoginFilter extends AbstractAuthenticationProcessingFilter {

    private JWTUtil jwtUtil;

    public ApiLoginFilter(String defaultFilterProcessesUrl,JWTUtil jwtUtil) {

        super(defaultFilterProcessesUrl);
        this.jwtUtil=jwtUtil; //생성자 주입을 통해 JWT를 주입받자.
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        log.info("--------------ApiLoginFilter-----------------------");
        String email=request.getParameter("email");
        String pw=request.getParameter("pw");


        log.info("email,pw,: "+email+" : "+pw);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, pw);

        return getAuthenticationManager().authenticate(authToken);

    }

    /*
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("login fail local Handler--------------------------");
        log.info(failed.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // json 으로 에러 리턴
        response.setContentType("application/json;charset=utf-8");
        JSONObject json = new JSONObject();
        String message = failed.getMessage();
        json.put("code", "401");
        json.put("message", message);

        PrintWriter out = response.getWriter();
        out.print(json);
        //super.unsuccessfulAuthentication(request, response, failed);
    }

     */

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        //authResult는 성공한 사용자의 인증 정보를 가지고 있는 Authentication 객체이다.
        log.info("ApiLoginFilter--------------------");
        log.info("successfulAuthentication: "+authResult);
        log.info(authResult.getPrincipal());

        String email= ( (ClubAuthMemberDTO) authResult.getPrincipal() ).getUsername();//인증정보에서 유저이름(이메일) 추출
        String token=null;
        try{
            token= jwtUtil.generateToken(email); //인증 성공 시, JWT 토큰을 발행해준다.
            response.setContentType("text/plain");
            response.getOutputStream().write(token.getBytes());
            log.info(token);

        }catch (Exception e){
            e.printStackTrace();

        }
        //super.successfulAuthentication(request, response, chain, authResult);
    }


}
