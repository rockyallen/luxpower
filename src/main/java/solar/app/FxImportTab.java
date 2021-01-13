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
import solar.model.DataStoreCache;
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

    private final Button reload = new Button("Import");
    private final Button cache = new Button("Cache");
    private final Button analyse = new Button("Analyse");
    private final Button model = new Button("Model");
    private final TextArea t = new TextArea("");
    private final CheckBox weatherBox = new CheckBox("Include weather (model only)");
    private final CheckBox fudgeBox = new CheckBox("Estimate pv3 (Import only)");
    private Set<Listener> listeners = new HashSet<>();
    private Collection<Record> records;

    @Override
    public void addListener(Listener ll) {
        listeners.add(ll);
    }

    public void announceChanged() {
        for (Listener ll : listeners) {
            ll.changed(records, "");
        }
    }

    public FxImportTab() {

        model.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                DataStoreModel dsc = new DataStoreModel<Collection<Record>>();
            dsc.setWeather(weatherBox.isSelected());
                dsc.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent ev) {
                        t.appendText("Loaded successfully " + dsc.toString() + "\n");
                        records = (Collection<Record>) dsc.getValue();
                        t.appendText("Number of records=" + records.size() + "\n");
                        analyse.setDisable(false);
                    }
                });
                dsc.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent ev) {
                        t.appendText("Failed " + dsc.toString() + "\n");
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
        });

        cache.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                DataStoreCacheReader dsc = new DataStoreCacheReader<Collection<Record>>();
                dsc.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent ev) {
                        t.appendText("Loaded successfully " + dsc.toString() + "\n");
                        records = (Collection<Record>) dsc.getValue();
                        t.appendText("Number of records=" + records.size() + "\n");
                        
                        analyse.setDisable(false);
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
        });

        reload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                DataStoreXls dsx = new DataStoreXls<Collection<Record>>();
                dsx.setFudge(fudgeBox.isSelected());
                dsx.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent ev) {
                        t.appendText("Loaded successfully " + dsx.toString() + "\n");
                        records = (Collection<Record>) dsx.getValue();
                        t.appendText("Number of records=" + records.size() + "\n");
                        analyse.setDisable(false);

                        t.appendText("Caching records");
                        DataStoreCacheWriter dsw = new DataStoreCacheWriter();
                        dsw.setData(records);
                        Executors.newSingleThreadExecutor().submit(dsw);                        
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
        });

        analyse.setOnAction(value -> {
            announceChanged();
            analyse.setDisable(true);
        });

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        analyse.setDisable(true);
        p.getChildren().addAll(reload, fudgeBox, cache, model, weatherBox, analyse);
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
    }
}
