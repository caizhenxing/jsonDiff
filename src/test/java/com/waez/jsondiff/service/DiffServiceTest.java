package com.waez.jsondiff.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;

import com.google.common.cache.Cache;
import com.waez.jsondiff.model.DiffObject;

import dto.DiffResponseDTO;
import dto.LineDTO;

public class DiffServiceTest {

    @Mock
    private Cache<Long, DiffObject> cache;

    @InjectMocks
    private DiffService underTest;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testMakeDiffFullCompareWith2Differences() throws IOException {
        Path tempLeftPath = null;
        Path tempRightPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            byte[] leftContent = new String("aaaaaaa").getBytes();
            byte[] rightContent = new String("abaaaaa").getBytes();

            Files.write(tempLeftPath, leftContent, StandardOpenOption.CREATE);
            Files.write(tempRightPath, rightContent, StandardOpenOption.CREATE);

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));
            diffObject.setRightPart(Optional.of(tempRightPath));

            DiffResponseDTO diffResult = underTest.makeDiff(diffObject);

            assertTrue(diffResult != null);
            assertTrue("Diff successfully calculated".equals(diffResult.getMessage()));

            List<LineDTO> collect = diffResult.getLines().stream().filter(x -> x.getOffset() == 1 && x.getLength() == 1)
                    .collect(Collectors.toList());

            assertTrue(!ObjectUtils.isEmpty(collect));
        } catch (IOException e) {
            throw e;
        } finally {
            if (tempLeftPath != null) {
                Files.deleteIfExists(tempLeftPath);
            }

            if (tempRightPath != null) {
                Files.deleteIfExists(tempRightPath);
            }
        }
    }

    @Test
    public void testMakeDiffFullCompareWithDifferencesAtBeginning() throws IOException {
        Path tempLeftPath = null;
        Path tempRightPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            byte[] leftContent = new String("aaaaaaa").getBytes();
            byte[] rightContent = new String("baaaaaa").getBytes();

            Files.write(tempLeftPath, leftContent, StandardOpenOption.CREATE);
            Files.write(tempRightPath, rightContent, StandardOpenOption.CREATE);

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));
            diffObject.setRightPart(Optional.of(tempRightPath));

            DiffResponseDTO diffResult = underTest.makeDiff(diffObject);

            assertTrue(diffResult != null);
            assertTrue("Diff successfully calculated".equals(diffResult.getMessage()));

            List<LineDTO> collect = diffResult.getLines().stream().filter(x -> x.getOffset() == 0 && x.getLength() == 1)
                    .collect(Collectors.toList());

            assertTrue(!ObjectUtils.isEmpty(collect));
        } catch (IOException e) {
            throw e;
        } finally {
            if (tempLeftPath != null) {
                Files.deleteIfExists(tempLeftPath);
            }

            if (tempRightPath != null) {
                Files.deleteIfExists(tempRightPath);
            }
        }
    }

    @Test
    public void testMakeDiffFullCompareWithDifferencesAtBeginningTwiceMatch() throws IOException {
        Path tempLeftPath = null;
        Path tempRightPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            byte[] leftContent = new String("aaaaaaa").getBytes();
            byte[] rightContent = new String("bbaaaaa").getBytes();

            Files.write(tempLeftPath, leftContent, StandardOpenOption.CREATE);
            Files.write(tempRightPath, rightContent, StandardOpenOption.CREATE);

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));
            diffObject.setRightPart(Optional.of(tempRightPath));

            DiffResponseDTO diffResult = underTest.makeDiff(diffObject);

            assertTrue(diffResult != null);
            assertTrue("Diff successfully calculated".equals(diffResult.getMessage()));

            List<LineDTO> collect = diffResult.getLines().stream().filter(x -> x.getOffset() == 0 && x.getLength() == 2)
                    .collect(Collectors.toList());

            assertTrue(!ObjectUtils.isEmpty(collect));
        } catch (IOException e) {
            throw e;
        } finally {
            if (tempLeftPath != null) {
                Files.deleteIfExists(tempLeftPath);
            }

            if (tempRightPath != null) {
                Files.deleteIfExists(tempRightPath);
            }
        }
    }

    @Test
    public void testMakeDiffFullCompareWithDifferencesAtEnd() throws IOException {
        Path tempLeftPath = null;
        Path tempRightPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            byte[] leftContent = new String("aaaaaaa").getBytes();
            byte[] rightContent = new String("aaaaaab").getBytes();

            Files.write(tempLeftPath, leftContent, StandardOpenOption.CREATE);
            Files.write(tempRightPath, rightContent, StandardOpenOption.CREATE);

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));
            diffObject.setRightPart(Optional.of(tempRightPath));

            DiffResponseDTO diffResult = underTest.makeDiff(diffObject);

            assertTrue(diffResult != null);
            assertTrue("Diff successfully calculated".equals(diffResult.getMessage()));

            List<LineDTO> collect = diffResult.getLines().stream().filter(x -> x.getOffset() == 6 && x.getLength() == 1)
                    .collect(Collectors.toList());

            assertTrue(!ObjectUtils.isEmpty(collect));
        } catch (IOException e) {
            throw e;
        } finally {
            if (tempLeftPath != null) {
                Files.deleteIfExists(tempLeftPath);
            }

            if (tempRightPath != null) {
                Files.deleteIfExists(tempRightPath);
            }
        }
    }

    @Test
    public void testMakeSameFileContent() throws IOException {
        Path tempLeftPath = null;
        Path tempRightPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            byte[] leftContent = new String("aaaaaaa").getBytes();
            byte[] rightContent = new String("aaaaaaa").getBytes();

            Files.write(tempLeftPath, leftContent, StandardOpenOption.CREATE);
            Files.write(tempRightPath, rightContent, StandardOpenOption.CREATE);

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));
            diffObject.setRightPart(Optional.of(tempRightPath));

            DiffResponseDTO diffResult = underTest.makeDiff(diffObject);

            assertTrue(diffResult != null);
            assertTrue("Data is exactly the same".equals(diffResult.getMessage()));
            assertTrue(ObjectUtils.isEmpty(diffResult.getLines()));
        } catch (IOException e) {
            throw e;
        } finally {
            if (tempLeftPath != null) {
                Files.deleteIfExists(tempLeftPath);
            }

            if (tempRightPath != null) {
                Files.deleteIfExists(tempRightPath);
            }
        }
    }

    @Test
    public void testMakeSameFile() throws IOException {
        Path tempLeftPath = null;
        Path tempRightPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            byte[] leftContent = new String("aaaaaaa").getBytes();
            byte[] rightContent = new String("aaaaaaabbbbbbb").getBytes();

            Files.write(tempLeftPath, leftContent, StandardOpenOption.CREATE);
            Files.write(tempRightPath, rightContent, StandardOpenOption.CREATE);

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));
            diffObject.setRightPart(Optional.of(tempRightPath));

            DiffResponseDTO diffResult = underTest.makeDiff(diffObject);

            assertTrue(diffResult != null);
            assertTrue("Data is NOT the same size".equals(diffResult.getMessage()));
            assertTrue(ObjectUtils.isEmpty(diffResult.getLines()));
        } catch (IOException e) {
            throw e;
        } finally {
            if (tempLeftPath != null) {
                Files.deleteIfExists(tempLeftPath);
            }

            if (tempRightPath != null) {
                Files.deleteIfExists(tempRightPath);
            }
        }
    }

    @Test
    public void testAddNewDiffWithLeftPart() throws Exception {
        Path tempLeftPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");

            String leftContent = new String("{ \"binary\": \"YWFhYWFh\" }");

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));

            URI leftPartURI = underTest.addNewDiff(diffObject, leftContent, true);

            assertTrue(tempLeftPath.toFile().toURI().equals(leftPartURI));
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void testAddNewDiffWithRightPart() throws Exception {
        Path tempRightPath = null;

        try {
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            String rightContent = new String("{ \"binary\": \"YWFhYWFh\" }");

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setRightPart(Optional.of(tempRightPath));

            URI rightPartURI = underTest.addNewDiff(diffObject, rightContent, false);

            assertTrue(tempRightPath.toFile().toURI().equals(rightPartURI));
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void testDeletePartFiles() throws Exception {
        Path tempLeftPath = null;
        Path tempRightPath = null;

        try {
            tempLeftPath = Files.createTempFile("tempLeftFile", ".tmp");
            tempRightPath = Files.createTempFile("tempRightFile", ".tmp");

            byte[] leftContent = new String("aaaaaaa").getBytes();
            byte[] rightContent = new String("aaaaaaa").getBytes();

            Files.write(tempLeftPath, leftContent, StandardOpenOption.CREATE);
            Files.write(tempRightPath, rightContent, StandardOpenOption.CREATE);

            DiffObject diffObject = new DiffObject(1l);
            diffObject.setLeftPart(Optional.of(tempLeftPath));
            diffObject.setRightPart(Optional.of(tempRightPath));

            ReflectionTestUtils.invokeMethod(underTest, "deletePartFiles", diffObject);

            assertTrue(Files.notExists(tempLeftPath, LinkOption.NOFOLLOW_LINKS));
            assertTrue(Files.notExists(tempRightPath, LinkOption.NOFOLLOW_LINKS));
        } catch (IOException e) {
            throw e;
        } finally {
            if (tempLeftPath != null) {
                Files.deleteIfExists(tempRightPath);
            }

            if (tempRightPath != null) {
                Files.deleteIfExists(tempRightPath);
            }
        }
    }
}
