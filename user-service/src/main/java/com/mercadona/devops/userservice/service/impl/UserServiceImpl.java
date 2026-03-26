package com.mercadona.devops.userservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadona.devops.essentials.error.BadRequestException;
import com.mercadona.devops.essentials.error.ResourceNotFoundException;
import com.mercadona.devops.userservice.dto.CreateUserRequest;
import com.mercadona.devops.userservice.dto.UpdateUserRequest;
import com.mercadona.devops.userservice.dto.UserDto;
import com.mercadona.devops.userservice.model.UserEntity;
import com.mercadona.devops.userservice.repository.UserRepository;
import com.mercadona.devops.userservice.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        log.info("Retrieved {} users", users.size());
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        UserDto user = toDto(loadUser(id));
        log.info("Retrieved user with id {}", id);
        return user;
    }

    @Override
    public UserDto create(CreateUserRequest request) {
        validateEmailUniqueness(request.getEmail(), null);
        UserEntity entity = new UserEntity(request.getName(), request.getEmail(), request.getSegment());
        UserDto createdUser = toDto(userRepository.save(entity));
        log.info("Created user with id {} and segment {}", createdUser.getId(), createdUser.getSegment());
        return createdUser;
    }

    @Override
    public UserDto update(Long id, UpdateUserRequest request) {
        UserEntity entity = loadUser(id);
        validateEmailUniqueness(request.getEmail(), id);
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setSegment(request.getSegment());
        UserDto updatedUser = toDto(userRepository.save(entity));
        log.info("Updated user with id {}", id);
        return updatedUser;
    }

    @Override
    public void delete(Long id) {
        UserEntity entity = loadUser(id);
        userRepository.delete(entity);
        log.info("Deleted user with id {}", id);
    }

    private UserEntity loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateEmailUniqueness(String email, Long id) {
        boolean exists = id == null
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, id);
        if (exists) {
            log.warn("Rejected user operation because email {} already exists", email);
            throw new BadRequestException("User email already exists");
        }
    }

    private UserDto toDto(UserEntity entity) {
        return new UserDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getSegment());
    }
}
