package it.unibo.application.dto;

import java.time.LocalDate;

public record Misurazione(
    String username,
    LocalDate data,
    int peso
) {}