package com.example.clubsite.repository;

import com.example.clubsite.entity.ClubMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember,String> {


    @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraph.EntityGraphType.LOAD)
    //기존에는 LAZY 방식을 통해 그때그떄 1개씩만 가져오지만, 이 경우는 @EntityGraph 어노테이션을 통해
    //한번의 fetchJoin으로 모든 roleSet을 가져오게 설정한다.
    // ( @EntityGraph x시 LAZY 방식으로 인해 roleset 못가져온다.
    @Query("select m from ClubMember m where m.fromSocial = :social and m.email =:email")
    Optional<ClubMember> findByEmail(String email, boolean social);
}
