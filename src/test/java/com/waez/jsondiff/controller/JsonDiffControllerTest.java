package com.waez.jsondiff.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waez.jsondiff.model.DiffObject;
import com.waez.jsondiff.service.DiffService;

import dto.DiffResponseDTO;

public class JsonDiffControllerTest {

    private static final String FILE_LOCATION = "file:///C:/Temp/mock.tmp";

    @Mock
    private DiffService diffService;

    @InjectMocks
    private JsonDiffController underTest;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(underTest).build();
    }

    @Test
    public void testLeftDiffNoPreviousLeftPart() throws Exception {
        boolean left = true;
        DiffObject diffObject = new DiffObject(1l);

        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(diffObject);
        when(diffService.addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left)))
                .thenReturn(URI.create(FILE_LOCATION));

        MvcResult responseBody = this.mockMvc
                .perform(post("/v1/diff/{id}/left", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated()).andReturn();

        assertTrue(FILE_LOCATION.equals(responseBody.getResponse().getHeader("location")));

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService).addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left));
    }

    @Test
    public void testLeftDiffPreviousLeftPart() throws Exception {
        boolean left = true;

        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(null);
        when(diffService.addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left)))
                .thenReturn(URI.create(FILE_LOCATION));

        MvcResult responseBody = this.mockMvc
                .perform(post("/v1/diff/{id}/left", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated()).andReturn();

        assertTrue(FILE_LOCATION.equals(responseBody.getResponse().getHeader("location")));

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService).addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left));
    }

    @Test
    public void testLeftDiffPreviousLeftPartThrowsIOException() throws Exception {
        boolean left = true;

        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(null);
        when(diffService.addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left)))
                .thenThrow(new IOException("Test Exception"));

        this.mockMvc.perform(post("/v1/diff/{id}/left", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is5xxServerError());

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService).addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left));
    }

    @Test
    public void testRightDiffNoPreviousRightPart() throws Exception {
        boolean left = false;

        DiffObject diffObject = new DiffObject(1l);

        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(diffObject);
        when(diffService.addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left)))
                .thenReturn(URI.create(FILE_LOCATION));

        MvcResult responseBody = this.mockMvc
                .perform(post("/v1/diff/{id}/right", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated()).andReturn();

        assertTrue(FILE_LOCATION.equals(responseBody.getResponse().getHeader("location")));

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService).addNewDiff(Matchers.any(DiffObject.class), Matchers.anyObject(), Matchers.eq(left));
    }

    @Test
    public void testGetDiffWithBothParts() throws Exception {
        DiffResponseDTO expected = new DiffResponseDTO();
        expected.setMessage("Ok");

        DiffObject diffObject = new DiffObject(1l);
        diffObject.setLeftPart(Optional.of(Paths.get("C:/")));
        diffObject.setRightPart(Optional.of(Paths.get("C:/")));

        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(diffObject);
        when(diffService.makeDiff(diffObject)).thenReturn(expected);

        MvcResult responseBody = this.mockMvc
                .perform(get("/v1/diff/{id}", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andReturn();

        // Checking if response is a valid JSON.
        new ObjectMapper().readTree(responseBody.getResponse().getContentAsString());

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService).makeDiff(diffObject);
    }

    @Test
    public void testGetDiffWithOnePart() throws Exception {
        DiffResponseDTO expected = new DiffResponseDTO();
        expected.setMessage("Ok");

        DiffObject diffObject = new DiffObject(1l);
        diffObject.setLeftPart(Optional.of(Paths.get("C:/")));

        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(diffObject);
        when(diffService.makeDiff(diffObject)).thenReturn(expected);

        this.mockMvc.perform(get("/v1/diff/{id}", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService, never()).makeDiff(diffObject);
    }

    @Test
    public void testGetDiffWithBothThrowsIOException() throws Exception {
        DiffResponseDTO expected = new DiffResponseDTO();
        expected.setMessage("Ok");

        DiffObject diffObject = new DiffObject(1l);
        diffObject.setLeftPart(Optional.of(Paths.get("C:/")));
        diffObject.setRightPart(Optional.of(Paths.get("C:/")));

        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(diffObject);
        when(diffService.makeDiff(diffObject)).thenThrow(new IOException());

        this.mockMvc.perform(get("/v1/diff/{id}", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isInternalServerError());

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService).makeDiff(diffObject);
    }

    @Test
    public void testGetDiffWithNoParts() throws Exception {
        when(diffService.getDiffById(Matchers.anyLong())).thenReturn(null);

        this.mockMvc.perform(get("/v1/diff/{id}", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());

        verify(diffService).getDiffById(Matchers.anyLong());
        verify(diffService, never()).makeDiff(Matchers.any(DiffObject.class));
    }
}
