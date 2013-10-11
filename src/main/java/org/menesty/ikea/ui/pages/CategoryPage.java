package org.menesty.ikea.ui.pages;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Menesty
 * Date: 10/10/13
 * Time: 7:58 AM
 */
public class CategoryPage extends BasePage {

    private List<BasePage> pages;

    public  List<BasePage> getSubPages(){
        return pages;
    }

    public CategoryPage(String name, BasePage... pages) {
        super(name);
        this.pages = new ArrayList<>();
        for(BasePage page: pages)
        addPage(page);
    }


    @Override
    public Node createView() {
        VBox main = new VBox(8) {
            // stretch to allways fill height of scrollpane
            @Override
            protected double computePrefHeight(double width) {
                return Math.max(
                        super.computePrefHeight(width),
                        getParent().getBoundsInLocal().getHeight()
                );
            }
        };
        main.getStyleClass().add("category-page");
       /* // create header
        Label header = new Label(getName());
        header.setMaxWidth(Double.MAX_VALUE);
        header.setMinHeight(Control.USE_PREF_SIZE); // Workaround for RT-14251
        header.getStyleClass().add("page-header");
        main.getChildren().add(header);*/


        Label categoryHeader = new Label(getName());
        categoryHeader.setMaxWidth(Double.MAX_VALUE);
        categoryHeader.setMinHeight(Control.USE_PREF_SIZE);
        categoryHeader.getStyleClass().add("category-header");
        main.getChildren().add(categoryHeader);


        TilePane directChildFlow = new TilePane(8, 8);
        directChildFlow.setPrefColumns(1);
        directChildFlow.getStyleClass().add("category-page-flow");
        // add sub sections
        for (BasePage page : pages) {
            directChildFlow.getChildren().add(page.createTile());

        }
        main.getChildren().add(directChildFlow);


        // wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("noborder-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(main);
        return scrollPane;
    }

    public void addPage(BasePage basePage) {
        basePage.setBreadCrumbPath(getBreadCrumb());
        pages.add(basePage);

    }

}