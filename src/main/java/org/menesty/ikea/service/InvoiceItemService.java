package org.menesty.ikea.service;

import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.processor.invoice.InvoiceItem;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * y
 * Created by Menesty on 12/22/13.
 */
public class InvoiceItemService extends Repository<InvoiceItem> {

    public List<InvoiceItem> load(InvoicePdf invoicePdf) {
        boolean started = isActive();
        try {
            if (!started)
                begin();
            TypedQuery<InvoiceItem> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.invoicePdf.id = ?1", entityClass);
            query.setParameter(1, invoicePdf.getId());
            return query.getResultList();
        } finally {
            if (!started)
                commit();
        }
    }

    public boolean hasItems(InvoicePdf invoicePdf) {
        boolean result;
        begin();
        TypedQuery<Long> query = getEm().createQuery("select count(entity.id) from " + entityClass.getName() + " entity where  entity.invoicePdf.id = ?1", Long.class);
        query.setParameter(1, invoicePdf.getId());
        result = query.getSingleResult() > 0;
        commit();
        return result;
    }

    public void deleteBy(InvoicePdf currentInvoicePdf) {
        begin();
        remove(load(currentInvoicePdf));
        commit();
        /*try {
            remove(load(currentInvoicePdf));
            begin();
            Query query = getEm().createQuery("delete from " + entityClass.getName() + " entity where entity.invoicePdf=?1");
            query.setParameter(1, currentInvoicePdf);
            query.executeUpdate();
        } catch (Exception e) {
            rollback();
        } finally {
            commit();
        }*/
    }
}
