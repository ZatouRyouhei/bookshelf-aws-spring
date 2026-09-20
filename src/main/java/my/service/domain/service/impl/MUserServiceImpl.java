package my.service.domain.service.impl;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.service.domain.exception.BookExistsException;
import my.service.domain.exception.UserNotFoundException;
import my.service.domain.model.MUser;
import my.service.domain.model.TBook;
import my.service.domain.service.MUserService;
import my.service.dto.RestBookSearchCondition;
import my.service.mail.MailService;
import my.service.repository.MUserMapper;
import my.service.repository.TBookMapper;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class MUserServiceImpl implements MUserService {

    private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 6;

    private final MUserMapper userMapper;

    private final TBookMapper bookMapper;

    private final PasswordEncoder encoder;

    private final MailService mailService;

    @Value("${app.base-url}")
    private String appBaseURL;

    @Value("${app.queue-url}")
    private String queueURL;

    @Override
    public void regist(MUser user) {
        // ユーザが存在するか確認する。
        MUser targetUser = userMapper.findOne(user.getId());
        String newPassword = "";
        if (targetUser == null) {
            // ユーザが存在しない場合はパスワードを自動生成する。
            newPassword = generateRandomPassword();
            // 生成したパスワードを暗号化してセットする。
            user.setPassword(encoder.encode(newPassword));
        } else {
            // ユーザが存在する場合は登録されているパスワードをそのままセットする。（暗号化済み）
            user.setPassword(targetUser.getPassword());
        }
        int count = userMapper.insertOne(user);
        log.info("登録件数={}件", count);

        // ユーザが存在しない場合は生成したパスワードをメール通知する。
        if (targetUser == null) {
            String mailTo = user.getMailAddress();
            String title = "【ワタシノホンダナ】ユーザ登録";
            String body = String.format("ユーザを登録しました。\n URL : %s \n ID : %s \n パスワード : %s", appBaseURL, user.getId(), newPassword);
            mailService.sendMail(mailTo, title, body);
        }
    }

    @Override
    public MUser login(String id) {
        MUser user = userMapper.findOne(id);
        return user;
    }

    @Override
    public List<MUser> getList() {
        List<MUser> userList = userMapper.findMany();
        return userList;
    }

    @Override
    public void delete(String id) {
         // ユーザが存在しているか確認
        MUser targetUser = userMapper.findOne(id);
        if (targetUser == null) {
            throw new UserNotFoundException("ユーザが見つかりませんでした。id=" + id);
        }
        // 本情報が登録されているユーザは削除不可
        RestBookSearchCondition condition = new RestBookSearchCondition();
        condition.setUserId(id);
        List<TBook> bookList = bookMapper.findMany(condition);
        if (bookList.size() > 0) {
            throw new BookExistsException("使用中のため削除できません。id=" + id);
        }
        int count = userMapper.deleteOne(id);
        log.info("削除件数={}件", count);
    }

    @Override
    public void changePassword(String id, String password) {
        int count = userMapper.updatePassword(id, password);
        log.info("更新件数={}件", count);
    }

    @Override
    public void resetPassword(String id) {
        // ユーザが存在しているか確認
        MUser targetUser = userMapper.findOne(id);
        if (targetUser == null) {
            throw new UserNotFoundException("ユーザが見つかりませんでした。id=" + id);
        }
        String newPassword = generateRandomPassword();
        String encodedPassword = encoder.encode(newPassword);
        int count = userMapper.updatePassword(id, encodedPassword);
        log.info("更新件数={}件", count);

        // 生成したパスワードをメールで通知する
        String mailTo = targetUser.getMailAddress();
        String title = "【ワタシノホンダナ】パスワード初期化";
        String body = String.format("パスワードを初期化しました。\n URL : %s \n ID : %s \n パスワード : %s", appBaseURL, id, newPassword);
        mailService.sendMail(mailTo, title, body);
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARACTERS.charAt(random.nextInt(PASSWORD_CHARACTERS.length())));
        }
        return sb.toString();
    }
}
