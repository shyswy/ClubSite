package com.example.clubsite.controller;


import com.example.clubsite.dto.NoteDTO;
import com.example.clubsite.entity.Note;
import com.example.clubsite.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/notes/")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping(value = "")
    public ResponseEntity<Long> register(@RequestBody NoteDTO noteDTO){ //Post방식으로 새로운 노트를 등록할 수 있는 기능
        //@RequestBody: JSON 데이터를 받아서 NoteDTO로 변환해준다.
        log.info("-------------------register-----------------");
        log.info(noteDTO);

        Long num=noteService.register(noteDTO);
        return new ResponseEntity<>(num, HttpStatus.OK);

    }


    @GetMapping(value = "/{num}",produces = MediaType.APPLICATION_JSON_VALUE) //Note 번호를 PathVariable로 받으면
    public ResponseEntity<NoteDTO> read(@PathVariable("num") Long num){
        log.info("-------------read-----------------------");
        log.info(num);
        return new ResponseEntity<>(noteService.get(num),HttpStatus.OK); //해당하는 번호의 NoteDTO 객체를 리턴해준다.
    }


    @GetMapping(value="/all",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NoteDTO>> getList(String email){ //@RequestParam이 생략된 것
        log.info("---------------getList---------------------");
        log.info(email);
        return  new ResponseEntity<>(noteService.getAllWithWriter(email),HttpStatus.OK);
    }

    @DeleteMapping(value = "/{num}",produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> remove(@PathVariable("num") Long num){
        log.info("----------------remove---------------------");
        log.info(num);
        noteService.remove(num);
        return new ResponseEntity<>("removed",HttpStatus.OK);

    }

    @PutMapping(value = "/{num}",produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> modify(@RequestBody NoteDTO noteDTO){//ajax요청으로 온 JSON 데이터의 Body에 있는 NoteDTO에 대한 정보를 추출
        noteService.modify(noteDTO);
        log.info("mod");
        return new ResponseEntity<>("modified",HttpStatus.OK);

    }





}
