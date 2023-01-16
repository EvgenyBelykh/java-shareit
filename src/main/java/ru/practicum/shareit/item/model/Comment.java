package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "comments", schema = "public")
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Builder
@ToString

public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    @ToString.Exclude
    private Item item;

    @Column(name = "created")
    private LocalDateTime created;

    public Comment(String text, User author, Item item, LocalDateTime created) {
        this.text = text;
        this.author = author;
        this.item = item;
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id == comment.id && Objects.equals(text, comment.text) && Objects.equals(author, comment.author) && Objects.equals(item, comment.item) && Objects.equals(created, comment.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, author, item, created);
    }
}
