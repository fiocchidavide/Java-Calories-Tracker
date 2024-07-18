package it.unibo.application.dto;

import java.util.Optional;

public record ValoriCibo(
    String nome,
    int kcal,
    int proteine,
    int grassi,
    int carboidrati,
    Optional<Integer> porzione,
    Optional<String> brand,
    boolean privato
) {}
