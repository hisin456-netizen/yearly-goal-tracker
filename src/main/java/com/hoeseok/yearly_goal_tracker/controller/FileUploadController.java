package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    private final String uploadDir = "uploads/";

    // /uploads/** 는 인증 없이 서빙되므로 html/svg 같은 스크립트 실행 가능 형식은 받지 않는다.
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp");
    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/gif", ".gif",
            "image/webp", ".webp");

    @PostConstruct
    public void init() {
        new File(uploadDir).mkdirs();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(
            @RequestParam("file") MultipartFile file) {
        String ext = resolveImageExtension(file);
        try {
            String filename = UUID.randomUUID() + ext;
            Path path = Paths.get(uploadDir, filename);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(ApiResponse.success(Map.of("url", "/uploads/" + filename)));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 저장 파일명에 붙일 확장자를 허용 목록에서만 고른다.
     * 클라이언트가 보낸 파일명의 확장자는 허용 목록에 있을 때만 쓰고(경로 문자가 섞인 값 포함 전부 거부),
     * 확장자가 없으면 Content-Type 으로 정한다.
     */
    private String resolveImageExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
            }
            return ext;
        }
        String ext = EXTENSION_BY_CONTENT_TYPE.get(file.getContentType());
        if (ext == null) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
        return ext;
    }
}
