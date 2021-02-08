package solar.app;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import solar.model.Calculator;
import solar.model.DataStoreXls;
import solar.model.Changeable;
import solar.model.Components;
import solar.model.DataStoreCacheReader;
import solar.model.DataStoreCacheWriter;
import solar.model.Listener;
import solar.model.DataStoreModel;
import solar.model.Record;

/**
 * Pull in data from logs, or run a model.
 *
 * @author rocky
 */
public class FxImportTab extends BorderPane implements Changeable {

    private final Button components = new Button("Reload");
    private final Button reload = new Button("Import");
    private final Button model = new Button("Run");
    private final TextArea t = new TextArea("");
    private final RadioButton noWeather = new RadioButton("No weather");
    private final RadioButton rawWeather = new RadioButton("Raw weather");
    private final RadioButton smoothedWeather = new RadioButton("Smoothed weather");
    private final ToggleGroup group1 = new ToggleGroup();
    private final CheckBox fudgeBox = new CheckBox("Estimate PV3");
    private final Set<Listener> listeners = new HashSet<>();
    private final ComboBox battery = new ComboBox();
    private final ComboBox inverter = new ComboBox();
    private final ComboBox pv1 = new ComboBox();
    private final ComboBox pv2 = new ComboBox();
    private final ComboBox pv3 = new ComboBox();
    private final ComboBox cost = new ComboBox();

    private final Components componentsList = new Components();
    private Collection<Record> records;

    @Override
    public void addListener(Listener ll) {
        listeners.add(ll);
    }

    public void announceChanged() {
        t.appendText("Updating graphs...\n");
        for (Listener ll : listeners) {
            t.appendText(ll.toString() + "...\n");
            ll.changed(records, componentsList);
        }
        t.appendText("Done\n");
    }

    public FxImportTab() {

        model.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                loadModel();
            }
        });

        reload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                loadLogs();
            }
        });

        components.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                loadComponents();
            }
        });
        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);

        GridPane g = new GridPane();
        g.setHgap(5);
        g.setVgap(5);
        g.add(new Label("PV1"), 0, 0, 1, 1);
        g.add(pv1, 1, 0, 1, 1);
        g.add(new Label("PV2"), 0, 1, 1, 1);
        g.add(pv2, 1, 1, 1, 1);
        g.add(new Label("PV3"), 0, 2, 1, 1);
        g.add(pv3, 1, 2, 1, 1);
        g.add(new Label("Inverter"), 0, 3, 1, 1);
        g.add(inverter, 1, 3, 1, 1);
        g.add(new Label("Battery"), 0, 4, 1, 1);
        g.add(battery, 1, 4, 1, 1);
        g.add(new Label("Cost"), 0, 5, 1, 1);
        g.add(cost, 1, 5, 1, 1);

        g.add(components, 1, 6, 1, 1);

        TitledPane tpane = new TitledPane("System", g);
        tpane.setCollapsible(false);
        p.getChildren().add(tpane);

        VBox vb = new VBox();
        vb.setPadding(FxMainAnalysis.INSETS);
        vb.setSpacing(FxMainAnalysis.SPACING);
        vb.getChildren().addAll(fudgeBox, reload);
        tpane = new TitledPane("Recorded data", vb);
        tpane.setCollapsible(false);
        p.getChildren().add(tpane);

        vb = new VBox();
        vb.setPadding(FxMainAnalysis.INSETS);
        vb.setSpacing(FxMainAnalysis.SPACING);
        noWeather.setSelected(true);
        group1.getToggles().addAll(noWeather, rawWeather, smoothedWeather);
        vb.getChildren().addAll(noWeather, rawWeather, smoothedWeather, model);
        tpane = new TitledPane("Modelled data", vb);
        tpane.setCollapsible(false);
        p.getChildren().add(tpane);
        setTop(p);

        ScrollPane sp = new ScrollPane();
        sp.setHbarPolicy(ScrollBarPolicy.NEVER);
        VBox box = new VBox();
        sp.setContent(box);
        t.setPrefSize(10000, 10000);
        box.getChildren().add(t);
        VBox.setVgrow(sp, Priority.ALWAYS);
        box.setPadding(FxMainAnalysis.INSETS);

        setCenter(box);

        loadCache();
        loadComponents();
    }

    private void loadCache() {
        DataStoreCacheReader dsc = new DataStoreCacheReader();
        dsc.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent ev) {
                t.appendText("Loaded successfully " + dsc.toString() + "\n");
                records = (Collection<Record>) dsc.getValue();
                t.appendText("Number of records=" + records.size() + "\n");
                announceChanged();
            }
        });
        dsc.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent ev) {
                t.appendText("Failed to load cache " + dsc.toString() + "\n");
            }
        });
        dsc.messageProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal,
                    Object newVal) {
                t.appendText(newVal.toString() + "\n");
            }
        });

        Executors.newSingleThreadExecutor().submit(dsc);
    }

    private void loadModel() {
        DataStoreModel dsm = new DataStoreModel();
        if (smoothedWeather.isSelected())
        dsm.setWeather(Calculator.Weather.SMOOTHEDWEATHER);
        else if (rawWeather.isSelected())
        dsm.setWeather(Calculator.Weather.RAWWEATHER);

        String sel = (String) pv1.getValue();
        if (sel != null) {
            componentsList.setPv1(componentsList.getArrays().get(sel));
        }
        sel = (String) pv2.getValue();
        if (sel != null) {
            componentsList.setPv2(componentsList.getArrays().get(sel));
        }
        sel = (String) pv3.getValue();
        if (sel != null) {
            componentsList.setPv3(componentsList.getArrays().get(sel));
        }
        sel = (String) inverter.getValue();
        if (sel != null) {
            componentsList.setInv12(componentsList.getInverters().get(sel));
        }
        sel = (String) battery.getValue();
        if (sel != null) {
            componentsList.setBattery(componentsList.getBatteries().get(sel));
        }
        sel = (String) cost.getValue();
        if (sel != null) {
            componentsList.setCost(componentsList.getCosts().get(sel));
        }

        dsm.setComponents(componentsList);

        dsm.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent ev) {
                t.appendText("Modelled successfully " + dsm.toString() + "\n");
                records = (Collection<Record>) dsm.getValue();
                t.appendText("Number of records=" + records.size() + "\n");
                announceChanged();
            }
        });
        dsm.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent ev) {
                t.appendText("Model FAILED " + dsm.toString() + "\n");
            }
        });
        dsm.messageProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal,
                    Object newVal) {
                t.appendText(newVal.toString() + "\n");
            }
        });
        Executors.newSingleThreadExecutor().submit(dsm);
    }

    private void loadLogs() {
        DataStoreXls dsx = new DataStoreXls();
        dsx.setFudge(fudgeBox.isSelected());
        dsx.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent ev) {
                t.appendText("Loaded successfully " + dsx.toString() + "\n");
                records = (Collection<Record>) dsx.getValue();
                t.appendText("Number of records=" + records.size() + "\n");
                //                    analyse.setDisable(false);

                t.appendText("Caching records...\n");
                DataStoreCacheWriter dsw = new DataStoreCacheWriter();
                dsw.setData(records);
                Executors.newSingleThreadExecutor().submit(dsw);
                announceChanged();
            }
        });
        dsx.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent ev) {
                t.appendText("Failed " + dsx.toString() + "\n");
            }
        });
        dsx.messageProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal,
                    Object newVal) {
                t.appendText(newVal.toString() + "\n");
            }
        });

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(dsx.getFolder());

        File from = directoryChooser.showDialog(null);
        if (from != null) {
            dsx.setFolder(from);
            Executors.newSingleThreadExecutor().submit(dsx);
        }
    }

    private void loadComponents() {
        t.appendText("Loading components.xls\n");
        if (componentsList.load(new File("components.xls"))) {
            t.appendText("Loaded successfully\n");
            t.appendText(componentsList.toString());
            inverter.getItems().clear();
            inverter.getItems().addAll(componentsList.getInverters().keySet());
            battery.getItems().clear();
            battery.getItems().addAll(componentsList.getBatteries().keySet());
            pv1.getItems().clear();
            pv1.getItems().addAll(componentsList.getArrays().keySet());
            pv2.getItems().clear();
            pv2.getItems().addAll(componentsList.getArrays().keySet());
            pv3.getItems().clear();
            pv3.getItems().addAll(componentsList.getArrays().keySet());
            cost.getItems().clear();
            cost.getItems().addAll(componentsList.getCosts().keySet());
        } else {
            t.appendText("Loading FAILED. File moved? Format wrong?\n");
        }
    }

    private Node make(String pV1, ComboBox pv1) {
        HBox p = new HBox();
        p.getChildren().add(new Label(pV1));
        p.getChildren().add(pv1);
        return p;
    }
}
