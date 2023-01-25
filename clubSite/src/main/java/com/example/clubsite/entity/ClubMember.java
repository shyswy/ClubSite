package com.example.clubsite.entity;

import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ClubMember extends BaseEntity{


    @Id
    private String email;

    private String password;

    private String name;

    private boolean fromSocial;// 소셜 계정 유무

    @ElementCollection(fetch = FetchType.LAZY)
    private Set<ClubMemberRole> roleSet; //한명의 멤버에게 여러 권한을 부여 가능하게 Set으로 처리( 중복은 제거하게)
    //( 특수 케이스, 보통은 1명당 1개의 Role)

    public void addMemberRole(ClubMemberRole clubMemberRole){
        roleSet.add(clubMemberRole);
    } //권한 추가 메소드


}
