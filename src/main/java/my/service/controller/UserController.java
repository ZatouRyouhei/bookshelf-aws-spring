package my.service.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.service.domain.exception.BookExistsException;
import my.service.domain.exception.UserNotFoundException;
import my.service.domain.model.MUser;
import my.service.domain.service.MUserService;
import my.service.dto.RestUser;
import my.service.security.JwtService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final MUserService service;

    private final ModelMapper modelMapper;

    private final PasswordEncoder encoder;

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RestUser request) {
        MUser mUser = service.login(request.getId());
        // idが存在しなかったとき
        if (mUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ユーザが見つかりませんでした。");
        }

        // パスワードが間違えていた時
        if (request.getPassword() == null || !encoder.matches(request.getPassword(), mUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("認証に失敗しました。");
        }

        // JWT生成
        String token = jwtService.generateToken(mUser);

        // レスポンス用ユーザ生成
        RestUser responseUser = new RestUser();
        responseUser.setId(mUser.getId());
        responseUser.setName(mUser.getName());
        responseUser.setMailAddress(mUser.getMailAddress());
        responseUser.setRoleName(mUser.getRoleName());
        responseUser.setToken(token);

        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }

    @PostMapping("/regist")
    public ResponseEntity<String> regist(@RequestBody RestUser request) {
        log.info(request.toString());
        try {
            MUser user = modelMapper.map(request, MUser.class);
            service.regist(user);
            return ResponseEntity.status(HttpStatus.OK).body("ユーザを登録しました。");
        } catch (Exception e) {
            log.error("ユーザ登録エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ユーザ登録を失敗しました。");
        }
    }

    @PostMapping("/getList")
    public ResponseEntity<List<RestUser>> getList() {
        List<MUser> userList = service.getList();
        List<RestUser> restUserList = userList.stream().map(user -> {
            return modelMapper.map(user, RestUser.class);
        }).toList();
        return ResponseEntity.ok(restUserList);
    }

    @PostMapping("/delete/{userId}")
    public ResponseEntity<String> delete(@PathVariable("userId") String userId) {
        try {
            service.delete(userId);
            return ResponseEntity.status(HttpStatus.OK).body("ユーザ情報を削除しました。");
        } catch (UserNotFoundException e) {
            log.error("ユーザが存在しない。", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (BookExistsException e) {
            log.error("本情報が登録されているため削除不可。", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("ユーザ情報削除エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ユーザ情報削除を失敗しました。");
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody RestUser request) {
        if (request.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("パスワードを指定してください。");
        }
        try {
            String encodedPassword = encoder.encode(request.getPassword());
            service.changePassword(request.getId(), encodedPassword);
            return ResponseEntity.status(HttpStatus.OK).body("パスワードを変更しました。");
        } catch (Exception e) {
            log.error("パスワード変更エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("パスワード変更が失敗しました。");
        }
    }

    @PostMapping("/reset/{userId}")
    public ResponseEntity<String> resetPassword(@PathVariable("userId") String userId) {
        try {
            service.resetPassword(userId);
            return ResponseEntity.status(HttpStatus.OK).body("パスワードを初期化しました。");
        } catch (UserNotFoundException e) {
            log.error("ユーザが存在しない。", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("パスワード初期化エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("パスワード初期化が失敗しました。");
        }
    }
}
