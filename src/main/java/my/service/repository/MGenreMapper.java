package my.service.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import my.service.domain.model.MGenre;

@Mapper
public interface MGenreMapper {
    public List<MGenre> findMany();

    public MGenre findOne(int id);
}
