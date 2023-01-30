package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositories.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserRepository;

import java.time.LocalDateTime;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    void verifyBootstrappingByPersistingAnItem() {
        Item item = new Item();
        item.setName("Вещь");
        item.setDescription("Необходимая вещь");
        item.setAvailable(true);

        Assertions.assertEquals(0, item.getId());
        em.persist(item);
        Assertions.assertEquals(1, item.getId());
    }

    @Test
    void verifyRepositoryByPersistingAnItem() {
        Item item = new Item();
        item.setName("Вещь");
        item.setDescription("Необходимая вещь");
        item.setAvailable(true);

        Assertions.assertEquals(0, item.getId());
        itemRepository.save(item);
        Assertions.assertEquals(1, item.getId());
    }

    @Test
    void searchTest() {
        Item firstItem = new Item();
        firstItem.setName("Вещь №1");
        firstItem.setDescription("Необходимая вещь №1");
        firstItem.setAvailable(true);
        itemRepository.save(firstItem);

        Item secondItem = new Item();
        secondItem.setName("Вещь №2");
        secondItem.setDescription("Необходимая вещь №2");
        secondItem.setAvailable(true);
        itemRepository.save(secondItem);

        Item thirdItem = new Item();
        thirdItem.setName("Вещь №2");
        thirdItem.setDescription("Необходимая вещь №2");
        thirdItem.setAvailable(false);
        itemRepository.save(thirdItem);

        Assertions.assertEquals(2, itemRepository.search("вещь").size());
        Assertions.assertEquals(1, itemRepository.search("вещь", PageRequest.of(0, 1)).getContent().size());
    }

    @Test
    void findOwnerByIdItemTest() {
        User firstUser = new User();
        firstUser.setName("ИмяРек №1");
        firstUser.setEmail("user1@email.ru");
        userRepository.save(firstUser);

        User secondUser = new User();
        secondUser.setName("ИмяРек №2");
        secondUser.setEmail("user2@email.ru");
        userRepository.save(secondUser);

        Item firstItem = new Item();
        firstItem.setName("Вещь №1");
        firstItem.setDescription("Необходимая вещь №1");
        firstItem.setAvailable(true);
        firstItem.setOwner(firstUser);
        itemRepository.save(firstItem);

        Item secondItem = new Item();
        secondItem.setName("Вещь №2");
        secondItem.setDescription("Необходимая вещь №2");
        secondItem.setAvailable(true);
        secondItem.setOwner(secondUser);
        itemRepository.save(secondItem);

        Assertions.assertEquals(secondUser.getId(), itemRepository.findOwnerByIdItem(secondItem.getId()));
    }

    @Test
    void findByItemRequestIdOrderByIdTest() {
        User firstUser = new User();
        firstUser.setName("ИмяРек №1");
        firstUser.setEmail("user1@email.ru");
        userRepository.save(firstUser);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Необходима вещь");
        itemRequest.setUser(firstUser);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequestRepository.save(itemRequest);

        User secondUser = new User();
        secondUser.setName("ИмяРек №2");
        secondUser.setEmail("user2@email.ru");
        userRepository.save(secondUser);

        Item firstItem = new Item();
        firstItem.setName("Вещь №1");
        firstItem.setDescription("Необходимая вещь №1");
        firstItem.setAvailable(true);
        firstItem.setOwner(secondUser);
        firstItem.setItemRequest(itemRequest);
        itemRepository.save(firstItem);

        Assertions.assertEquals(1, itemRepository.findByItemRequestIdOrderById(itemRequest.getId()).size());
        Assertions.assertEquals(firstItem.getId(),
                itemRepository.findByItemRequestIdOrderById(itemRequest.getId()).get(0).getId());
    }
}
