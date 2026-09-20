package my.service.domain.service;

import java.util.List;

import my.service.domain.model.MUser;

public interface MUserService {
    public void regist(MUser user);

    public MUser login(String id);

    public List<MUser> getList();

    public void delete(String id);

    public void changePassword(String id, String password);

    public void resetPassword(String id);
}
