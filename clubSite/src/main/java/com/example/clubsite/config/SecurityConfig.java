package com.example.clubsite.config;

import com.example.clubsite.security.filter.ApiCheckFilter;
import com.example.clubsite.security.filter.ApiLoginFilter;
import com.example.clubsite.security.handler.ApiLoginFailHandler;
import com.example.clubsite.security.handler.ClubLoginSuccessHandler;
import com.example.clubsite.security.service.ClubUserDetailsService;
import com.example.clubsite.security.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@Log4j2
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true)//@PreAuthorisze(), @PreSecure() 사용하기 위한 설정
public class SecurityConfig { //2.7 버전 이후, websecurityconfigurationAdapter deprecated 됨


    @Autowired
    private ClubUserDetailsService userDetailsService;
    @Bean
    PasswordEncoder passwordEncoder() {//비밀 번호 암호화를 담당하는 ENcoder 정의
        return new BCryptPasswordEncoder(); //Bcrypt 방식 사용
    }

    /*
    @Bean
    public InMemoryUserDetailsManager userDetailsService() { //test 유저 생성하는 용도.
        UserDetails user = User.builder()   // user1, 1111 의 로그인 정보인 유저를 생성.
                .username("user1")
                .password(passwordEncoder().encode("1111"))  //저장할때는 암호화된 정보로 저장해줘야한다.
                .roles("USER") //USER 이라는 ROle 부여
                .build();

        log.info("userDetailsService............................");
        log.info(user);

        return new InMemoryUserDetailsManager(user);
    }

     */

    /*
//  http.formLogin(); //인증에 문제 발생 시, 로그인화면으로 이동.
    //별도의 디자인을 가진 로그인 페이지를 사용하기 위해서는 loginPage()나 loginProcessUrl(), FailureUrl() 등
    //필요한 설정을 지정할 수 있다. 보통 별도의 디자인 쓰기에 LoginPage()로 사용한다.

    //csrf 토큰을 생성하는 것을 방지한다.
    //csrf를 통한 공격을 방지하기 위함
    //csrf: A 사이트 관리자가 B사이트에서 csrf 포함된 게시물에 접근 시, A사이트 관리자에게 로그인 요청이 가고,
    //만약 A 사이트 관리자가 A 사이트에 로그인 되어있는 상태라면, A사이트의 "관리자 권한" 을 얻을 수 있게 된다.
    //-> 관리자 권한을 훔칠 수 있다!

    http.logout();// 로그아웃 처리.  ( 정말 로그아웃 하시겠습니까? 페이지)
        //만약 csrf 토큰을( disable 처리 x 시) 사용할 경우, 반드시 POST 방식으로 처리해야한다.
        //여기선 disable했기에  Get으로 처리 가능.


       // http.oauth2Login(); //기존 oauth
        http.oauth2Login(); //.successHandler(successHandler()); //성공시의 handler 추가

        http.rememberMe().tokenValiditySeconds(60*60*24*7).userDetailsService(userDetailsService);// 7일간 쿠키 유지
        //쿠키를 사용하는 인증 방식. 사용자가 브라우저를 닫아도, 로그인 정보가 쿠키로 남아있어 별도의 처리없이 로그인 가능.
        //단, 소셜 로그인은 remember-me 방식 불가능.
*/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

           http.authorizeHttpRequests((auth) -> {
              auth.antMatchers("/sample/all").permitAll();// 로그인하지 않은 익명 사용자에게도 허락한다.
            auth.antMatchers("/sample/member").hasRole("USER");
        });

        //AuthenticationManager설정
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        //반드시 필요
        http.authenticationManager(authenticationManager);

        http.formLogin(); //인증에 문제 발생 시, 로그인화면으로 이동.
        http.csrf().disable(); //csrf disable
        http.logout();//logout
        http.oauth2Login(); //login 이후
        http.rememberMe().tokenValiditySeconds(60*60*24*7).userDetailsService(userDetailsService);// 7일간 쿠키 유지
        http.addFilterBefore(apiCheckFilter(),UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(apiLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public ClubLoginSuccessHandler successHandler() {
        return new ClubLoginSuccessHandler(passwordEncoder());
    }

    @Bean
    JWTUtil jwtUtil(){
        return new JWTUtil();
    }
    @Bean
    public ApiCheckFilter apiCheckFilter(){
        return new ApiCheckFilter("/notes/**/*",jwtUtil());
    }


    public ApiLoginFilter apiLoginFilter(AuthenticationManager authenticationManager) throws Exception{

        ApiLoginFilter apiLoginFilter =  new ApiLoginFilter("/api/login",jwtUtil());
                                                                                //생성자 파라미터에 JWTUtil 주입.
        apiLoginFilter.setAuthenticationManager(authenticationManager);
        apiLoginFilter.setAuthenticationFailureHandler(new ApiLoginFailHandler()); //생성한 Fail 핸들러를 전달.

        return apiLoginFilter;
    }







}

