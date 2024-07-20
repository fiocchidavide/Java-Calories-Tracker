package it.unibo.application.dto;

import java.util.Optional;

public record ValoriRicetta(String nome, Optional<Integer> porzione, boolean privato) {
} 
