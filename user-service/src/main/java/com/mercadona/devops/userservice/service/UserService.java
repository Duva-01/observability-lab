package com.mercadona.devops.userservice.service;

import java.util.List;

import com.mercadona.devops.userservice.dto.CreateUserRequest;
import com.mercadona.devops.userservice.dto.UpdateUserRequest;
import com.mercadona.devops.userservice.dto.UserDto;

public interface UserService {

    List<UserDto> findAll();

    UserDto findById(Long id);

    UserDto create(CreateUserRequest request);

    UserDto update(Long id, UpdateUserRequest request);

    void delete(Long id);
}
