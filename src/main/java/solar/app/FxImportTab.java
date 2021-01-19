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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import solar.model.DataStoreXls;
import solar.model.Changeable;
import solar.model.Components;
import solar.model.DataStoreCacheReader;
import solar.model.DataStoreCacheWriter;
import solar.model.Listener;
import solar.model.DataStoreModel;
import solar.model.Record;

/**
 *
 * @author rocky
 */
public class FxImportTab extends BorderPane implements Changeable {

    //private final Button components = new Button("Components");
    private final Button reload = new Button("Import");
    private final Button model = new Button("Go");
    private final TextArea t = new TextArea("");
    private final CheckBox weatherBox = new CheckBox("Weather");
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
            ll.changed(records, "");
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

//        components.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent value) {
//                loadComponents();
//            }
//        });

        VBox vb = new VBox();
        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Data:"), reload, fudgeBox);
        vb.getChildren().add(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Model:"), weatherBox, // components,
                new Label("PV1"), pv1,
                new Label("PV2"), pv2,
                new Label("PV3"), pv3,
                new Label("Inverter"), inverter,
                new Label("Battery"), battery,
                new Label("Cost"), cost,
                model);
        vb.getChildren().add(p);
        setTop(vb);

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
                //                      analyse.setDisable(false);
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
        dsm.setWeather(weatherBox.isSelected());

        String sel = (String) pv1.getValue();
        if (sel != null) {
            dsm.setPv1(componentsList.getArrays().get(sel));
        }
        sel = (String) pv2.getValue();
        if (sel != null) {
            dsm.setPv2(componentsList.getArrays().get(sel));
        }
        sel = (String) pv3.getValue();
        if (sel != null) {
            dsm.setPv3(componentsList.getArrays().get(sel));
        }
        sel = (String) inverter.getValue();
        if (sel != null) {
            dsm.setInv12(componentsList.getInverters().get(sel));
        }
        sel = (String) battery.getValue();
        if (sel != null) {
            dsm.setBattery(componentsList.getBatteries().get(sel));
        }
        sel = (String) cost.getValue();
        if (sel != null) {
            //dsc.setBattery(componentsList.getBatteries().get(sel));
        }
//      pv1.valueProperty().addListener(new ChangeListener<String>() {
//            @Override public void changed(ObservableValue ov, String t, String t1) {                
//                ppv1;                
//            }    
//        });                
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
}
