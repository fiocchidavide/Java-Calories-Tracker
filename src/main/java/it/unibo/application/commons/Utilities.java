package it.unibo.application.commons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class Utilities {
    public static Optional<Integer> strictlyPositive(int x) {
        return Optional.ofNullable(x > 0 ? x : null);
    }
    public static Optional<String> notBlank(String s) {
        return Optional.ofNullable(s.isBlank() ? null : s);
    }
    public static Optional<Integer> parseOptionalStrictlyPositiveInt(String s){
        try {
            return strictlyPositive(Integer.parseInt(s));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }
    public static <K,V> Map<K, V> mapFromLists(List<K> keys, List<V> values) {
        if(keys.size() != values.size()){
            throw new IllegalArgumentException("Keys and values lists must have same size.");
        }
        return IntStream.range(0, keys.size()).collect(() -> new HashMap<>(), (m, i) -> m.put(keys.get(i), values.get(i)), (m1, m2) -> m1.putAll(m2));
    }
    public static List<String> stringList(Object... args){
        return Arrays.stream(args).map(o -> String.valueOf(o)).toList();
    }
    public static <T> Optional<List<T>> notEmpty(List<T> l){
        return Optional.ofNullable(l.isEmpty() ? null : l);
    }
    public static <T> Optional<Set<T>> notEmpty(Set<T> l){
        return Optional.ofNullable(l.isEmpty() ? null : l);
    }        
}
