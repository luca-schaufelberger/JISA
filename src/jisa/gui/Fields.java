package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.SRunnable;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Fields extends JFXWindow implements Element, Iterable<Field<?>> {

    public  BorderPane                          pane;
    public  GridPane                            list;
    public  ButtonBar                           buttonBar;
    private Map<Field<?>, ConfigBlock.Value<?>> links  = new HashMap<>();
    private List<Field<?>>                      fields = new LinkedList<>();
    private ConfigBlock                         config = null;
    private String                              tag    = null;
    private int                                 rows   = 0;

    /**
     * Creates a input fields group for user-input.
     *
     * @param title Title of the window/grid-element.
     */
    public Fields(String title) {
        super(title, Fields.class.getResource("fxml/InputWindow.fxml"));
        buttonBar.getButtons().addListener((InvalidationListener) change -> GUI.runNow(() -> {
            boolean show = !buttonBar.getButtons().isEmpty();
            buttonBar.setVisible(show);
            buttonBar.setManaged(show);
        }));

        Util.addShutdownHook(() -> {
            if (config != null) {
                links.forEach((field, value) -> value.set(field.get()));
                config.save();
            }
        });

    }

    @SuppressWarnings("unchecked")
    public void linkConfig(ConfigBlock config) {

        links.clear();
        this.config = config;

        int i = 0;
        for (Field<?> field : this) {

            String name = field.getText().replace(" ", "-");
            switch (field.get().getClass().getSimpleName()) {

                case "String":
                    links.put(field, config.stringValue(name));
                    ((Field<String>) field).set(((ConfigBlock.Value<String>) links.get(field)).getOrDefault(((Field<String>) field).get()));
                    break;

                case "Double":
                    links.put(field, config.doubleValue(name));
                    ((Field<Double>) field).set(((ConfigBlock.Value<Double>) links.get(field)).getOrDefault(((Field<Double>) field).get()));
                    break;

                case "Integer":
                    links.put(field, config.intValue(name));
                    ((Field<Integer>) field).set(((ConfigBlock.Value<Integer>) links.get(field)).getOrDefault(((Field<Integer>) field).get()));
                    break;

                case "Boolean":
                    links.put(field, config.booleanValue(name));
                    ((Field<Boolean>) field).set(((ConfigBlock.Value<Boolean>) links.get(field)).getOrDefault(((Field<Boolean>) field).get()));
                    break;

            }

        }

    }

    public void writeToConfig() {

        links.forEach((field, value) -> value.set(field.get()));

    }

    public void updateGridding() {

        GUI.runNow(() -> {

            int shift   = 0;
            int lastRow = 0;

            for (Node n : list.getChildren()) {

                int row = GridPane.getRowIndex(n);

                shift += Math.max(0, (row - lastRow) - 1);

                GridPane.setRowIndex(n, row - shift);

                lastRow = row;

            }

        });

    }

    /**
     * Add a simple text box to the fields group. Accepts any string.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to show in the text-box
     *
     * @return Reference object (SetGettable) to set and get the value of the text-box
     */
    public Field<String> addTextField(String name, String initialValue) {


        TextField field = new TextField(initialValue);
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<String> f = new Field<>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(String value) {

                field.setText(value);
            }

            @Override
            public String get() {

                return field.getText();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();
                field.textProperty().addListener(list);

            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public boolean isVisible() {

                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {

                return label.getText();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {

                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {

                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }


    /**
     * Add a simple text box to the fields group. Accepts any string.
     *
     * @param name Name of the field
     *
     * @return Reference object (SetGettable) to set and get the value of the text-box
     */
    public Field<String> addTextField(String name) {

        return addTextField(name, "");
    }


    /**
     * Adds a check-box to the fields group. Provides boolean user input.
     *
     * @param name         Name of the field
     * @param initialValue Initial state of the check-box
     *
     * @return Reference object to set and get the value (true or false) of the check-box
     */
    public Field<Boolean> addCheckBox(String name, boolean initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        CheckBox field = new CheckBox();
        field.setText(name);
        field.setSelected(initialValue);
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label();
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));


        Field<Boolean> f = new Field<>() {

            private ChangeListener<Boolean> list = null;

            @Override
            public void set(Boolean value) {

                field.setSelected(value);
            }

            @Override
            public Boolean get() {

                return field.isSelected();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.selectedProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.selectedProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {

                return field.isDisabled();
            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return field.getText();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    label.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> field.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a check-box to the fields group. Provides boolean user input.
     *
     * @param name Name of the field
     *
     * @return Reference object to set and get the value (true or false) of the check-box
     */
    public Field<Boolean> addCheckBox(String name) {

        return addCheckBox(name, false);
    }

    /**
     * Adds a text-box with a "browse" button for selecting a file save location.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to display
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileSave(String name, String initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field = new TextField(initialValue);
        Label     label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        Button button = new Button("Browse...");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(actionEvent -> {
            String file = GUI.saveFileSelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(field, button);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);
        inner.setSpacing(15);

        GUI.runNow(() -> list.addRow(rows++, label, inner));


        Field<String> f = new Field<>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(String value) {

                field.setText(value);
            }

            @Override
            public String get() {

                return field.getText();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, inner);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    inner.setVisible(visible);
                    inner.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    public Field<Double> addDoubleDisplay(String name, double initialValue) {

        TextField field = new TextField(String.format("%e", initialValue));
        field.setMaxWidth(Integer.MAX_VALUE);
        field.setBackground(Background.EMPTY);
        field.setBorder(Border.EMPTY);
        field.setEditable(false);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<Double> f = new Field<>() {

            private double value = initialValue;
            private ChangeListener<String> list = null;

            @Override
            public void set(Double value) {
                GUI.runNow(() -> field.setText(String.format("%e", value)));
                this.value = value;
            }

            @Override
            public Double get() {
                return value;
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                field.textProperty().removeListener(list);

                list = (observableValue, s, t1) -> new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                field.textProperty().addListener(list);

            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text-box with a "browse" button for selecting a file save location.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileSave(String name) {

        return addFileSave(name, "");
    }

    public Field<String> addDirectorySelect(String name, String initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field = new TextField(initialValue);
        Label     label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        Button button = new Button("Browse...");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(actionEvent -> {
            String file = GUI.directorySelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(field, button);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);
        inner.setSpacing(15);

        GUI.runNow(() -> list.addRow(rows++, label, inner));


        Field<String> f = new Field<>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(String value) {

                field.setText(value);
            }

            @Override
            public String get() {

                return field.getText();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, inner);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    inner.setVisible(visible);
                    inner.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    public Field<String> addDirectorySelect(String name) {

        return addDirectorySelect(name, "");
    }

    /**
     * Adds a text-box with a "browse" button for selecting a file for opening.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileOpen(String name, String initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField field = new TextField(initialValue);
        Label     label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        Button button = new Button("Browse...");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(actionEvent -> {
            String file = GUI.openFileSelect();
            if (file != null) {
                field.setText(file);
            }
        });
        field.setMaxWidth(Integer.MAX_VALUE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        HBox inner = new HBox(field, button);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);
        inner.setSpacing(15);

        GUI.runNow(() -> list.addRow(rows++, label, inner));

        Field<String> f = new Field<>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(String value) {

                field.setText(value);
            }

            @Override
            public String get() {

                return field.getText();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, inner);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {
                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    inner.setVisible(visible);
                    inner.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {
                field.setDisable(disabled);
                button.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text-box with a "browse" button for selecting a file for opening.
     *
     * @param name Name of the field
     *
     * @return SetGettable object that can set or get the selected file path as a String
     */
    public Field<String> addFileOpen(String name) {

        return addFileOpen(name, "");
    }

    /**
     * Adds a text box that only accepts integer values.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to display
     *
     * @return SetGettable to set or get the value as an integer
     */
    public Field<Integer> addIntegerField(String name, int initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        IntegerField field = new IntegerField();
        field.setText(String.valueOf(initialValue));
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<Integer> f = new Field<>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(Integer value) {

                field.setText(value.toString());
            }

            @Override
            public Integer get() {

                return field.getIntValue();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.textProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.textProperty().addListener(list);
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {

                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {

                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text box that only accepts integer values.
     *
     * @param name Name of the field
     *
     * @return SetGettable to set or get the value as an integer
     */
    public Field<Integer> addIntegerField(String name) {

        return addIntegerField(name, 0);
    }

    /**
     * Adds a text box that only accepts numerical (decimal, floating point) values.
     *
     * @param name         Name of the field
     * @param initialValue Initial value to display
     *
     * @return SetGettable to set or get the value as a double
     */
    public Field<Double> addDoubleField(String name, double initialValue) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        DoubleInput field = new DoubleInput();
        field.setValue(initialValue);
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        Field<Double> f = new Field<>() {

            private ChangeListener<String> list = null;

            @Override
            public void set(Double value) {

                field.setValue(value);
            }

            @Override
            public Double get() {

                return field.getValue();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                field.setOnChange((v) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start());
            }

            @Override
            public void editValues(String... values) {

            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {

                return field.disabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {

                field.disabled(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a text box that only accepts numerical (decimal, floating point) values.
     *
     * @param name Name of the field
     *
     * @return SetGettable to set or get the value as a double
     */
    public Field<Double> addDoubleField(String name) {

        return addDoubleField(name, 0.0);
    }

    /**
     * Adds a drop-down box with the specified choices.
     *
     * @param name         Name of the field
     * @param initialValue Index of option to initially have selected
     * @param options      Array of names for the options
     *
     * @return SetGettable to set and get the selected value, represented as an integer (0 = first option, 1 = second option etc)
     */
    public Field<Integer> addChoice(String name, int initialValue, String... options) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<String> field = new ChoiceBox<>();
        field.setMaxWidth(Integer.MAX_VALUE);
        Label label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        GridPane.setVgrow(label, Priority.NEVER);
        GridPane.setVgrow(field, Priority.NEVER);
        GridPane.setHgrow(label, Priority.NEVER);
        GridPane.setHgrow(field, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.RIGHT);

        GUI.runNow(() -> list.addRow(rows++, label, field));

        field.getItems().addAll(options);
        field.getSelectionModel().select(initialValue);

        Field<Integer> f = new Field<>() {

            private ChangeListener<Number> list = null;

            @Override
            public void set(Integer value) {

                field.getSelectionModel().select(value);
            }

            @Override
            public Integer get() {

                return field.getSelectionModel().getSelectedIndex();
            }

            @Override
            public void setOnChange(SRunnable onChange) {

                if (list != null) {
                    field.getSelectionModel().selectedIndexProperty().removeListener(list);
                }

                list = (observable, oldValue, newValue) -> (new Thread(() -> {
                    try {
                        onChange.run();
                    } catch (Exception e) {
                        Util.exceptionHandler(e);
                    }
                })).start();

                field.getSelectionModel().selectedIndexProperty().addListener(list);
            }

            @Override
            public synchronized void editValues(String... values) {

                GUI.runNow(() -> {

                    int selected = field.getSelectionModel().getSelectedIndex();

                    int min = Math.min(values.length - 1, Math.max(0, selected));
                    if (list != null) {
                        field.getSelectionModel().selectedIndexProperty().removeListener(list);
                        field.setItems(FXCollections.observableArrayList(values));
                        field.getSelectionModel().select(min);
                        field.getSelectionModel().selectedIndexProperty().addListener(list);
                    } else {
                        field.setItems(FXCollections.observableArrayList(values));
                        field.getSelectionModel().select(min);
                    }

                });
            }

            @Override
            public boolean isVisible() {
                return field.isVisible();
            }

            @Override
            public void remove() {

                GUI.runNow(() -> {
                    Fields.this.list.getChildren().removeAll(label, field);
                    updateGridding();
                });

            }

            @Override
            public String getText() {
                return label.getText();
            }

            @Override
            public boolean isDisabled() {

                return field.isDisabled();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                    field.setVisible(visible);
                    field.setManaged(visible);
                });

            }


            @Override
            public void setText(String text) {
                GUI.runNow(() -> label.setText(text));
            }


            @Override
            public void setDisabled(boolean disabled) {

                field.setDisable(disabled);
            }


        };

        fields.add(f);
        return f;

    }

    /**
     * Adds a drop-down box with the specified choices.
     *
     * @param name    Name of the field
     * @param options Array of names for the options
     *
     * @return SetGettable to set and get the selected value, represented as an integer (0 = first option, 1 = second option etc)
     */
    public Field<Integer> addChoice(String name, String... options) {

        return addChoice(name, 0, options);
    }

    @Override
    public Pane getPane() {

        return pane;
    }


    public jisa.gui.Separator addSeparator() {

        Separator separator = new Separator();
        GUI.runNow(() -> list.addRow(rows++, separator));
        GridPane.setColumnSpan(separator, 2);
        VBox.setVgrow(separator, Priority.ALWAYS);

        return new jisa.gui.Separator() {

            @Override
            public void remove() {
                GUI.runNow(() -> {
                    list.getChildren().remove(separator);
                    updateGridding();
                });
            }

            @Override
            public boolean isVisible() {
                return separator.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> separator.setVisible(visible));
            }


        };

    }

    /**
     * Add a button to the bottom of the fields group.
     *
     * @param text    Text to display in the button
     * @param onClick Action to perform when clicked
     */
    public jisa.gui.Button addButton(String text, ClickHandler onClick) {

        Button button = new Button(text);

        button.setOnAction((ae) -> (new Thread(() -> {
            try {
                onClick.click();
            } catch (Exception e) {
                Util.exceptionHandler(e);
            }
        })).start());

        GUI.runNow(() -> buttonBar.getButtons().add(button));

        return new jisa.gui.Button() {

            @Override
            public boolean isDisabled() {

                return button.isDisabled();
            }

            @Override
            public boolean isVisible() {

                return button.isVisible();
            }

            @Override
            public String getText() {

                return button.getText();
            }

            @Override
            public void setOnClick(ClickHandler onClick) {

                GUI.runNow(() -> button.setOnAction(
                        (actionEvent) -> {
                            Thread t = new Thread(() -> {
                                try {
                                    onClick.click();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            t.setDaemon(true);
                            t.start();
                        }
                ));

            }

            @Override
            public void remove() {

                GUI.runNow(() -> buttonBar.getButtons().remove(button));
            }

            @Override
            public void setDisabled(boolean disabled) {

                GUI.runNow(() -> button.setDisable(disabled));
            }


            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    button.setVisible(visible);
                    button.setManaged(visible);
                });
            }


            @Override
            public void setText(String text) {

                GUI.runNow(() -> button.setText(text));
            }


        };

    }

    public void setFieldsDisabled(boolean flag) {

        for (Field<?> f : fields) {
            f.setDisabled(flag);
        }

    }

    /**
     * Shows the fields as its own window, with an "OK" and "Cancel" button. Does not return until the window has been
     * closed either by clicking "OK" or "Cancel" or closing the window. Returns a boolean indicating whether "OK" was
     * clicked or not.
     *
     * @return Was "OK" clicked?
     */
    public boolean showAndWait() {

        final Semaphore     semaphore = new Semaphore(0);
        final AtomicBoolean result    = new AtomicBoolean(false);

        Button okay   = new Button("OK");
        Button cancel = new Button("Cancel");

        okay.setOnAction(ae -> {
            result.set(true);
            semaphore.release();
        });

        cancel.setOnAction(ae -> {
            result.set(false);
            semaphore.release();
        });

        GUI.runNow(() -> buttonBar.getButtons().addAll(cancel, okay));

        stage.setOnCloseRequest(we -> {
            result.set(false);
            semaphore.release();
        });

        show();

        try {
            semaphore.acquire();
        } catch (Exception ignored) {
        }

        close();

        GUI.runNow(() -> buttonBar.getButtons().removeAll(cancel, okay));

        return result.get();

    }

    @Override
    public Iterator<Field<?>> iterator() {

        return fields.iterator();
    }

}
