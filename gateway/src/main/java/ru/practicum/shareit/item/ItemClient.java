package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(long userId, ItemDto itemDto){
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> patchItem(long userId, long itemId, ItemDto itemDto){
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getAllByIdUser(long userId, Integer from, Integer size){
        if(from == null || size == null){
            return get("", userId);
        }
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getByIdItem(long itemId, long idUser){
        return get("/" + itemId, idUser);
    }

    public ResponseEntity<Object> searchItem(long userId, String text, Integer from, Integer size){
        if(from == null || size == null){
            Map<String, Object> parameters = Map.of(
                    "text", text
            );
            return get("/search" + "?text={text}", userId, parameters);
        }
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search" + "?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, AddCommentDto addCommentDto){
        return post("/" + itemId + "/comment", userId, addCommentDto);
    }
}
