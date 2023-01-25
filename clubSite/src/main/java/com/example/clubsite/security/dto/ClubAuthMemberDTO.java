package com.example.clubsite.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Log4j2
@Getter
@Setter
@ToString
public class ClubAuthMemberDTO extends User { //DTO로써의 역할을 수행함과 동시에 스프링 시큐리티의 인증/인가 작업에 사용가능.

    private String email;

    private String name;

    private boolean fromSocial;


    public ClubAuthMemberDTO(String username,   //상위의 User 클래스의 생성자를 호출
                             String password,
                             boolean formSocial,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.email=username;         //상위의 User의 생성자를 호출하여 정보를 구성하고, email, social 미디어 가입 여부를 저장한다.
        this.fromSocial=formSocial;
    }
}
