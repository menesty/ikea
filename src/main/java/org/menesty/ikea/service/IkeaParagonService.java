package org.menesty.ikea.service;

import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.menesty.ikea.domain.IkeaParagon;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        XLSTransformer transformer = new XLSTransformer();

        Map<String, Object> bean = new HashMap<>();
        bean.put("items", load());

        if (!path.endsWith(".xlsx") && !path.endsWith(".xls"))
            path = path.concat(".xlsx");

        try {
            Workbook workbook = transformer.transformXLS(getClass().getResourceAsStream("/config/ikea-paragons.xlsx"), bean);

            Path file = FileSystems.getDefault().getPath(path);
            StandardOpenOption operation = StandardOpenOption.CREATE_NEW;

            if (file.toFile().exists())
                operation = StandardOpenOption.TRUNCATE_EXISTING;

            workbook.write(Files.newOutputStream(file, operation));

        } catch (InvalidFormatException | IOException e) {
            logger.log(Level.SEVERE, "exportToXls", e);
        }
    }
}
