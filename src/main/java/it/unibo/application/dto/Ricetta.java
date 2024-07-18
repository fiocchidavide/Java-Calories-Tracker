package it.unibo.application.dto;

import java.util.Map;

public record Ricetta(String username, ValoriRicetta ricetta, Map<Alimento, Integer> ingredienti) {
    
}
