package my.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RestGenre {
    private Integer id;
    private String name;

    public RestGenre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
