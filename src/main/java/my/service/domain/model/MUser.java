package my.service.domain.model;

import lombok.Data;

@Data 
public class MUser {
    private String id;
    private String password;
    private String name;
    private String roleName;
    private String mailAddress;
}
