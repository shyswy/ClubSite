package com.example.clubsite.security.service;

import com.example.clubsite.entity.ClubMember;
import com.example.clubsite.repository.ClubMemberRepository;
import com.example.clubsite.security.dto.ClubAuthMemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;


@Log4j2
@RequiredArgsConstructor
@Service   //Service 어노테이션을 통해 자동으로 빈으로 처리되게하고, 스프링은 이를 USerDetialsService로 인식한다!
public class ClubUserDetailsService  implements UserDetailsService {
    //인증 매니저 -> Provider(호환 타입 관련) -> UserDetailsService ( 이곳) 에서 loadUserByName 메소드로 인증, 인가 정보를 담은 DTO 반환
    //이 DTO는 User를 상속한 뒤, User의 생성자를 사용할 수 있게 구현
    private final ClubMemberRepository clubMemberRepository;
    //username은 각 유저를 식별하는 id. 여기선 Email이 ID 즉 유저네임이다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //UserDetialsService의 loadUserByUsername을 오버라이딩해서 아이디를 통한 인증을 처리한다.
        log.info("ClubUserDetailsService loadUserByUsername " + username);
        Optional<ClubMember> result = clubMemberRepository.findByEmail(username, false);
        //멤버 정보를 리포지토리에서 찾는다.
        if(result.isEmpty()){
            throw new UsernameNotFoundException("Check User Email or from Social ");
        }
        ClubMember clubMember = result.get();
        log.info("-----------------------------");
        log.info(clubMember);
        ClubAuthMemberDTO clubAuthMemberDTO = new ClubAuthMemberDTO( //인증, 허가에 필요한 정보를 담은 DTO 생성
                clubMember.getEmail(),
                clubMember.getPassword(),
                clubMember.isFromSocial(),
                clubMember.getRoleSet().stream() // role을 저장한 Set의 모든 요소들을 ROLE_USER와 같은 SimpleGrantedAuthority타입으로
                        .map(role -> new SimpleGrantedAuthority("ROLE_"+role.name()))
                        .collect(Collectors.toSet())
        );
        clubAuthMemberDTO.setName(clubMember.getName());
        clubAuthMemberDTO.setFromSocial(clubMember.isFromSocial());
        return clubAuthMemberDTO;////인증, 허가에 필요한 정보를 담은 DTO 반환
    }
}
