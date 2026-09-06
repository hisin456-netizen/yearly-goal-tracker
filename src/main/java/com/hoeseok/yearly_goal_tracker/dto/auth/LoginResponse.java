package com.hoeseok.yearly_goal_tracker.dto.auth;

import com.hoeseok.yearly_goal_tracker.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private final String token;
    private final UserResponse user;
}
