package dto;

/**
 * DTO Class that represent the difference in lines </br>
 * This class is used to be marshalled into a JSON at controller level inside
 * DiffResponseDTO.
 * 
 * @author Damian
 *
 */
public class LineDTO {
    private final Integer line;
    private final Integer offset;
    private final Integer length;

    public LineDTO() {
        this.line = 0;
        this.offset = 0;
        this.length = 0;
    }

    public LineDTO(Integer line, Integer offset, Integer length) {
        this.line = line;
        this.offset = offset;
        this.length = length;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLength() {
        return length;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line [line=").append(line).append(", offset=").append(offset).append(", length=").append(length)
                .append("]");
        return builder.toString();
    }
}
