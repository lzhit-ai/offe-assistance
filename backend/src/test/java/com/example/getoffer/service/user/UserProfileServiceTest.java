package com.example.getoffer.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.getoffer.dto.auth.UserProfileResponse;
import com.example.getoffer.dto.user.UpdateProfileRequest;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.UserRepository;
import com.example.getoffer.service.auth.AuthService;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserProfileService service;

    @Test
    void updateProfileStoresTrimmedNickname() {
        User user = new User();
        user.setId(1L);
        user.setUsername("login_name");
        user.setNickname("old_name");

        UserProfileResponse response = new UserProfileResponse();
        response.setUsername("login_name");
        response.setNickname("new_name");

        when(userRepository.findByUsername("login_name")).thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndIdNot("new_name", 1L)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(authService.toUserProfile(user)).thenReturn(response);

        UserProfileResponse result = service.updateProfile("login_name", new UpdateProfileRequest(" new_name "));

        assertThat(user.getNickname()).isEqualTo("new_name");
        assertThat(result.getNickname()).isEqualTo("new_name");
    }

    @Test
    void updateProfileRejectsDuplicateNickname() {
        User user = new User();
        user.setId(1L);
        user.setUsername("login_name");

        when(userRepository.findByUsername("login_name")).thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndIdNot("taken_name", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateProfile("login_name", new UpdateProfileRequest("taken_name")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nickname");
    }

    @Test
    void updateProfileRejectsNicknameLongerThanTenCharacters() {
        User user = new User();
        user.setId(1L);
        user.setUsername("login_name");

        when(userRepository.findByUsername("login_name")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.updateProfile("login_name", new UpdateProfileRequest("12345678901")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
