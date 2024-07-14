package it.unibo.application.dto;

import java.util.Optional;

public record Alimento(
    int codAlimento,
    String nome,
    int kcal,
    int carboidrati,
    int grassi,
    int proteine,
    Optional<Integer> porzione,
    char tipo,
    Optional<String> brand,
    String proprietario,
    boolean privato
) {}