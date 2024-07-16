package it.unibo.application.dto;

import java.util.Optional;

public record Target(int kcal, Optional<Integer> percentualeProteine, Optional<Integer> percentualeGrassi,
        Optional<Integer> percentualeCarboidrati) {
}
