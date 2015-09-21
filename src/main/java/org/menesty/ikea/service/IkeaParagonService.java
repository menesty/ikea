package org.menesty.ikea.service;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.menesty.ikea.domain.IkeaParagon;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Menesty on 5/14/14.
 */
public class IkeaParagonService extends Repository<IkeaParagon> {
    private static final Logger logger = Logger.getLogger(IkeaParagonService.class.getName());

    public List<IkeaParagon> load(int offset, int limit) {
        return load(offset, limit, new OrderBy("createdDate", OrderBy.Direction.desc));
    }

    public IkeaParagon findByName(String name) {
        boolean started = isActive();

        if (!started)
            begin();

        IkeaParagon result;

        TypedQuery<IkeaParagon> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.name = ?1", entityClass);
        query.setParameter(1, name);
        query.setMaxResults(1);

        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            result = null;
        }
        if (!started)
            commit();

        return result;
    }

    public void setUploaded(Date date, String name) {
        boolean started = isActive();

        if (!started)
            begin();

        Query query = getEm().createQuery("update " + entityClass.getName() + " entity set entity.uploaded = true where entity.name = ?1 and entity.createdDate = ?2");
        query.setParameter(1, name);
        query.setParameter(2, date);

        query.executeUpdate();

        if (!started)
            commit();

    }

    public void exportToXls(String path) {
        Context bean = new Context();
        bean.putVar("items", load());

        if (!path.endsWith(".xlsx") && !path.endsWith(".xls"))
            path = path.concat(".xlsx");


        try (InputStream in = getClass().getResourceAsStream("/config/ikea-paragons.xlsx")) {
            try (OutputStream os = Files.newOutputStream(FileSystems.getDefault().getPath(path), StandardOpenOption.CREATE_NEW)) {
                JxlsHelper.getInstance().processTemplate(in, os, bean);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exportToXls", e);
        }
    }
}
