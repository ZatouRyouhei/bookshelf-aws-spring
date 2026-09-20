package my.service.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.service.domain.model.MGenre;
import my.service.domain.service.MGenreService;
import my.service.repository.MGenreMapper;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class MGenreServiceImpl implements MGenreService {
    
    private final MGenreMapper mapper;
    
    @Override
    public List<MGenre> getGenres() {
        List<MGenre> genreList = mapper.findMany();
        return genreList;
    }
}
