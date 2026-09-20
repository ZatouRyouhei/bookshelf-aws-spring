package my.service.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import my.service.domain.model.MUser;

@Mapper 
public interface MUserMapper {
    public int insertOne(MUser user);

    public MUser findOne(String id);

    public List<MUser> findMany();

    public int deleteOne(String id);

    public int updatePassword(String id, String password);
}
