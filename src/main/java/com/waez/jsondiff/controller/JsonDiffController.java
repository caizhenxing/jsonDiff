package com.waez.jsondiff.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.waez.jsondiff.model.DiffObject;
import com.waez.jsondiff.service.DiffService;

import dto.DiffResponseDTO;

/**
 * Main controller that expose the different resources to perform data
 * comparison. </br>
 * <ul>
 * <li>"/v1/diff/{id}/left", in order to add data to "left side".</li>
 * <li>"/v1/diff/{id}/right", in order to add data to "right side".</li>
 * <li>"/v1/diff/{id}/", return the differences between "right and left side",
 * or a message if some of the parts are missing.</li>
 * </ul>
 * 
 * @author Damian
 */
@RestController
@RequestMapping(value = "/v1/diff")
public class JsonDiffController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDiffController.class);

    @Autowired
    private DiffService diffService;

    /**
     * Enpoint that adds information for comparison at the "left side" with the
     * given data and id.
     * 
     * @param data
     *            a JSON base64 encoded binary.
     * @param id
     *            the provided id to associate with this data.
     * @return a plain text response saying if the operation was successful or
     *         not.
     */
    @RequestMapping(value = "/{id}/left", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> leftDiff(@RequestBody String data, @PathVariable Long id) {
        return saveData(data, id, true);
    }

    /**
     * Enpoint that adds information for comparison at the "right side" with the
     * given data and id.
     * 
     * @param data
     *            a JSON base64 encoded binary.
     * @param id
     *            the provided id to associate with this data.
     * @return a plain text response saying if the operation was successful or
     *         not.
     */
    @RequestMapping(value = "/{id}/right", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> rightDiff(@RequestBody String data, @PathVariable Long id) {
        return saveData(data, id, false);
    }

    /**
     * Return all the differences that may exist between what was loaded at the
     * "left and right side", or a message saying that contents are the same, or
     * another message saying that the size of data is different so not
     * comparable. </br>
     * This endpoint answers with a JSON response, listing all the lines where
     * differences happened, with the offset and the length of difference
     * 
     * @param id
     *            the id to use to perform the difference.
     * @return a JSON response, listing all the lines where differences
     *         happened, with the offset and the length of difference
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DiffResponseDTO> getDiff(@PathVariable Long id) {
        LOGGER.info("Get diff incoming request with id: '{}'", id);

        DiffObject diffObject = diffService.getDiffById(id);

        if (diffObject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DiffResponseDTO("Nothign to compare with id: " + id + ", it may be expired already"));
        }

        Optional<Path> leftPart = diffObject.getLeftPart();
        Optional<Path> rightPart = diffObject.getRightPart();

        if (leftPart.isPresent() && rightPart.isPresent()) {
            try {
                DiffResponseDTO diffResponse = diffService.makeDiff(diffObject);

                return ResponseEntity.ok().body(diffResponse);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new DiffResponseDTO("Could not read file content for id: " + id));
            }
        }

        return ResponseEntity.badRequest()
                .body(new DiffResponseDTO("One of the parts to compare with are missing with id: " + id));
    }

    private ResponseEntity<String> saveData(String data, Long id, boolean leftPart) {
        LOGGER.info("Incoming request with id: '{}' for {} side", id, leftPart ? "left" : "right");

        Optional<DiffObject> diffOptionalObject = Optional
                .of(Optional.ofNullable(diffService.getDiffById(id)).orElse(new DiffObject(id)));

        try {
            URI uri = diffService.addNewDiff(diffOptionalObject.get(), data, leftPart);

            return ResponseEntity.created(uri).body("Part of comparision added with id: " + id + " at: " + uri);
        } catch (IOException ioe) {
            LOGGER.error("Error while trying to write part of compare, ex: ", ioe);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not write to file content for id: " + id);
        }
    }
}
