package it.unibo.application.commons;

import java.util.Optional;

public class Utilities {
    public static Optional<Integer> strictlyPositive(int x) {
        return Optional.ofNullable(x > 0 ? x : null);
    }
    public static Optional<String> notBlank(String s) {
        return Optional.ofNullable(s.isBlank() ? s : null);
    }
    public static Optional<Integer> parseOptionalInt(String s){
        try {
            return strictlyPositive(Integer.parseInt(s));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }   
}
