package org.menesty.ikea.ui.pages.wizard.order.step.service;

import org.menesty.ikea.lib.domain.ikea.IkeaPackageInfo;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.ikea.IkeaProductPart;
import org.menesty.ikea.lib.dto.ikea.JsonPkgInfo;
import org.menesty.ikea.lib.dto.ikea.JsonPkgInfoArr;
import org.menesty.ikea.lib.dto.ikea.JsonProduct;
import org.menesty.ikea.lib.dto.ikea.JsonProductItem;
import org.menesty.ikea.lib.util.DownloadProductTask;
import org.menesty.ikea.service.ServiceFacade;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/10/15.
 * 18:03.
 */
public class IkeaProductService {
    public IkeaProduct getProduct(String artNumber, boolean deep) throws Exception {
        DownloadProductTask downloadProductTask = new DownloadProductTask(artNumber, 3000);
        JsonProduct jsonProduct = downloadProductTask.call();
        JsonProductItem productItem = jsonProduct.getActiveProductItem();

        IkeaProduct ikeaProduct = new IkeaProduct();

        ikeaProduct.setArtNumber(productItem.getPartNumber());
        ikeaProduct.setPrice(productItem.getPrice());
        ikeaProduct.setShortName(productItem.getType());

        if (!productItem.isCombo()) {
            List<JsonPkgInfo> jsonPkgInfos = productItem.getActiveJsonPkgInfo();
            List<IkeaPackageInfo> ikeaPackageInfos = jsonPkgInfos.stream().map(this::transform).collect(Collectors.toList());

            ikeaProduct.setIkeaPackageInfos(ikeaPackageInfos);
        } else {
            List<JsonPkgInfoArr> jsonPkgInfoArrs = productItem.getPkgInfoArr();

            List<IkeaProductPart> productParts = jsonPkgInfoArrs.stream().map(jsonPkgInfoArr -> {
                IkeaProductPart productPart = new IkeaProductPart();

                Optional<JsonPkgInfo> firstJsonPkgInfo = jsonPkgInfoArr.getPkgInfo().stream().filter(jsonPkgInfo -> jsonPkgInfo.getQuantity() != 0).findFirst();

                if (firstJsonPkgInfo.isPresent()) {
                    productPart.setCount(firstJsonPkgInfo.get().getQuantity());
                }
                //try get IkeaProduct
                IkeaProduct product = null;
                if (deep) {
                    try {
                        product = getProduct(jsonPkgInfoArr.getArticleNumber(), false);
                    } catch (Exception e) {
                        //create product from exist information
                        ServiceFacade.getErrorConsole().add(e);
                    }
                }

                if (product == null) {
                    product = new IkeaProduct();
                    product.setArtNumber(jsonPkgInfoArr.getArticleNumber());
                    List<IkeaPackageInfo> ikeaPackageInfos = jsonPkgInfoArr.getPkgInfo().stream().map(this::transform).collect(Collectors.toList());
                    product.setIkeaPackageInfos(ikeaPackageInfos);
                }
                productPart.setProduct(product);

                return productPart;
            }).collect(Collectors.toList());

            ikeaProduct.setIkeaProductParts(productParts);
        }

        return ikeaProduct;
    }


    private IkeaPackageInfo transform(JsonPkgInfo jsonPkgInfo) {
        IkeaPackageInfo packageInfo = new IkeaPackageInfo();

        packageInfo.setBoxCount(jsonPkgInfo.getQuantity());
        packageInfo.setBoxNumber(jsonPkgInfo.getConsumerPackNo());
        packageInfo.setHeight(jsonPkgInfo.getHeight());
        packageInfo.setLength(jsonPkgInfo.getLength());
        packageInfo.setWeight(jsonPkgInfo.getWeigh());
        packageInfo.setWidth(jsonPkgInfo.getWidth());

        return packageInfo;
    }
}
