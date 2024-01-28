package com.eden.navigatorfx.routing.utils;

public sealed interface Opt<V> {
    record Some<V>(V value) implements Opt<V>{}
    record None<V>() implements Opt<V>{}
}
