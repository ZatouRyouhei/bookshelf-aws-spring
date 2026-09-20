package my.service.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MGenre {
    private Integer id;
    private String name;
    public MGenre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
