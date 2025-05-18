package itawi.chessgame.dto;

import lombok.Data;

@Data
public class PieceDTO {
    private String id;
    private String type;
    private String color;
    private String position;

    private String imageUrl; // Path to the piece image

    public PieceDTO(String id, String type, String color, String position) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.position = position;
        this.imageUrl = "/" + color.charAt(0) + "-" + type.toLowerCase() + ".png";
    }
}
