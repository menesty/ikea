package org.menesty.ikea.ui.table;

import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import org.menesty.ikea.ui.controls.form.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Menesty
 * Date: 11/13/13
 * Time: 7:16 PM
 */
public abstract class AbstractEditableTableCell<S, T, Field extends TextField> extends TableCell<S, T> {
  protected Field textField;

  public AbstractEditableTableCell() {
  }

  /**
   * Any action attempting to commit an edit should call this method rather than commit the edit directly itself. This
   * method will perform any validation and conversion required on the value. For text values that normally means this
   * method just commits the edit but for numeric values, for example, it may first parse the given input. <p> The only
   * situation that needs to be treated specially is when the field is losing focus. If you user hits enter to commit the
   * cell with bad data we can happily cancel the commit and force them to enter a real value. If they click away from the
   * cell though we want to give them their old value back.
   *
   * @param losingFocus true if the reason for the call was because the field is losing focus.
   */
  protected abstract void commitHelper(boolean losingFocus);

  /**
   * Provides the string representation of the value of this cell when the cell is not being edited.
   */
  protected abstract String getString();

  @Override
  public void startEdit() {
    super.startEdit();
    setGraphic(getField());
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    Platform.runLater(() -> {
      textField.getField().requestFocus();
      textField.getField().selectAll();

    });
  }

  protected Field getField() {
    if (textField == null) {
      textField = createTextField();
      textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
      textField.getField().setOnKeyPressed(t -> {
        if (t.getCode() == KeyCode.ENTER) {
          commitHelper(false);
        } else if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        } else if (t.getCode() == KeyCode.TAB) {
          commitHelper(false);

          TableColumn nextColumn = getNextColumn(!t.isShiftDown());
          if (nextColumn != null) {
            getTableView().edit(getTableRow().getIndex(), nextColumn);
          }
        }
      });
      textField.getField().focusedProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue && textField != null) {
          commitHelper(true);
        }
      });
    }

    return textField;
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    setText(getString());
    setContentDisplay(ContentDisplay.TEXT_ONLY);
    textField = null;
  }

  @Override
  public void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      if (isEditing()) {
        if (textField != null) {
          textField.setText(getString());
        }
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      } else {
        setText(getString());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
      }
    }
  }

  abstract Field createTextField();
    /*{
        textField = new TextField(getString());

    }*/

  /**
   * @param forward true gets the column to the right, false the column to the left of the current column
   * @return
   */
  private TableColumn<S, ?> getNextColumn(boolean forward) {
    List<TableColumn<S, ?>> columns = new ArrayList<>();
    for (TableColumn<S, ?> column : getTableView().getColumns()) {
      columns.addAll(getLeaves(column));
    }
    //There is no other column that supports editing.
    if (columns.size() < 2) {
      return null;
    }
    int currentIndex = columns.indexOf(getTableColumn());
    int nextIndex = currentIndex;
    if (forward) {
      nextIndex++;
      if (nextIndex > columns.size() - 1) {
        nextIndex = 0;
      }
    } else {
      nextIndex--;
      if (nextIndex < 0) {
        nextIndex = columns.size() - 1;
      }
    }
    return columns.get(nextIndex);
  }

  private List<TableColumn<S, ?>> getLeaves(TableColumn<S, ?> root) {
    List<TableColumn<S, ?>> columns = new ArrayList<>();
    if (root.getColumns().isEmpty()) {
      //We only want the leaves that are editable.
      if (root.isEditable()) {
        columns.add(root);
      }
      return columns;
    } else {
      for (TableColumn<S, ?> column : root.getColumns()) {
        columns.addAll(getLeaves(column));
      }
      return columns;
    }
  }
}