package com.sun.javafx.scene.control.skin;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.Region;

/**
 * Created by Menesty on
 * 9/6/15.
 * 02:42.
 */
public class AutoCompleteComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> {
    private boolean popupNeedsReconfiguring = true;
    /***************************************************************************
     * *
     * Constructors                                                            *
     * *
     * ************************************************************************
     *
     * @param comboBox
     */
    public AutoCompleteComboBoxListViewSkin(ComboBox<T> comboBox) {
        super(comboBox);
    }


    @Override public void show() {
        if (getSkinnable() == null) {
            throw new IllegalStateException("ComboBox is null");
        }

        Node content = getPopupContent();
        if (content == null) {
            throw new IllegalStateException("Popup node is null");
        }

        if (getPopup().isShowing()) return;

        positionAndShowPopup();
    }

    private void positionAndShowPopup() {
        final PopupControl _popup = getPopup();
        _popup.getScene().setNodeOrientation(getSkinnable().getEffectiveNodeOrientation());


        final Node popupContent = getPopupContent();
        popupContent.applyCss();
        popupContent.autosize();
        Point2D p = getPrefPopupPosition();

        popupNeedsReconfiguring = true;
        reconfigurePopup();

        final ComboBoxBase<T> comboBoxBase = getSkinnable();
        _popup.show(comboBoxBase.getScene().getWindow(), p.getX(), p.getY());

        popupContent.requestFocus();
    }

    private Point2D getPrefPopupPosition() {
        double dx = 0;
        dx += (getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) ? -3 : 0;
        return com.sun.javafx.util.Utils.pointRelativeTo(getSkinnable(), getPopupContent(), HPos.CENTER, VPos.BOTTOM, dx, 0, false);
    }

    @Override
    void reconfigurePopup() {
        // RT-26861. Don't call getPopup() here because it may cause the popup
        // to be created too early, which leads to memory leaks like those noted
        // in RT-32827.
        if (popup == null) return;

        final boolean isShowing = popup.isShowing();
        if (! isShowing) return;

        if (! popupNeedsReconfiguring) return;
        popupNeedsReconfiguring = false;

        final Point2D p = getPrefPopupPosition();

        final Node popupContent = getPopupContent();
        final double minWidth = popupContent.prefWidth(1);
        final double minHeight = popupContent.prefHeight(1);

        if (p.getX() > -1) popup.setAnchorX(p.getX());
        if (p.getY() > -1) popup.setAnchorY(p.getY());
        if (minWidth > -1) popup.setMinWidth(minWidth);
        if (minHeight > -1) popup.setMinHeight(minHeight);

        final Bounds b = popupContent.getLayoutBounds();
        final double currentWidth = b.getWidth();
        final double currentHeight = b.getHeight();
        final double newWidth  = currentWidth < minWidth ? minWidth : currentWidth;
        final double newHeight = currentHeight < minHeight ? minHeight : currentHeight;

        if (newWidth != currentWidth || newHeight != currentHeight) {
            // Resizing content to resolve issues such as RT-32582 and RT-33700
            // (where RT-33700 was introduced due to a previous fix for RT-32582)
            popupContent.resize(newWidth, newHeight);
            if (popupContent instanceof Region) {
                ((Region)popupContent).setMinSize(newWidth, newHeight);
                ((Region)popupContent).setPrefSize(newWidth, newHeight);
            }
        }
    }

}
