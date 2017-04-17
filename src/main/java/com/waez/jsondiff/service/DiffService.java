package com.waez.jsondiff.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.waez.jsondiff.model.DiffObject;
import com.waez.jsondiff.request.JSONBinaryRequest;

import dto.DiffResponseDTO;

/**
 * Service class that perform all the activities related to how to save the
 * data, keep the association between the diffObject and the files, how to
 * manage them, and how to perform the difference in the data.
 * 
 * @author Damian
 */
@Service
public class DiffService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiffService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Cache<Long, DiffObject> cache;

    /**
     * Default constructor that creates a cache with a expiration policy, in
     * order to remove the records and the associated files to it. <br/>
     * Also, its responsible to create the hook to perform the file cleaning
     * when the JVM is destroyed.
     */
    public DiffService() {
        CacheBuilder<Long, DiffObject> builder = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
                .removalListener(notification -> {
                    if (notification.getCause() == RemovalCause.EXPIRED) {
                        try {
                            deletePartFiles(notification.getValue());
                        } catch (IOException ioe) {
                            LOGGER.error("Error while trying to delete files for id: '{}'",
                                    notification.getValue().getId(), ioe);
                        }
                    }
                });

        this.cache = builder.build();

        // Works just when gracefully shutdown is requested from Windows or
        // Linux.
        Runtime.getRuntime().addShutdownHook(cleanUpTask());
    }

    /**
     * Method that checks if the provided diffObject parts have the same
     * content, different length, or if there is any difference to calculate,
     * returning them.
     * 
     * @param diffObject
     *            a Object containing the files associated to the left and right
     *            sides to perform the difference.
     * @return a Response with all the lines, offsets and length of the
     *         differences found.
     * @throws IOException
     *             if there is any problem trying to open the files associated
     *             to the diffObject.
     */
    public DiffResponseDTO makeDiff(final DiffObject diffObject) throws IOException {
        File leftPartFile = diffObject.getLeftPart().get().toFile();
        File rightPartFile = diffObject.getRightPart().get().toFile();

        boolean isTwoEqual = FileUtils.contentEquals(leftPartFile, rightPartFile);

        DiffResponseDTO response = new DiffResponseDTO();

        if (isTwoEqual) {
            response.setMessage("Data is exactly the same");
        } else if (leftPartFile.length() != rightPartFile.length()) {
            response.setMessage("Data is NOT the same size");
        } else {
            processDiff(leftPartFile, rightPartFile, response);
        }

        return response;
    }

    private void processDiff(final File leftPartFile, final File rightPartFile, final DiffResponseDTO response)
            throws IOException {
        // Given we don't know the size of the file in the file system, is more
        // prudent to read line by line to avoid OOM or to blow up the stack.
        LineIterator leftPartIt = FileUtils.lineIterator(leftPartFile, "UTF-8");
        LineIterator rightPartIt = FileUtils.lineIterator(rightPartFile, "UTF-8");

        try {
            int line = 0;

            while (leftPartIt.hasNext() && rightPartIt.hasNext()) {
                char[] leftCharArray = leftPartIt.nextLine().toCharArray();
                char[] rightCharArray = rightPartIt.nextLine().toCharArray();

                int lineSize = leftCharArray.length;

                int offsetStart = 0;
                int offsetSize = 1;
                boolean offsetSet = false;

                for (int j = 0; j < lineSize; j++) {
                    if (leftCharArray[j] != rightCharArray[j]) {
                        if (!offsetSet) {
                            offsetStart = j;
                            offsetSet = true;
                        } else {
                            offsetSize++;
                        }

                        if (j == lineSize - 1) {
                            response.addLine(line, offsetStart, offsetSize);

                            offsetSet = false;
                            offsetSize = 1;
                            offsetStart = 0;
                        }
                    } else {
                        if (offsetSet) {
                            response.addLine(line, offsetStart, offsetSize);

                            offsetSet = false;
                            offsetSize = 1;
                            offsetStart = 0;
                        }
                    }
                }

                line++;
            }
        } finally {
            LineIterator.closeQuietly(leftPartIt);
            LineIterator.closeQuietly(rightPartIt);
        }

        response.setMessage("Diff successfully calculated");
    }

    /**
     * Return the diffObject with the given id.
     * 
     * @param id
     *            an id to identify a diffObject.
     * @return returns the diffObject associated with id, or null
     */
    public DiffObject getDiffById(final Long id) {
        return cache.getIfPresent(id);
    }

    /**
     * Adds a new diffObject in memory that holds the files associated to the
     * left and right side to perform the differences, but not the content by
     * themselves (In order to don't keep them in memory)).
     * 
     * @param diffObject
     *            a Object containing the files associated to the left and right
     *            sides to perform the difference.
     * @param data
     *            the data to be stored in the given part or side.
     * @param leftPart
     *            if this data belongs to the left or right side.
     * @return a URI pointing where the file with the data was created.
     * @throws IOException
     *             if there is any problem trying to access to the files
     *             associated to the diffObject.
     */
    public URI addNewDiff(final DiffObject diffObject, final String data, final boolean leftPart) throws IOException {
        JSONBinaryRequest bean = MAPPER.readValue(data, JSONBinaryRequest.class);

        URI uri = null;

        if (leftPart) {
            diffObject.setLeftPart(writeData(bean.getBinary(), diffObject.getLeftPart()));
            uri = diffObject.getLeftPart().get().toUri();
        } else {
            diffObject.setRightPart(writeData(bean.getBinary(), diffObject.getRightPart()));
            uri = diffObject.getRightPart().get().toUri();
        }

        cache.put(diffObject.getId(), diffObject);

        return uri;
    }

    private Optional<Path> writeData(final byte[] data, final Optional<Path> part) throws IOException {
        if (part.isPresent()) {
            return Optional.of(Files.write(part.get(), data, StandardOpenOption.CREATE));
        }

        return Optional.of(Files.write(Paths.get(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString()), data,
                StandardOpenOption.CREATE));
    }

    private void deletePartFiles(final DiffObject diffObject) throws IOException {
        Optional<Path> leftPart = diffObject.getLeftPart();
        Optional<Path> rightPart = diffObject.getRightPart();

        if (leftPart.isPresent()) {
            FileUtils.forceDelete(leftPart.get().toFile());

            LOGGER.info("Auto Removing for expiration, left file: '{}' for id: '{}' ",
                    leftPart.get().toFile().getAbsolutePath(), diffObject.getId());
        }

        if (rightPart.isPresent()) {
            FileUtils.forceDelete(rightPart.get().toFile());

            LOGGER.info("Auto Removing for expiration, right file: '{}' for id: '{}' ",
                    rightPart.get().toFile().getAbsolutePath(), diffObject.getId());
        }
    }

    /**
     * Method that runs when the JVM is asked to shutdown, and perform files
     * cleaning.
     * 
     * @return a Thread thats is in charge of perform the task.
     */
    private Thread cleanUpTask() {
        return new Thread() {
            @Override
            public void run() {
                LOGGER.info("Shutdown application, deleting temp files...");

                Stream.of(cache.asMap().values()).forEach(x -> x.stream().forEach(y -> {
                    try {
                        deletePartFiles(y);
                    } catch (IOException e) {
                        LOGGER.error("Failed to remove files for '{}'", y);
                    }
                }));

                cache.invalidateAll();
            }
        };
    }
}
