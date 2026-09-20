package my.service.dto;

import lombok.Data;

@Data
public class RestUser {
    private String id;
    private String password;
    private String name;
    private String roleName;
    private String mailAddress;
    private String token;
}
