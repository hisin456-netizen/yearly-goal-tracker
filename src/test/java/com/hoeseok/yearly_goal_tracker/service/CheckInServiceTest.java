package com.hoeseok.yearly_goal_tracker.service;

import com.hoeseok.yearly_goal_tracker.common.exception.CustomException;
import com.hoeseok.yearly_goal_tracker.common.exception.ErrorCode;
import com.hoeseok.yearly_goal_tracker.domain.CheckIn;
import com.hoeseok.yearly_goal_tracker.domain.SubTask;
import com.hoeseok.yearly_goal_tracker.domain.enums.CheckInStatus;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInCreateRequest;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInResponse;
import com.hoeseok.yearly_goal_tracker.dto.checkin.CheckInUpdateRequest;
import com.hoeseok.yearly_goal_tracker.repository.CheckInRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private SubTaskService subTaskService;

    @InjectMocks
    private CheckInService checkInService;

    private SubTask subTask;
    private CheckIn checkIn;

    @BeforeEach
    void setUp() {
        subTask = SubTask.builder()
                .id(1L)
                .title("매주 모의고사 1회 풀기")
                .build();

        checkIn = CheckIn.builder()
                .id(100L)
                .subTask(subTask)
                .checkInDate(LocalDate.of(2026, 8, 30))
                .status(CheckInStatus.SUCCESS)
                .progressRate(100)
                .memo("1회차 85점 합격권")
                .build();
    }

    @Test
    @DisplayName("체크인 등록 성공")
    void createCheckIn_success() {
        // given
        LocalDate today = LocalDate.of(2026, 8, 30);
        CheckInCreateRequest request = CheckInCreateRequest.builder()
                .checkInDate(today)
                .status(CheckInStatus.SUCCESS)
                .progressRate(100)
                .memo("1회차 85점 합격권")
                .build();

        given(subTaskService.findSubTaskById(1L)).willReturn(subTask);
        given(checkInRepository.existsBySubTaskIdAndCheckInDate(1L, today)).willReturn(false);
        given(checkInRepository.save(any(CheckIn.class))).willReturn(checkIn);

        // when
        CheckInResponse response = checkInService.createCheckIn(1L, request);

        // then
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCheckInDate()).isEqualTo(today);
        assertThat(response.getStatus()).isEqualTo(CheckInStatus.SUCCESS);
        assertThat(response.getMemo()).isEqualTo("1회차 85점 합격권");
    }

    @Test
    @DisplayName("체크인 등록 실패 - 같은 날짜 중복 체크인")
    void createCheckIn_duplicateDate() {
        // given
        LocalDate today = LocalDate.of(2026, 8, 30);
        CheckInCreateRequest request = CheckInCreateRequest.builder()
                .checkInDate(today)
                .status(CheckInStatus.SUCCESS)
                .build();

        given(subTaskService.findSubTaskById(1L)).willReturn(subTask);
        given(checkInRepository.existsBySubTaskIdAndCheckInDate(1L, today)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> checkInService.createCheckIn(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHECK_IN_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("하위 태스크별 체크인 목록 조회 성공")
    void getCheckInsBySubTaskId_success() {
        // given
        given(checkInRepository.findBySubTaskIdOrderByCheckInDateDesc(1L)).willReturn(List.of(checkIn));

        // when
        List<CheckInResponse> responses = checkInService.getCheckInsBySubTaskId(1L);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("체크인 수정 성공")
    void updateCheckIn_success() {
        // given
        CheckInUpdateRequest request = CheckInUpdateRequest.builder()
                .status(CheckInStatus.FAIL)
                .progressRate(30)
                .memo("미완료")
                .build();

        given(checkInRepository.findById(100L)).willReturn(Optional.of(checkIn));

        // when
        CheckInResponse response = checkInService.updateCheckIn(100L, request);

        // then
        assertThat(response.getStatus()).isEqualTo(CheckInStatus.FAIL);
        assertThat(response.getProgressRate()).isEqualTo(30);
        assertThat(response.getMemo()).isEqualTo("미완료");
    }

    @Test
    @DisplayName("체크인 삭제 성공")
    void deleteCheckIn_success() {
        // given
        given(checkInRepository.findById(100L)).willReturn(Optional.of(checkIn));

        // when
        checkInService.deleteCheckIn(100L);

        // then
        verify(checkInRepository).delete(checkIn);
    }
}
