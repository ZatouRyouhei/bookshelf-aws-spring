package my.service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.service.domain.model.MGenre;
import my.service.domain.service.MGenreService;

@RestController
@RequestMapping("/genre")
@RequiredArgsConstructor 
@Slf4j 
public class GenreController {
    private final MGenreService service;

    @GetMapping("/getList")
    public ResponseEntity<List<MGenre>> getList() {
        List<MGenre> genreList = service.getGenres();
        return ResponseEntity.ok(genreList);
    }
}
