package ru.practicum.shareit.item.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(value = "SELECT i FROM Item i WHERE LOWER(i.name) LIKE %:text% OR LOWER (i.description) LIKE %:text% " +
            "AND i.available = true")
    List<Item> search(@Param("text") String text);

    List<Item> findByOwnerIdOrderById(long idUser);

    @Query(value = "SELECT i.owner.id FROM Item i WHERE i.id = ?1")
    long findOwnerByIdItem(long itemId);
    @Query(value = "SELECT i FROM Item i WHERE i.itemRequest.id = ?1")
    List<Item> findByItemRequestIdOrderById(long requestId);
}
