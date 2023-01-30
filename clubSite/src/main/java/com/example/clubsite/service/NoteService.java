package com.example.clubsite.service;

import com.example.clubsite.dto.NoteDTO;
import com.example.clubsite.entity.ClubMember;
import com.example.clubsite.entity.Note;

import java.util.List;

public interface NoteService {

    Long register(NoteDTO noteDTO);

    NoteDTO get(Long num);

    void modify(NoteDTO noteDTO);

    void remove(Long num);

    List<NoteDTO> getAllWithWriter(String writerEmail);

    default Note dtoToEntity(NoteDTO noteDTO){
        Note note=Note.builder()
                .title(noteDTO.getTitle())
                .content(noteDTO.getContent())
                .num(noteDTO.getNum())
                .writer(ClubMember.builder().email(noteDTO.getWriterEmail()).build())
                .build();
        return note;
    }

    default NoteDTO entityToDTO(Note note){
        NoteDTO noteDTO=NoteDTO.builder()
                .content(note.getContent())
                .num(note.getNum())
                .title(note.getTitle())
                .writerEmail(note.getWriter().getEmail())
                .regDate(note.getRegDate())
                .modDate(note.getModDate())
                .build();
        return noteDTO;

    }




}
