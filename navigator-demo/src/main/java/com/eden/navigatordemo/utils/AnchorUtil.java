package com.eden.navigatordemo.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import javax.lang.model.element.AnnotationValue;

public class AnchorUtil {

    public static void anchor(Node node){
        anchor(node,0);
    }

    public static void anchor(Node node, double all) {
        anchorXY(node,all,all);
    }

    public static void anchorXY(Node node, double x, double y) {
        anchorY(node,y);
        anchorX(node, x);
    }

    public static void anchorX(Node node, double x) {
        AnchorPane.setLeftAnchor(node, x);
        AnchorPane.setRightAnchor(node, x);
    }

    public static void anchorY(Node node, double y) {
        AnchorPane.setTopAnchor(node, y);
        AnchorPane.setBottomAnchor(node, y);
    }
}
