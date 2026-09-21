package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.exception.GlobalExceptionHandler;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileUploadControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FileUploadController controller = new FileUploadController();
        controller.init();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static MockMultipartFile file(String originalName, String contentType) {
        return new MockMultipartFile("file", originalName, contentType, "content".getBytes());
    }

    /** 응답의 /uploads/<이름> 으로 저장된 파일을 찾아 지우고, 저장된 파일명을 돌려준다. */
    private static String deleteStored(String responseBody) throws Exception {
        String url = JsonPath.read(responseBody, "$.data.url");
        String stored = url.substring("/uploads/".length());
        assertThat(Files.deleteIfExists(Path.of("uploads", stored))).isTrue();
        return stored;
    }

    @Test
    @DisplayName("이미지 확장자(대문자 포함)는 업로드되고 소문자 확장자로 저장된다")
    void image_isAccepted() throws Exception {
        String body = mockMvc.perform(multipart("/api/v1/upload").file(file("photo.PNG", "image/png")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(deleteStored(body)).endsWith(".png");
    }

    @Test
    @DisplayName("확장자가 없으면 Content-Type 으로 확장자를 정한다")
    void noExtension_usesContentType() throws Exception {
        String body = mockMvc.perform(multipart("/api/v1/upload").file(file("blob", "image/jpeg")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(deleteStored(body)).endsWith(".jpg");
    }

    @Test
    @DisplayName("html 은 거부 (인증 없이 서빙되는 /uploads 에서 스크립트가 실행되는 것을 방지)")
    void html_isRejected() throws Exception {
        mockMvc.perform(multipart("/api/v1/upload").file(file("evil.html", "text/html")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("html 을 image/png 로 속여도 확장자가 허용 목록에 없으면 거부")
    void html_disguisedContentType_isRejected() throws Exception {
        mockMvc.perform(multipart("/api/v1/upload").file(file("evil.html", "image/png")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("svg 는 스크립트를 담을 수 있어 거부")
    void svg_isRejected() throws Exception {
        mockMvc.perform(multipart("/api/v1/upload").file(file("logo.svg", "image/svg+xml")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("파일명에 경로 문자를 섞어 uploads 밖에 쓰려는 시도는 거부")
    void pathTraversal_isRejected() throws Exception {
        mockMvc.perform(multipart("/api/v1/upload").file(file("a.png/../../evil", "image/png")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("확장자가 없고 이미지 Content-Type 도 아니면 거부")
    void noExtension_unknownContentType_isRejected() throws Exception {
        mockMvc.perform(multipart("/api/v1/upload").file(file("blob", "text/html")))
                .andExpect(status().isBadRequest());
    }
}
