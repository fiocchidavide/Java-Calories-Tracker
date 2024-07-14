package it.unibo.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record Consumazione(
    String username,
    int numero,
    LocalDate data,
    LocalTime ora,
    int codAlimento,
    int quantità
) {}
