package org.menesty.ikea.ui.pages;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.menesty.ikea.domain.StorageComboLack;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Menesty on
 * 7/3/15.
 * 17:54.
 */
public class SiteOrderPage extends BasePage {

    private SiteOrderAsyncService siteOrderAsyncService;


    public SiteOrderPage() {
        super(Pages.SITE_ORDERS.getTitle());
    }

    @Override
    protected void initialize() {
        siteOrderAsyncService = new SiteOrderAsyncService();
        siteOrderAsyncService.setOnSucceededListener(value -> {

        });
    }

    @Override
    protected Node createView() {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createToolBar());

        return wrap(borderPane);
    }

    private VBox createToolBar() {
        VBox controlBox = new VBox();

        ToolBar toolBar = new ToolBar();

        Button createSiteOrder = new Button(null, ImageFactory.createAdd32Icon());
        createSiteOrder.setTooltip(new Tooltip(I18n.UA.getString(I18nKeys.CREATE_SITE_ORDER)));
        createSiteOrder.setOnAction(eh -> {

        });

        toolBar.getItems().add(createSiteOrder);

        controlBox.getChildren().add(toolBar);

        return controlBox;
    }

    @Override
    public void onActive(Object... params) {
        siteOrderAsyncService.restart();
    }

    class SiteOrderAsyncService extends AbstractAsyncService<List<StorageComboLack>> {

        @Override
        protected Task<List<StorageComboLack>> createTask() {
            return new Task<List<StorageComboLack>>() {
                @Override
                protected List<StorageComboLack> call() throws Exception {
                    Callable<String> callable = HttpServiceUtil.get("/site-order");
                    callable.call();

                    return null;
                }
            };
        }
    }
}
