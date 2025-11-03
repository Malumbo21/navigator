package com.eden.navigatorfx.v2.layout;

import javafx.scene.Parent;

import java.util.function.BiConsumer;

public class NavLayoutFn implements NavLayout {
    private final Parent layoutRoot;
    private final BiConsumer<Parent, Parent> setContentFunction;
    public NavLayoutFn(final Parent layoutRoot, final BiConsumer<Parent, Parent> setContentFunction) {
        this.layoutRoot = layoutRoot;
        this.setContentFunction = setContentFunction;
    }

    @Override
    public void setContent(Parent view) {
        setContentFunction.accept(layoutRoot, view);
    }
}
