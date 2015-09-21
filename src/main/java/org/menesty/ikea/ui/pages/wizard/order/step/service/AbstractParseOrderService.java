package org.menesty.ikea.ui.pages.wizard.order.step.service;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.parser.ErrorMessage;
import org.menesty.ikea.service.parser.ParseResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 9/9/15.
 * 10:01.
 */
public abstract class AbstractParseOrderService extends AbstractAsyncService<List<ParseResult>> {
    protected ObjectProperty<List<File>> fileObjectProperty = new SimpleObjectProperty<>();
    protected StringProperty fileNameProperty = new SimpleStringProperty();
    protected IntegerProperty fileIndexProperty = new SimpleIntegerProperty();

    @Override
    protected Task<List<ParseResult>> createTask() {
        return new Task<List<ParseResult>>() {
            final List<File> _files = new ArrayList<>(fileObjectProperty.get());

            @Override
            protected List<ParseResult> call() throws Exception {
                List<ParseResult> parseResults = new ArrayList<>();

                _files.stream().forEach(file -> {
                    Platform.runLater(() -> {
                        fileNameProperty.set(file.getName());
                        fileIndexProperty.set(fileIndexProperty.get() + 1);
                    });

                    ParseResult parseResult = null;

                    try {
                        parseResult = parse(file);
                    } catch (FileNotFoundException e) {
                        parseResult = new ParseResult();
                        parseResult.setParseWarnings(Collections.singletonList(new ErrorMessage(I18n.UA.getString(I18nKeys.FILE_NOT_FOUND_EXCEPTION), "")));
                    } finally {
                        if (parseResult != null) {
                            parseResult.setFileName(file.getName());
                            parseResults.add(parseResult);
                        }
                    }
                });

                return parseResults;
            }
        };
    }

    protected abstract ParseResult parse(File file) throws FileNotFoundException;

    public void setFiles(List<File> file) {
        fileObjectProperty.set(file);
    }

    public StringProperty fileNameProperty() {
        return fileNameProperty;
    }

    public IntegerProperty fileIndexProperty() {
        return fileIndexProperty;
    }
}