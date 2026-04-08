package com.church.controller;

import com.church.model.Sermon;
import com.church.repository.SermonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class SermonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SermonRepository sermonRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clean() {
        sermonRepository.deleteAll();
    }

    private Sermon createAndSaveSermon(String title) {
        Sermon sermon = new Sermon();
        sermon.setTitle(title);
        sermon.setDate(LocalDate.now());
        return sermonRepository.save(sermon);
    }

    private Map<String, Object> createSermonRequest(String title) {
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("date", LocalDate.now().toString());
        return request;
    }

    @Test
    @DisplayName("/api/sermons/{id} 단건 조회")
    void getSermonById() throws Exception {
        Sermon saved = createAndSaveSermon("단건 테스트");

        mockMvc.perform(get("/api/sermons/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("단건 테스트"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("/api/sermons POST 설교 생성")
    void createSermon() throws Exception {
        Map<String, Object> request = createSermonRequest("생성 테스트");
        request.put("description", "설교 요약");
        request.put("preacher", "목사님");

        mockMvc.perform(post("/api/sermons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("생성 테스트"))
                .andExpect(jsonPath("$.data.description").value("설교 요약"))
                .andExpect(jsonPath("$.data.preacher").value("목사님"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("/api/sermons/{id} PUT 설교 수정")
    void updateSermon() throws Exception {
        Sermon saved = createAndSaveSermon("수정 전");

        Map<String, Object> update = createSermonRequest("수정 후");

        mockMvc.perform(put("/api/sermons/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("수정 후"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("/api/sermons/{id} DELETE 설교 삭제")
    void deleteSermon() throws Exception {
        Sermon saved = createAndSaveSermon("삭제 테스트");

        mockMvc.perform(delete("/api/sermons/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Assertions.assertFalse(sermonRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("/api/sermons 페이징 목록 조회")
    void getSermons() throws Exception {
        createAndSaveSermon("A");
        createAndSaveSermon("B");

        mockMvc.perform(get("/api/sermons")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    @DisplayName("/api/sermons/all 전체 목록 조회")
    void getAllSermons() throws Exception {
        createAndSaveSermon("A");
        createAndSaveSermon("B");

        mockMvc.perform(get("/api/sermons/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("/api/sermons/search/title 제목 검색")
    void searchByTitle() throws Exception {
        createAndSaveSermon("테스트 설교");
        createAndSaveSermon("다른 설교");

        mockMvc.perform(get("/api/sermons/search/title")
                        .param("title", "테스트")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("테스트 설교"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("유효성 검증 - 제목 빈 값")
    void createSermon_validationError() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "");
        request.put("date", LocalDate.now().toString());

        mockMvc.perform(post("/api/sermons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 404")
    void getSermonById_notFound() throws Exception {
        mockMvc.perform(get("/api/sermons/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
