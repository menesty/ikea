package org.menesty.ikea.factory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by Menesty
 * on 12/21/13.
 */
public class ImageFactory {
    private static final String ICON_PATH = "/styles/images/icon/";

    public static ImageView createAdd48Icon() {
        return new ImageView(new Image(ICON_PATH + "add1-48x48.png"));
    }

    public static ImageView creteEdit48Icon() {
        return new ImageView(new Image("/styles/images/icon/edit-48x48.png"));
    }

    public static ImageView createOrders72Icon() {
        return new ImageView(new Image(ICON_PATH + "orders-72x72.png"));
    }

    public static ImageView createPlus32Icon() {
        return new ImageView(new Image(ICON_PATH + "plus-32x32.png"));
    }

    public static ImageView createMinus32Icon() {
        return new ImageView(new Image(ICON_PATH + "minus-32x32.png"));
    }

    public static ImageView createBalance32Icon() {
        return new ImageView(new Image(ICON_PATH + "balance-32x32.png"));
    }

    public static ImageView createReload32Icon() {
        return new ImageView(new Image(ICON_PATH + "refresh-table-32x32.png"));
    }

    public static ImageView createSave32Icon() {
        return new ImageView(new Image(ICON_PATH + "save-32x32.png"));
    }

    public static ImageView createEppExport32Icon() {
        return new ImageView(new Image(ICON_PATH + "epp-32x32.png"));
    }

    public static ImageView createXlsExport32Icon() {
        return new ImageView(new Image(ICON_PATH + "xls-32x32.png"));
    }

    public static ImageView createDelete48Icon() {
        return new ImageView(new Image(ICON_PATH + "delete-48x48.png"));
    }

    public static ImageView createSync32Icon() {
        return new ImageView(new Image(ICON_PATH + "sync-32x32.png"));
    }

    public static ImageView createDelete32Icon() {
        return new ImageView(new Image(ICON_PATH + "delete-32x32.png"));
    }

    public static ImageView createPdf32Icon() {
        return new ImageView(new Image(ICON_PATH + "pdf-32x32.png"));
    }

    public static ImageView createXls32Icon() {
        return new ImageView(new Image(ICON_PATH + "xls-32x32.png"));
    }

    public static ImageView createWeb22Icon() {
        return new ImageView(new Image(ICON_PATH + "web-22x22.png"));
    }

    public static ImageView createAdd32Icon() {
        return new ImageView(new Image(ICON_PATH + "add-32x32.png"));
    }

    public static ImageView createWeb16Icon() {
        return new ImageView(new Image(ICON_PATH + "web-16x16.png"));
    }

    public static ImageView createEdit32Icon() {
        return new ImageView(new Image(ICON_PATH + "edit-32x32.png"));
    }

    public static ImageView createFetch16Icon() {
        return new ImageView(new Image(ICON_PATH + "refresh-16x16.png"));
    }

    public static ImageView createUsersIcon64() {
        return new ImageView(new Image(ICON_PATH + "users-64x64.png"));
    }

    public static ImageView createWarehouseIcon72() {
        return new ImageView(new Image(ICON_PATH + "warehouse-72x72.png"));
    }

    public static ImageView createDownload16Icon() {
        return new ImageView(new Image(ICON_PATH + "download-16x16.png"));
    }

    public static ImageView creteInfo48Icon() {
        return new ImageView(new Image(ICON_PATH + "info-48x48.png"));
    }

    public static ImageView createSetting22Icon() {
        return new ImageView(new Image("/styles/images/settings.png"));
    }

    public static ImageView createInvoice72Icon() {
        return new ImageView(new Image(ICON_PATH + "invoice-72x72.png"));
    }

    public static ImageView createUpload16Icon() {
        return new ImageView(new Image(ICON_PATH + "upload-16x16.png"));
    }

    public static ImageView createEmailSend16Icon() {
        return createImage(ICON_PATH + "email-send-16x16.png");
    }

    public static ImageView createMoney32Icon() {
        return createImage(ICON_PATH + "money-32x32.png");
    }

    public static ImageView createPrevious32Icon() {
        return createImage(ICON_PATH + "previous-32x32.png");
    }

    public static ImageView createIkea72Icon() {
        return new ImageView(new Image(ICON_PATH + "ikea-72x72.png"));
    }

    public static ImageView createClear32Icon() {
        return new ImageView(new Image(ICON_PATH + "clear-32x32.png"));
    }

    public static ImageView createProducts72Icon() {
        return new ImageView(new Image(ICON_PATH + "products-72x72.png"));
    }

    public static ImageView createCsv32Icon() {
        return new ImageView(new Image(ICON_PATH + "csv-32x32.png"));
    }

    public static ImageView createImport32Icon() {
        return new ImageView(new Image(ICON_PATH + "import-32x32.png"));
    }

    public static ImageView createShopIcon72() {
        return createImage(ICON_PATH + "shop-72x72.png");
    }

    public static ImageView createAdd16Icon() {
        return createImage(ICON_PATH + "add-16x16.png");
    }

    public static ImageView createDelete16Icon() {
        return createImage(ICON_PATH + "delete-16x16.png");
    }

    public static ImageView createHome24Icon() {
        return createImage(ICON_PATH + "home-20x20.png");
    }

    public static ImageView createInfo22Icon() {
        return createImage("/styles/images/info-22x22.png");
    }

    public static ImageView createSearch72Icon() {
        return createImage(ICON_PATH + "search-72x72.png");
    }

    public static ImageView createSiteOrders72Icon() {
        return createImage(ICON_PATH + "site-order-72x72.png");
    }

    public static ImageView createWizard48Icon() {
        return createImage(ICON_PATH + "wizard-48x48.png");
    }

    public static ImageView createError32Icon() {
        return createImage(ICON_PATH + "error-32x32.png");
    }

    public static ImageView createError22Icon() {
        return createImage(ICON_PATH + "error-22x22.png");
    }

    private static ImageView createImage(String path) {
        return new ImageView(new Image(path));
    }
}
