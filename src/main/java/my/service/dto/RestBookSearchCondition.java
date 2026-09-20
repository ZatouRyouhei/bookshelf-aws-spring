package my.service.dto;

import lombok.Data;

@Data 
public class RestBookSearchCondition {
    private String userId;
    private String title;
    private String author;
    private String completeDateFrom;
    private String completeDateTo;
    private Integer genre;
    private Integer rate;
    private boolean unComplete;
}
