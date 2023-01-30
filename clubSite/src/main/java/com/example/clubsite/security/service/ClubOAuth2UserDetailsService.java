package com.example.clubsite.security.service;

import com.example.clubsite.entity.ClubMember;
import com.example.clubsite.entity.ClubMemberRole;
import com.example.clubsite.repository.ClubMemberRepository;
import com.example.clubsite.security.dto.ClubAuthMemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ClubOAuth2UserDetailsService extends DefaultOAuth2UserService {
//OAuth2UserService 인터페이스는 UserDetailsService 인터페이스의 OAuth 버전이라고 생각하면 된다.
    // 해당 인터페이스는 다양한 구현 클래스를 가지고 있고, 간편성을 위해 이중 하나인 Default~~~ 구현 클래스를 상속받는다.

    private final ClubMemberRepository clubMemberRepository;

    private final PasswordEncoder passwordEncoder;


    @Override  //UserDetailsService의 loadUserByName 메소드 처럼, 유저 리퀘스트를 통해 적절한 인증 정보를 리턴하는 메소드
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("----------------------------");
        log.info("UserRequest: "+userRequest);

        String clientName = userRequest.getClientRegistration().getClientName();

        log.info("clientName: "+clientName); //소셜 로그인한 사용자의 클라이언트 이름
        log.info(userRequest.getAdditionalParameters());//그외의 파라미터 출력

        OAuth2User oAuth2User= super.loadUser(userRequest);
        log.info("********************************");
        oAuth2User.getAttributes().forEach((k,v)->{ //email, sub, picture등 여러가지 정보들을 모두 출력한다.
            //OAuth 에서 설정한 API 범위와 application properties에서 설정한 값에 따라 나오는 정보의 범위가 달라진다.
            log.info(k+": "+v);
        });

        String email=null;
        if(clientName.equals("Google")){ //구글을 통한 소셜 로그인의 경우
            email=oAuth2User.getAttribute("email"); //이메일 정보를 추출
            log.info("google email: "+email);
        }
        ClubMember member=saveSocialMember(email); //해당 이메일을 가진 사용자가 있다면 찾아서 리턴,
        //없다면 로그인 정보를 기반으로 ClubMember 객체를 생성한 뒤 저장하고, 해당 객체 리턴하는 메소드

        ClubAuthMemberDTO clubAuthMemberDTO=new ClubAuthMemberDTO(
                member.getEmail(),
                member.getPassword(),
                true,   //fromSocial
                member.getRoleSet().stream().map(
                                role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toList()),
                oAuth2User.getAttributes()
        );
        clubAuthMemberDTO.setName(member.getName());


        return clubAuthMemberDTO;
    }


    private ClubMember saveSocialMember(String email){

        //기존에 동일한 이메일로 가입한 회원이 있는 경우에는 그대로 조회만
        Optional<ClubMember> result = clubMemberRepository.findByEmail(email, true);

        if(result.isPresent()){
            return result.get();
        }

        //없다면 회원 추가 패스워드는 1111 이름은 그냥 이메일 주소로
        ClubMember clubMember = ClubMember.builder().email(email)
                .name(email)
                .password( passwordEncoder.encode("1111") )
                .fromSocial(true)
                .build();

        clubMember.addMemberRole(ClubMemberRole.USER);


        clubMemberRepository.save(clubMember);

        return clubMember;
    }

}
