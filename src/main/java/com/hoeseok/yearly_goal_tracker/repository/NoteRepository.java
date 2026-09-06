package com.hoeseok.yearly_goal_tracker.repository;

import com.hoeseok.yearly_goal_tracker.domain.Note;
import com.hoeseok.yearly_goal_tracker.domain.enums.NoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserIdOrderByIsPinnedDescCreatedAtDesc(Long userId);

    List<Note> findByUserIdAndTypeOrderByIsPinnedDescCreatedAtDesc(Long userId, NoteType type);

    List<Note> findByUserIdAndGoalIdOrderByCreatedAtDesc(Long userId, Long goalId);
}
