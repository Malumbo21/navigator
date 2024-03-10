package com.eden.navigatorfx.update;

import java.util.Stack;

import javafx.scene.Scene;

public class SceneStack {
    private Stack<Scene> stack;

    public SceneStack() {
        this.stack = new Stack<>();
    }

    public void push(Scene scene) {
        this.stack.push(scene);
    }

    public Scene pop() {
        return this.stack.pop();
    }

    public boolean canPop() {
        return this.stack.size() > 1;
    }

    public Scene getCurrentScene() {
        return this.stack.peek();
    }
}
