package com.example.clubsite.service;

import com.example.clubsite.dto.NoteDTO;
import com.example.clubsite.entity.Note;
import com.example.clubsite.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService{
    private final NoteRepository noteRepository;

    @Override
    public Long register(NoteDTO noteDTO) {
        Note note=dtoToEntity(noteDTO);
        noteRepository.save(note);

        return note.getNum();
    }

    @Override
    public NoteDTO get(Long num) {
        Optional<Note> result =noteRepository.getWithWriter(num);
        if(result.isPresent()){
            return entityToDTO(result.get());
        }

        return null;
    }

    @Override
    public void modify(NoteDTO noteDTO) {
        Optional<Note> result = noteRepository.findById(noteDTO.getNum());

        if(result.isPresent()){
            Note note=result.get();
            note.changeContent(noteDTO.getContent());
            note.changeTitle(noteDTO.getTitle());
            noteRepository.save(note);

        }

    }

    @Override
    public void remove(Long num) {
        noteRepository.deleteById(num);

    }

    @Override
    public List<NoteDTO> getAllWithWriter(String writerEmail) {
        List<Note> noteList = noteRepository.getList(writerEmail);
        return noteList.stream().map(n->entityToDTO(n)).collect(Collectors.toList());

    }


}
