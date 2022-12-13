package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private long id;

    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    private String email;

    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    private String name;
}
