package com.waez.jsondiff.model;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Class that represent the different sides needed t perform the difference
 * "left" and "right", and that associated an id to this relation. </br>
 * This class doesn't hold any kind of data at memory, but the files to point to
 * with them.
 * 
 * @author Damian
 *
 */
public class DiffObject {

    private Long id;
    private Optional<Path> leftPart = Optional.empty();
    private Optional<Path> rightPart = Optional.empty();

    public DiffObject(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Optional<Path> getLeftPart() {
        return leftPart;
    }

    public void setLeftPart(Optional<Path> leftPart) {
        this.leftPart = leftPart;
    }

    public Optional<Path> getRightPart() {
        return rightPart;
    }

    public void setRightPart(Optional<Path> rightPart) {
        this.rightPart = rightPart;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DiffObject [id=").append(id).append(", leftPart=").append(leftPart).append(", rightPart=")
                .append(rightPart).append("]");
        return builder.toString();
    }
}
