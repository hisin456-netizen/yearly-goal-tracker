package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.Goal;
import com.hoeseok.yearly_goal_tracker.domain.Note;
import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.domain.enums.NoteType;
import com.hoeseok.yearly_goal_tracker.dto.note.NoteCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.note.NoteResponse;
import com.hoeseok.yearly_goal_tracker.dto.note.NoteUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.GoalRepository;
import com.hoeseok.yearly_goal_tracker.repository.NoteRepository;
import com.hoeseok.yearly_goal_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    public NoteResponse createNote(Long userId, NoteCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Goal goal = null;
        if (request.getGoalId() != null) {
            goal = goalRepository.findById(request.getGoalId())
                    .orElseThrow(() -> new CustomException(ErrorCode.GOAL_NOT_FOUND));
        }

        Note note = Note.builder()
                .user(user)
                .goal(goal)
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .workDate(request.getWorkDate())
                .build();

        return new NoteResponse(noteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes(Long userId, NoteType type, Long goalId) {
        List<Note> notes;
        if (goalId != null) {
            notes = noteRepository.findByUserIdAndGoalIdOrderByCreatedAtDesc(userId, goalId);
        } else if (type != null) {
            notes = noteRepository.findByUserIdAndTypeOrderByIsPinnedDescCreatedAtDesc(userId, type);
        } else {
            notes = noteRepository.findByUserIdOrderByIsPinnedDescCreatedAtDesc(userId);
        }
        return notes.stream().map(NoteResponse::new).toList();
    }

    public NoteResponse updateNote(Long noteId, Long userId, NoteUpdateRequest request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTE_NOT_FOUND));

        if (!note.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        note.update(request.getTitle(), request.getContent(), request.getIsPinned(), request.getWorkDate());
        return new NoteResponse(note);
    }

    public void deleteNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTE_NOT_FOUND));

        if (!note.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        noteRepository.delete(note);
    }
}
