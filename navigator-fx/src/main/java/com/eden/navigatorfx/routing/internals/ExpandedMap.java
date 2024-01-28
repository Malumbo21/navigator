package com.eden.navigatorfx.routing.internals;

import java.util.HashMap;
import java.util.function.Consumer;

public class ExpandedMap<K,V> extends HashMap<K,V> {

    public void ifPresent(String key,Consumer<V> consumer){
        if(containsKey(key)){
            consumer.accept(get(key));
        }
    }
}
