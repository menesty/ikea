package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import com.google.common.base.Preconditions;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.lib.service.IkeaProductService;
import org.menesty.ikea.lib.service.parse.pdf.invoice.InvoiceParseResult;
import org.menesty.ikea.lib.service.parse.pdf.invoice.InvoicePdfParserService;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.pages.wizard.order.step.component.ItemProcessingInfoLabel;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Menesty on
 * 10/5/15.
 * 22:10.
 */
class LoadInvoiceParseService extends AbstractAsyncService<Void> {
  interface OnParseListener {
    void onParse(InvoiceParseResult parseResult);
  }

  private ObjectProperty<List<File>> filesProperty = new SimpleObjectProperty<>();
  private final OnParseListener onParseListener;
  private final ItemProcessingInfoLabel progressLabel;

  public LoadInvoiceParseService(OnParseListener onParseListener, ItemProcessingInfoLabel progressLabel) {
    Preconditions.checkNotNull(onParseListener);
    this.onParseListener = onParseListener;
    this.progressLabel = progressLabel;
  }

  @Override
  protected Task<Void> createTask() {
    List<File> _files = filesProperty.get();
    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        final InvoicePdfParserService parserService = ServiceFacade.getInvoicePdfParserService();
        final IkeaProductService ikeaProductService = ServiceFacade.getIkeaProductService();

        AtomicInteger counter = new AtomicInteger();
        Platform.runLater(() -> {
              progressLabel.showProgress();
              progressLabel.setTotal(_files.size());
            }
        );

        _files.stream().forEach(file -> {
          try {
            int currentIndex = counter.incrementAndGet();
            Platform.runLater(() -> progressLabel.setInfo(currentIndex, file.getName()));
            final InvoiceParseResult result = parserService.parse(file);

            result.getRawInvoiceItems().stream().forEach(rawItem -> {
              try {
                IkeaProduct product = ikeaProductService.getProduct(rawItem.getArtNumber(),
                    throwable -> ServiceFacade.getErrorConsole().add(throwable));
                rawItem.setProduct(product);
              } catch (Exception e) {
                ServiceFacade.getErrorConsole().add(e);
              }
            });

            Platform.runLater(() -> onParseListener.onParse(result));
          } catch (Exception e) {
            ServiceFacade.getErrorConsole().add(e);
          }
        });

        Platform.runLater(progressLabel::hideProgress);
        return null;
      }
    };
  }

  public void setFiles(List<File> files) {
    filesProperty.set(files);
  }
}