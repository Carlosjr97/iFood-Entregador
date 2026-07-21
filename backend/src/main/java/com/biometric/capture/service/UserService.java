package com.biometric.capture.service;

import com.biometric.capture.domain.User;
import com.biometric.capture.dto.CreateUserRequest;
import com.biometric.capture.dto.UserDto;
import com.biometric.capture.exception.ResourceNotFoundException;
import com.biometric.capture.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(CreateUserRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseGet(() -> userRepository.save(new User(request.name(), request.email())));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        return toDto(findUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
