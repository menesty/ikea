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
    return createIcon("add1-48x48.png");
  }

  public static ImageView creteEdit48Icon() {
    return new ImageView(new Image("/styles/images/icon/edit-48x48.png"));
  }

  public static ImageView createOrders72Icon() {
    return createIcon("orders-72x72.png");
  }

  public static ImageView createPlus32Icon() {
    return createIcon("plus-32x32.png");
  }

  public static ImageView createMinus32Icon() {
    return createIcon("minus-32x32.png");
  }

  public static ImageView createBalance32Icon() {
    return createIcon("balance-32x32.png");
  }

  public static ImageView createReload32Icon() {
    return createIcon("refresh-table-32x32.png");
  }

  public static ImageView createSave32Icon() {
    return createIcon("save-32x32.png");
  }

  public static ImageView createEppExport32Icon() {
    return createIcon("epp-32x32.png");
  }

  public static ImageView createXlsExport32Icon() {
    return createIcon("xls-32x32.png");
  }

  public static ImageView createDelete48Icon() {
    return createIcon("delete-48x48.png");
  }

  public static ImageView createSync32Icon() {
    return createIcon("sync-32x32.png");
  }

  public static ImageView createDelete32Icon() {
    return createIcon("delete-32x32.png");
  }

  public static ImageView createPdf32Icon() {
    return createIcon("pdf-32x32.png");
  }

  public static ImageView createXls32Icon() {
    return createIcon("xls-32x32.png");
  }

  public static ImageView createWeb22Icon() {
    return createIcon("web-22x22.png");
  }

  public static ImageView createAdd32Icon() {
    return createIcon("add-32x32.png");
  }

  public static ImageView createWeb16Icon() {
    return createIcon("web-16x16.png");
  }

  public static ImageView createEdit32Icon() {
    return createIcon("edit-32x32.png");
  }

  public static ImageView createFetch16Icon() {
    return createIcon("refresh-16x16.png");
  }

  public static ImageView createUsersIcon64() {
    return createIcon("users-64x64.png");
  }

  public static ImageView createWarehouseIcon72() {
    return createIcon("warehouse-72x72.png");
  }

  public static ImageView createDownload16Icon() {
    return createIcon("download-16x16.png");
  }

  public static ImageView creteInfo48Icon() {
    return createIcon("info-48x48.png");
  }

  public static ImageView createSetting22Icon() {
    return createImage("/styles/images/settings.png");
  }

  public static ImageView createInvoice72Icon() {
    return createIcon("invoice-72x72.png");
  }

  public static ImageView createUpload16Icon() {
    return createIcon("upload-16x16.png");
  }

  public static ImageView createEmailSend16Icon() {
    return createIcon("email-send-16x16.png");
  }

  public static ImageView createMoney32Icon() {
    return createIcon("money-32x32.png");
  }

  public static ImageView createPrevious32Icon() {
    return createIcon("previous-32x32.png");
  }

  public static ImageView createIkea72Icon() {
    return createIcon("ikea-72x72.png");
  }

  public static ImageView createClear32Icon() {
    return createIcon("clear-32x32.png");
  }

  public static ImageView createProducts72Icon() {
    return createIcon("products-72x72.png");
  }

  public static ImageView createCsv32Icon() {
    return createIcon("csv-32x32.png");
  }

  public static ImageView createImport32Icon() {
    return createIcon("import-32x32.png");
  }

  public static ImageView createShopIcon72() {
    return createIcon("shop-72x72.png");
  }

  public static ImageView createAdd16Icon() {
    return createIcon("add-16x16.png");
  }

  public static ImageView createDelete16Icon() {
    return createIcon("delete-16x16.png");
  }

  public static ImageView createHome24Icon() {
    return createIcon("home-20x20.png");
  }

  public static ImageView createInfo22Icon() {
    return createImage("/styles/images/info-22x22.png");
  }

  public static ImageView createInfo16Icon() {
    return createImage("/styles/images/info-16x16.png");
  }

  public static ImageView createSearch72Icon() {
    return createIcon("search-72x72.png");
  }

  public static ImageView createSiteOrders72Icon() {
    return createIcon("site-order-72x72.png");
  }

  public static ImageView createWizard48Icon() {
    return createIcon("wizard-48x48.png");
  }

  public static ImageView creatReport48Icon() {
    return createIcon("report-48x48.png");
  }

  public static ImageView createError32Icon() {
    return createIcon("error-32x32.png");
  }

  public static ImageView createError22Icon() {
    return createIcon("error-22x22.png");
  }

  private static ImageView createImage(String path) {
    return new ImageView(new Image(path));
  }

  private static ImageView createIcon(String iconName) {
    return createImage(ICON_PATH + iconName);
  }

  public static ImageView createReload16Icon() {
    return createIcon("reset-16x16.png");
  }

  public static ImageView createClear16Icon() {
    return createIcon("clear-16x16.png");
  }

  public static ImageView createWarning16Icon() {
    return createIcon("warning-16x16.png");
  }

  public static ImageView createIkea32Icon() {
    return createIcon("ikea-32x32.png");
  }

  public static ImageView createWarehouse32Icon() {
    return createIcon("warehouse-32x32.png");
  }

  public static ImageView createEdit16Icon() {
    return createIcon("edit-16x16.png");
  }

  public static ImageView createPaste32Icon() {
    return createIcon("paste-32x32.png");
  }

  public static ImageView createIkeaSmallIcon() {
    return createIcon("ikea-small.png");
  }

  public static ImageView createResumptionIcon72() {
    return createIcon("resumption-72x72.png");
  }

  public static ImageView createLogIcon72() {
    return createIcon("log-72x72.png");
  }

  public static ImageView createCopyIcon32() {
    return createIcon("copy-32x32.png");
  }

  public static ImageView createCopyIcon316() {
    return createIcon("copy-16x16.png");
  }
}
