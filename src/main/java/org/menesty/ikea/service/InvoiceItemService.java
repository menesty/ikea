package org.menesty.ikea.service;

import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.processor.invoice.InvoiceItem;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * y
 * Created by Menesty on 12/22/13.
 */
public class InvoiceItemService extends Repository<InvoiceItem> {

    public List<InvoiceItem> loadInvoicePdf(int id) {
        boolean started = isActive();

        try {
            if (!started)
                begin();
            TypedQuery<InvoiceItem> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.invoicePdf.id = ?1", entityClass);
            query.setParameter(1, id);
            return query.getResultList();

        } finally {
            if (!started)
                commit();
        }
    }

    public List<InvoiceItem> loadBy(InvoicePdf invoicePdf) {
        return loadInvoicePdf(invoicePdf.getId());
    }

    public boolean hasItems(InvoicePdf invoicePdf) {
        boolean started = isActive();

        if (!started)
            begin();

        TypedQuery<Long> query = getEm().createQuery("select count(entity.id) from " + entityClass.getName() + " entity where  entity.invoicePdf.id = ?1", Long.class);
        query.setParameter(1, invoicePdf.getId());
        boolean result = query.getSingleResult() > 0;

        if (!started)
            commit();

        return result;
    }

    public void deleteBy(InvoicePdf currentInvoicePdf) {
        boolean started = isActive();
        if (!started)
            begin();
        remove(loadBy(currentInvoicePdf));
        if (!started)
            commit();
        /*try {
            remove(loadBy(currentInvoicePdf));
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

    public List<InvoiceItem> loadBy(CustomerOrder order) {
        try {
            begin();
            TypedQuery<InvoiceItem> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity left join entity.invoicePdf  invoicePdf where invoicePdf.customerOrder.id=?1", entityClass);
            query.setParameter(1, order.getId());
            return query.getResultList();
        } finally {
            commit();
        }
    }
}
