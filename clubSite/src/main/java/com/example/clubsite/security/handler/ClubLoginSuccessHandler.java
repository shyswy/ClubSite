package com.example.clubsite.security.handler;


import com.example.clubsite.security.dto.ClubAuthMemberDTO;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class ClubLoginSuccessHandler implements AuthenticationSuccessHandler {

    private RedirectStrategy redirectStrategy=new DefaultRedirectStrategy();
    private PasswordEncoder passwordEncoder;
    public ClubLoginSuccessHandler(PasswordEncoder passwordEncoder){
        this.passwordEncoder=passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("on Authentication is Success");
        ClubAuthMemberDTO clubAuthMemberDTO=(ClubAuthMemberDTO) authentication.getPrincipal();//로그인 사용자의 authentication 객체 사용
        //authentication 객체의 getPrincipal() 메서드를 실행하게 되면, UserDetails를 구현한 사용자 객체를 Return 한다. -> 로그인한 사용자의 UserDetial 객체 수신

        boolean fromSocial=clubAuthMemberDTO.isFromSocial();
        log.info("User is Social Login User or Not?: "+fromSocial);
        boolean isDefaultPassword=passwordEncoder.matches("1111",clubAuthMemberDTO.getPassword()); //해당 유저의 비밀번호가 디폴트 비밀번호인 1111인가?

        if(fromSocial&&isDefaultPassword){//소셜 로그인 사용자 && 비밀번호가 디폴트 비밀번호인 1111이면
            redirectStrategy.sendRedirect(request,response,"/sample/modify");// 해당 url에서 유저가 이름 변경, 비밀번호 변경 가능.

        }


    }
}
