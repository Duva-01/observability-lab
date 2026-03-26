package com.mercadona.devops.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadona.devops.userservice.model.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
