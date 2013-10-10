package org.menesty.ikea.ui.pages;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.IkeaApplication;

public abstract class BasePage {
    String breadCrumbPath;

    String name;

    public BasePage(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    void setBreadCrumbPath(String path) {
        breadCrumbPath = path;
    }

    public String getBreadCrumb() {

        return StringUtils.isNotBlank(breadCrumbPath) ? breadCrumbPath + "/" + getName() : getName();
    }

    public abstract Node createView();

    public Node createTile(){
        Button tile = new Button(getName().trim(),getIcon());
        tile.setMinSize(140,145);
        tile.setPrefSize(140,145);
        tile.setMaxSize(140,145);
        tile.setContentDisplay(ContentDisplay.TOP);
        tile.getStyleClass().clear();
        tile.getStyleClass().add("sample-tile");
        tile.setOnAction(new EventHandler() {
            public void handle(Event event) {
                IkeaApplication.getPageManager().goToPage(BasePage.this);
            }
        });
        return tile;

    }

    protected Node getPreviewIcon(){
        return null;
    }

    protected Node createIconContent(){
        return null;
    }

    private Node getIcon() {
        Node previewIcon = getPreviewIcon();
        if(previewIcon ==null) {

            ImageView imageView = new ImageView(new Image(IkeaApplication.class.getResource("/styles/images/icon-overlay.png").toString()));
            imageView.setMouseTransparent(true);
            Rectangle overlayHighlight = new Rectangle(-8,-8,130,130);
            overlayHighlight.setFill(new LinearGradient(0,0.5,0,1,true, CycleMethod.NO_CYCLE, new Stop[]{ new Stop(0, Color.BLACK), new Stop(1,Color.web("#444444"))}));
            overlayHighlight.setOpacity(0.8);
            overlayHighlight.setMouseTransparent(true);
            overlayHighlight.setBlendMode(BlendMode.ADD);
            Rectangle background = new Rectangle(-8,-8,130,130);
            background.setFill(Color.web("#b9c0c5"));
            Group group = new Group(background);
            Rectangle clipRect = new Rectangle(114,114);
            clipRect.setArcWidth(38);
            clipRect.setArcHeight(38);
            group.setClip(clipRect);
            Node content = createIconContent();
            if (content != null) {
                content.setTranslateX((int)((114-content.getBoundsInParent().getWidth())/2)-(int)content.getBoundsInParent().getMinX());
                content.setTranslateY((int)((114-content.getBoundsInParent().getHeight())/2)-(int)content.getBoundsInParent().getMinY());
                group.getChildren().add(content);
            }
            group.getChildren().addAll(overlayHighlight,imageView);
            // Wrap in extra group as clip dosn't effect layout without it
            return new Group(group);
        }
        return previewIcon;
    }

}