package org.menesty.ikea.factory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by Menesty on 12/21/13.
 */
public class ImageFactory {

    public static ImageView createAdd48Img() {
        return new ImageView(new Image("/styles/images/icon/add1-48x48.png"));
    }

    public static ImageView creteEdit48Img() {
        return new ImageView(new Image("/styles/images/icon/edit-48x48.png"));
    }

    public static ImageView createOrders72Img(){
        return new ImageView(new Image("/styles/images/icon/orders-72x72.png"));
    }
}
