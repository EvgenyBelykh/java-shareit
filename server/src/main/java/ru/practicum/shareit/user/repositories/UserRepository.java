package ru.practicum.shareit.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmailContaining(String email);

    User getUserByEmail(String email);
}
