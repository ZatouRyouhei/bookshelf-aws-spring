package my.service.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import my.service.domain.model.TBook;
import my.service.dto.RestBookSearchCondition;

@Mapper
public interface TBookMapper {
    public List<TBook> findMany(RestBookSearchCondition condition);
    
    public int insertOne(TBook book);

    public Integer getMaxSeqNo(String userId);

    public int updateOne(TBook book);

    public int deleteOne(String userId, Integer seqNo);

    public int insertMany(List<TBook> bookList);
}
