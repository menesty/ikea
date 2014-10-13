package org.menesty.ikea.core.component.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.menesty.ikea.core.component.CategoryGroup;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.core.component.PageDescription;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumb;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumbItem;
import org.menesty.ikea.core.component.factory.PageFactory;
import org.menesty.ikea.ui.pages.BasePage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 10/11/14.
 * 10:59.
 */
public class WorkspaceArea extends Pane {
    private final ScrollPane categoryWorkspace;
    private CategoryPane.PageClickListener pageClickListener;
    private List<CategoryGroup> categoryGroups = new ArrayList<>();
    private DialogSupport dialogSupport;

    private BreadCrumb breadCrumb;

    private ActivePageHolder activePageHolder = new ActivePageHolder();


    public WorkspaceArea(BreadCrumb breadCrumb, DialogSupport dialogSupport) {
        this.breadCrumb = breadCrumb;
        this.dialogSupport = dialogSupport;
        setId("page-area");
        getStyleClass().add("noborder-scroll-pane");

        pageClickListener = new CategoryPane.PageClickListener() {
            @Override
            public void onPageClick(PageDescription pageDescription) {
                navigate(pageDescription);
            }
        };

        categoryWorkspace = new ScrollPane();
        categoryWorkspace.getStyleClass().add("noborder-scroll-pane");
        categoryWorkspace.setFitToWidth(true);

        VBox categoryPane = new VBox();
        categoryPane.setFillWidth(true);
        categoryPane.getStyleClass().addAll("app", "category-page");

        categoryWorkspace.setContent(categoryPane);

        showCategories();

    }

    public void addCategory(CategoryGroup categoryGroup) {
        categoryGroups.add(categoryGroup);
        ((VBox) categoryWorkspace.getContent()).getChildren().add(new CategoryPane(categoryGroup, pageClickListener));
    }

    public List<CategoryGroup> getCategories() {
        return Collections.unmodifiableList(categoryGroups);
    }

    @Override
    protected void layoutChildren() {
        for (Node child : getChildren()) {
            child.resizeRelocate(0, 0, getWidth(), getHeight());

            if (child.equals(categoryWorkspace)) {
                ((VBox) ((ScrollPane) child).getContent()).setPrefHeight(getHeight());
            }
        }
    }

    public void showCategories() {
        getChildren().clear();
        getChildren().setAll(categoryWorkspace);

        activePageHolder.deactivate();
    }

    public void navigate(final BreadCrumbItem item) {
        if (item.getPageDescription() == null) {
            showCategories();
            breadCrumb.setItems(new ArrayList<BreadCrumbItem>());
            return;
        }

        navigate(item.getPageDescription());
    }

    private BasePage navigate(Class<? extends BasePage> pageClass, Object... params) {
        activePageHolder.deactivate();

        BasePage page = PageFactory.createPage(pageClass);
        page.setDialogSupport(dialogSupport);

        getChildren().clear();
        getChildren().setAll(page.getView());

        page.onActive(params);

        return page;
    }

    public void navigate(final PageDescription pageDescription, Object... params) {
        activePageHolder.update(pageDescription, navigate(pageDescription.getPageClass(), params));
        updateBreadCrumb(pageDescription);
    }

    private void updateBreadCrumb(PageDescription pageDescription) {
        for (CategoryGroup categoryGroup : categoryGroups) {
            List<BreadCrumbItem> items = categoryGroup.getBreadCrumbChain(pageDescription);

            if (items != null) {
                breadCrumb.setItems(items);
                return;
            }
        }
    }

    public void navigate(PageDescription parent, Class<? extends BasePage> subPage, Object... params) {
        for (CategoryGroup categoryGroup : categoryGroups) {
            PageDescription pageDescription = categoryGroup.getPageDescription(parent, subPage);
            if (pageDescription != null) {
                navigate(pageDescription, params);
                return;
            }
        }
    }

    public PageDescription getActivePage() {
        return activePageHolder.getPageDescription();
    }
}

class ActivePageHolder {
    private PageDescription pageDescription;

    private BasePage page;

    public void update(PageDescription pageDescription, BasePage page) {
        this.page = page;
        this.pageDescription = pageDescription;
    }

    public void deactivate() {
        if (page != null) {
            page.onDeactivate();
            page.setDialogSupport(null);
            page = null;
            pageDescription = null;
        }
    }

    public PageDescription getPageDescription() {
        return pageDescription;
    }
}

class CategoryPane extends VBox {

    public interface PageClickListener {
        void onPageClick(PageDescription pageDescription);
    }

    private PageClickListener pageClickListener;

    public CategoryPane(CategoryGroup categoryGroup, PageClickListener pageClickListener) {
        this.pageClickListener = pageClickListener;

        Label categoryHeader = new Label(categoryGroup.getName());

        categoryHeader.setMaxWidth(Double.MAX_VALUE);
        categoryHeader.setMinHeight(Control.USE_PREF_SIZE);
        categoryHeader.getStyleClass().add("category-header");

        getChildren().add(categoryHeader);

        TilePane directChildFlow = new TilePane(8, 8);
        directChildFlow.setPrefColumns(1);
        directChildFlow.getStyleClass().add("category-page-flow");
        // add sub sections
        for (PageDescription pageDescription : categoryGroup.getItems()) {
            directChildFlow.getChildren().add(createTile(pageDescription));
        }

        getChildren().add(directChildFlow);

    }


    public Node createTile(final PageDescription pageDescription) {
        Button tile = new Button(pageDescription.getName().trim(), getIcon(pageDescription.getIcon()));

        tile.setMinSize(140, 145);
        tile.setPrefSize(140, 145);
        tile.setMaxSize(140, 145);
        tile.setContentDisplay(ContentDisplay.TOP);
        tile.getStyleClass().clear();
        tile.getStyleClass().add("sample-tile");

        tile.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (pageClickListener != null)
                    pageClickListener.onPageClick(pageDescription);
            }
        });

        return tile;

    }

    private Node getIcon(Node content) {
        ImageView imageView = new ImageView(new Image(WorkspaceArea.class.getResource("/styles/images/icon-overlay.png").toString()));
        imageView.setMouseTransparent(true);

        Rectangle overlayHighlight = new Rectangle(-8, -8, 130, 130);
        overlayHighlight.setFill(new LinearGradient(0, 0.5, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.BLACK), new Stop(1, Color.web("#444444"))));
        overlayHighlight.setOpacity(0.8);
        overlayHighlight.setMouseTransparent(true);
        overlayHighlight.setBlendMode(BlendMode.ADD);

        Rectangle background = new Rectangle(-8, -8, 130, 130);
        background.setFill(Color.web("#b9c0c5"));

        Group group = new Group(background);

        Rectangle clipRect = new Rectangle(114, 114);
        clipRect.setArcWidth(38);
        clipRect.setArcHeight(38);

        group.setClip(clipRect);

        if (content != null) {
            content.setTranslateX((int) ((114 - content.getBoundsInParent().getWidth()) / 2) - (int) content.getBoundsInParent().getMinX());
            content.setTranslateY((int) ((114 - content.getBoundsInParent().getHeight()) / 2) - (int) content.getBoundsInParent().getMinY());
            group.getChildren().add(content);
        }
        group.getChildren().addAll(overlayHighlight, imageView);
        return new Group(group);
    }
}



