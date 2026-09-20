package my.service.controller;


import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// プロジェクト生成直後はEnableWebMvcがつくが削除する。
// @EnableWebMvcが付くとSpring Bootの自動設定（MVC全体）が丸ごと無効化され、
// StringHttpMessageConverterのデフォルト文字コードがUTF-8ではなく素のSpring既定値ISO-8859-1
// に戻ってしまいます。
// @EnableWebMvc
@RestController
public class PingController {
    @RequestMapping(path = "/ping", method = RequestMethod.GET)
    public Map<String, String> ping() {
        Map<String, String> pong = new HashMap<>();
        pong.put("pong", "Hello, World!");
        return pong;
    }
}
