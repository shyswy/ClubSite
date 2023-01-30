package com.example.clubsite.security.filter;

import com.example.clubsite.security.util.JWTUtil;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Log4j2
public class ApiCheckFilter extends OncePerRequestFilter {
    private AntPathMatcher antPathMatcher;
    private  String pattern;

    private JWTUtil jwtUtil;
    public ApiCheckFilter(String pattern,JWTUtil jwtUtil){
        this.antPathMatcher=new AntPathMatcher();
        this.pattern=pattern;
        this.jwtUtil=jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Request URL: "+request.getRequestURI());
        log.info(antPathMatcher.match(pattern,request.getRequestURI()));
        if(antPathMatcher.match(pattern,request.getRequestURI())){
            log.info("APICheckFilter------------------------------------------------");
            boolean checkHeader=checkAuthHeader(request);
            log.info("checkHeader: "+checkHeader);
            if(checkHeader){ //헤더에 지정 값 "1234" 존재시
                filterChain.doFilter(request,response); //다음 필터로
                return;
            }
            else{
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                // json 리턴
                response.setContentType("application/json;charset=utf-8");
                JSONObject json = new JSONObject();
                String message = "FAIL CHECK API TOKEN";
                json.put("code", "403");
                json.put("message", message);

                PrintWriter out = response.getWriter();
                out.print(json);
                return;
            }


        }

        filterChain.doFilter(request,response); //다음 필터로 넘어가는 역할
    }

    private boolean checkAuthHeader(HttpServletRequest request){
        boolean checkResult =false;
        String authHeader=request.getHeader("Authorization"); //인증 헤더 불러오기.
        if(StringUtils.hasText(authHeader)&& authHeader.startsWith("Bearer ")){
            //Header은  "인증타입 ~~~~~~~~~~" 로 구성됨.
            // 여기선 Bearer이기에, 7째부터 유효한 String
            log.info("Authorization exist: "+authHeader);
            try{
                String email= jwtUtil.validateAndExtract(authHeader.substring(7));
                log.info("decode result: "+email);
                checkResult=email.length()>0; // email이 존재하면 true.
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return checkResult;
    }
}
