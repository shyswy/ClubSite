package com.example.clubsite.controller;


import com.example.clubsite.security.dto.ClubAuthMemberDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Log4j2
@RequestMapping("/sample/")
public class SampleController {

    @GetMapping("/all") //로그인 x 유저 접근 가능
    public void exAll(){
        log.info("exAll..........");
    }

    @GetMapping("/member") //로그인 사용자만 접근 가능
    public void exMember(@AuthenticationPrincipal ClubAuthMemberDTO clubAuthMemberDTO){
        log.info("exMember..........");
        log.info("---------------------------");
        log.info(clubAuthMemberDTO);

    }

    @GetMapping("/admin") // 관리자 권한이 있는 유저만 접근 가능.
    public void exAdmin(){
        log.info("exAdmin..........");
    }

//    @GetMapping("/member")
//    public void exMember(@AuthenticationPrincipal ClubAuthMemberDTO clubAuthMember){
//
//        log.info("exMember..........");
//
//        log.info("-------------------------------");
//        log.info(clubAuthMember);
//
//    }

}
