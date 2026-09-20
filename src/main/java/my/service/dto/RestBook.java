package my.service.dto;

import lombok.Data;

@Data 
public class RestBook {
    // ユーザID
    private String userId;
    // 連番
    private Integer seqNo;
    // タイトル
    private String title;
    // 著者
    private String author;
    // 値段
    private Integer price;
    // 出版社
    private String publisher;
    // 出版日yyyy-MM-dd
    private String published;
    // 購入日yyyy-MM-dd
    private String buyDate;
    // 読了日yyyy-MM-dd
    private String completeDate;
    // ジャンル
    private RestGenre genre;
    // 感想
    private String memo;
    // 評価
    private Integer rate;
    // 画像URL
    private String imgUrl;
    // 情報ページURL
    private String infoUrl;
}
