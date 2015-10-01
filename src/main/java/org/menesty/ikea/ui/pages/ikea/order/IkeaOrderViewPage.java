package org.menesty.ikea.ui.pages.ikea.order;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.order.IkeaOrderDetail;
import org.menesty.ikea.lib.domain.order.IkeaProcessOrder;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.ikea.order.component.RawOrderViewComponent;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

/**
 * Created by Menesty on
 * 9/29/15.
 * 10:22.
 */
public class IkeaOrderViewPage extends BasePage {

    private RawOrderViewComponent rawOrderViewComponent;
    private LoadService loadService;
    private TabPane tabPane;

    @Override
    protected void initialize() {
        loadService = new LoadService();
        loadService.setOnSucceededListener(value -> {
            rawOrderViewComponent.setData(value.getIkeaClientOrderItemDtos());
            Tab first = tabPane.getTabs().get(0);
            first.setText(first.getText() + " : " + value.getName());
        });
    }

    @Override
    protected Node createView() {
        tabPane = new TabPane();

        {
            Tab tab = new Tab(I18n.UA.getString(I18nKeys.CLIENTS_ORDER));
            tab.setClosable(false);
            tab.setContent(rawOrderViewComponent = new RawOrderViewComponent());
            tabPane.getTabs().add(tab);
        }

        return wrap(tabPane);
    }

    @Override
    public void onActive(Object... params) {
        loadingPane.bindTask(loadService);
        loadService.setProcessOrderId(this.<IkeaProcessOrder>cast(params[0]).getId());
        loadService.restart();
    }

    class LoadService extends AbstractAsyncService<IkeaOrderDetail> {
        private LongProperty processOrderIdProperty = new SimpleLongProperty();

        @Override
        protected Task<IkeaOrderDetail> createTask() {
            final Long _id = processOrderIdProperty.get();
            return new Task<IkeaOrderDetail>() {
                @Override
                protected IkeaOrderDetail call() throws Exception {
                    APIRequest apiRequest = HttpServiceUtil.get("/ikea-order-detail/" + _id);

                    return apiRequest.getData(IkeaOrderDetail.class);
                }
            };
        }

        public void setProcessOrderId(Long id) {
            processOrderIdProperty.set(id);
        }
    }
}


