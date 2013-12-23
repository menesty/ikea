package org.menesty.ikea.factory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by Menesty on 12/21/13.
 */
public class ImageFactory {

    public static ImageView createAdd48Icon() {
        return new ImageView(new Image("/styles/images/icon/add1-48x48.png"));
    }

    public static ImageView creteEdit48Icon() {
        return new ImageView(new Image("/styles/images/icon/edit-48x48.png"));
    }

    public static ImageView createOrders72Icon() {
        return new ImageView(new Image("/styles/images/icon/orders-72x72.png"));
    }

    public static ImageView createPlus32Icon() {
        return new ImageView(new Image("/styles/images/icon/plus-32x32.png"));
    }

    public static ImageView createMinus32Icon() {
        return new ImageView(new Image("/styles/images/icon/minus-32x32.png"));
    }

    public static ImageView createBalance32Icon() {
        return new ImageView(new Image("/styles/images/icon/balance-32x32.png"));
    }

    public static ImageView createReload32Icon() {
        return new ImageView(new Image("/styles/images/icon/reload-32x32.png"));
    }

    public static ImageView createSave32Icon() {
        return new ImageView(new Image("/styles/images/icon/save-32x32.png"));
    }

    public static ImageView createEppExport32Icon() {
        return new ImageView(new Image("/styles/images/icon/epp-32x32.png"));
    }

    public static ImageView createXlsExportIcon() {
        return new ImageView(new Image("/styles/images/icon/xls-32x32.png"));
    }
}
