package com.example.clubsite.security;


import com.example.clubsite.entity.ClubMember;
import com.example.clubsite.entity.ClubMemberRole;
import com.example.clubsite.repository.ClubMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.HashSet;
import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
public class ClubMemberTests {

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testEncode(){//Encode 방식이 BCrypt 방식이므로, 매번 암호화된 값은 달라진다.
        String password="1111";
        String enPW=passwordEncoder.encode(password); //암호화
        System.out.println("encoded password: "+enPW);
        boolean matches = passwordEncoder.matches(password, enPW); //matched함수를 통해 암호화된 암호와 원래 암호 매칭
        System.out.println("matchResult: "+matches); //올바르게 매칭 성공!
    }


    @Test
    public void insertDummies() {

        //1 - 80까지는 USER만 지정
        //81- 90까지는 USER,MANAGER
        //91- 100까지는 USER,MANAGER,ADMIN

        IntStream.rangeClosed(1,100).forEach(i -> {
            ClubMember clubMember = ClubMember.builder()
                    .email("user"+i+"@naver.com")
                    .name("사용자"+i)
                    .fromSocial(false)
                    .roleSet(new HashSet<ClubMemberRole>())
                    .password(  passwordEncoder.encode("1111") )
                    .build();

            //default role
            clubMember.addMemberRole(ClubMemberRole.USER);

            if(i > 80){
                clubMember.addMemberRole(ClubMemberRole.MANAGER);
            }

            if(i > 90){
                clubMember.addMemberRole(ClubMemberRole.ADMIN);
            }

            clubMemberRepository.save(clubMember);

        });

    }

    @Test
    public void testRead() {

        Optional<ClubMember> result = clubMemberRepository.findByEmail("user95@naver.com", false);

        ClubMember clubMember = result.get();

        System.out.println(clubMember);

    }






}


