package pl.zielona_baza.common.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StateDTO {
    private Integer id;
    private String name;

}
