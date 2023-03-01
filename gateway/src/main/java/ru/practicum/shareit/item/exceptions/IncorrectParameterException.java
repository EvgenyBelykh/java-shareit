package ru.practicum.shareit.item.exceptions;

public class IncorrectParameterException extends RuntimeException  {
    private final String parameter;

    public IncorrectParameterException(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
