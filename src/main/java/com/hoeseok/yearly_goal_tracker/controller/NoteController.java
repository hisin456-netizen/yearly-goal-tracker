package com.hoeseok.yearly_goal_tracker.controller;

import com.hoeseok.yearly_goal_tracker.common.response.ApiResponse;
import com.hoeseok.yearly_goal_tracker.domain.enums.NoteType;
import com.hoeseok.yearly_goal_tracker.dto.note.NoteCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.note.NoteResponse;
import com.hoeseok.yearly_goal_tracker.dto.note.NoteUpdateRequest;
import com.hoeseok.yearly_goal_tracker.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NoteCreateRequest request
    ) {
        NoteResponse response = noteService.createNote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("노트가 저장되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotes(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) NoteType type,
            @RequestParam(required = false) Long goalId
    ) {
        List<NoteResponse> responses = noteService.getNotes(userId, type, goalId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody NoteUpdateRequest request
    ) {
        NoteResponse response = noteService.updateNote(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("노트가 수정되었습니다.", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        noteService.deleteNote(id, userId);
        return ResponseEntity.ok(ApiResponse.success("노트가 삭제되었습니다.", null));
    }
}
