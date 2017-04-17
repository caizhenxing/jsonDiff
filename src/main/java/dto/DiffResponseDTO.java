package dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO Class that represent all the differences found when the difference was
 * performed. </br>
 * This class is used to be marshalled into a JSON at controller level.
 * 
 * @author Damian
 *
 */
public class DiffResponseDTO {
    private final List<LineDTO> lines;
    private String message;

    public DiffResponseDTO() {
        this.lines = new ArrayList<>();
    }

    public DiffResponseDTO(String message) {
        this.message = message;
        this.lines = new ArrayList<>();
    }

    public void addLine(int line, int offset, int length) {
        lines.add(new LineDTO(line, offset, length));
    }

    public List<LineDTO> getLines() {
        return lines;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DiffResponseDTO [lines=").append(lines).append(", message=").append(message).append("]");
        return builder.toString();
    }
}
