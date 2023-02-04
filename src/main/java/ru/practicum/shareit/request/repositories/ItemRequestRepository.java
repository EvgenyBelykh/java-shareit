package ru.practicum.shareit.request.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findItemRequestByUserIdOrderByCreatedAsc(long userId);

    Optional<ItemRequest> findItemRequestById(long requestId);

    Page<ItemRequest> findItemRequestByUserIdNot(Long userId, Pageable pageable);

    List<ItemRequest> findItemRequestByUserIdNot(Long userId);
}
