package org.menesty.ikea.service;

import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.processor.invoice.InvoiceItem;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * y
 * Created by Menesty on 12/22/13.
 */
public class InvoiceItemService extends Repository<InvoiceItem> {

    public List<InvoiceItem> load(InvoicePdf invoicePdf) {
        try {
            begin();
            TypedQuery<InvoiceItem> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.invoicePdf.id = ?1", entityClass);
            query.setParameter(1, invoicePdf.getId());
            return query.getResultList();
        } finally {
            commit();
        }
    }

    public void deleteBy(InvoicePdf currentInvoicePdf) {
        try {
            begin();
            Query query = getEm().createQuery("delete from " + entityClass.getName() + " entity where entity.invoicePdf.id=?1");
            query.setParameter(1, currentInvoicePdf.getId());
            query.executeUpdate();
        } finally {
            commit();
        }
    }
}
