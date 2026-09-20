package my.service.domain.model;

import java.time.LocalDate;

import lombok.Data;

@Data 
public class TBook {
    private String userId;
    private Integer seqNo;
    private String author;
    private String title;
    private LocalDate buyDate;
    private LocalDate completeDate;
    private Integer genreId;
    private String imgUrl;
    private String infoUrl;
    private String memo;
    private Integer price;
    private LocalDate published;
    private String publisher;
    private Integer rate;
    private MGenre genre;
}
