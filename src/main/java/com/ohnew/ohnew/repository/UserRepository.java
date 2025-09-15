package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.User;
import com.ohnew.ohnew.entity.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndProvider(String email, Provider provider);
}
