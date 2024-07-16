package it.unibo.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Misurazione(
    String username,
    LocalDate data,
    BigDecimal peso
) {}