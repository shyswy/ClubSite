package com.example.clubsite.controller;


import com.example.clubsite.entity.ClubMember;
import com.example.clubsite.repository.ClubMemberRepository;
import com.example.clubsite.security.dto.ClubAuthMemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/sample/")
public class SampleController {

    private final ClubMemberRepository clubMemberRepository;

    @GetMapping("/all") //로그인 x 유저 접근 가능
    public void exAll(){
        log.info("exAll..........");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/member") //로그인 사용자만 접근 가능
    public void exMember(@AuthenticationPrincipal ClubAuthMemberDTO clubAuthMemberDTO){
        log.info("exMember..........");
        log.info("---------------------------");
        log.info(clubAuthMemberDTO);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin") // 관리자 권한이 있는 유저만 접근 가능.
    public void exAdmin(){
        log.info("exAdmin..........");
    }

    @PreAuthorize("#clubAuthMemberDTO!= null && #clubAuthMemberDTO.username eq \"dragon981126@gmail.com\"")
    @GetMapping("/meOnly")
    public String MeOnly(@AuthenticationPrincipal ClubAuthMemberDTO clubAuthMemberDTO){
        log.info("meOnly...........");
        log.info(clubAuthMemberDTO);
        return "/sample/admin";
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



    /*
    @GetMapping("/modify")
    public void update(@AuthenticationPrincipal ClubAuthMemberDTO clubAuthMemberDTO,Model model){
        model.addAttribute("ClubAuthMemberDTO",clubAuthMemberDTO);

    }

    @PostMapping("/modify")
    public String userUpdate(@AuthenticationPrincipal ClubAuthMemberDTO clubAuthMemberDTO,Model model){
        Optional<ClubMember> result = clubMemberRepository.findByEmail(clubAuthMemberDTO.getEmail(),
                clubAuthMemberDTO.isFromSocial());


        try {
            if (result.isPresent()) {
                ClubMember clubMember = result.get();
                clubMember.changeName(clubAuthMemberDTO.getName());
                clubMember.changePassword(clubAuthMemberDTO.getPassword());
                clubMemberRepository.save(clubMember);
            } else {
                ClubMember clubMember = ClubMember.builder()
                        .email(clubAuthMemberDTO.getEmail())
                        .fromSocial(clubAuthMemberDTO.isFromSocial())
                        .name(clubAuthMemberDTO.getName())
                        .password(clubAuthMemberDTO.getPassword())
                        .build();
                clubMemberRepository.save(clubMember);

            }
        } catch (Exception e) {
            log.info("modifying error: "+e);

            return "redirect:/modify";
        }

        return "redirect:/modify";


    }

     */






}
