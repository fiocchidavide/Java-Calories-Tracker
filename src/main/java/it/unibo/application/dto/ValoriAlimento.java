package it.unibo.application.dto;

import java.util.Optional;

public record ValoriAlimento(
    String nome,
    int kcal,
    int carboidrati,
    int grassi,
    int proteine,
    Optional<Integer> porzione,
    Optional<String> brand,
    boolean privato
) {}
