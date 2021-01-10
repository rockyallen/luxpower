package solar.app;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import solar.model.DataSource;
import solar.model.Importer;
import solar.model.Record;

/**
 *
 * @author rocky
 */
public class FxImportTab extends BorderPane {

    private final DataSource ds;
    private static final String DS = "datasource";

    Button reload = new Button("Import");
    Button cache = new Button("Cache");
    Button analyse = new Button("Analyse");
    TextArea t = new TextArea("");
    CustomOutputStream os = new CustomOutputStream(t);
    Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    String source = "";

    public FxImportTab(DataSource ds) {
        this.ds = ds;

        cache.setOnAction(value -> {
            loadCache();
        });
        reload.setOnAction(value -> {
            importXls();
        });
        analyse.setOnAction(value -> {
            ds.announceChanged();
            analyse.setDisable(true);
        });
        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        analyse.setDisable(true);
        p.getChildren().addAll(reload, cache, analyse);
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

    private void importXls() {

        source = prefs.get(DS, "/home/");
        File from = new File(source);

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(from);

        from = directoryChooser.showDialog(null);
        if (from != null) {
            cache.setDisable(true);
            reload.setDisable(true);
            analyse.setDisable(true);
            prefs.put(DS, from.getAbsolutePath());

            os.println("Loading from XLS...");
            final File from2 = from;
            // put this in a SwingWorker
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new Task<Integer>() {
                void message(String s) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            os.println(s);
                        }
                    });
                }

                @Override
                protected Integer call() {

                    try {
                        ds.getRecords().clear();
                        new Importer(ds.getRecords(), os).importFolder(from2);
                        message("Done import");

                        boolean result = ds.serialize();
                    } catch (Exception ex) {
                        Logger.getLogger("").log(Level.SEVERE, null, ex);
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            cache.setDisable(false);
                            reload.setDisable(false);
                            analyse.setDisable(false);
                            // ds.announceChanged();
                        }
                    });
                    return 0;
                }
            });
        } else {

            os.println("Import cancelled");
        }
    }

    private void loadCache() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Task<Integer>() {
            @Override
            protected Integer call() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        os.println("Loading from cache " + ds.f.getAbsolutePath() + "...");
                        cache.setDisable(true);
                        reload.setDisable(true);
                        analyse.setDisable(true);
                    }
                });
                if (ds.deserialize()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            os.println("Loaded records = " + ds.getRecords().size());
//                            ds.announceChanged();
                        }
                    });
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            os.println("Deserialisation failed" + ds.getRecords().size());
                        }
                    });
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        os.println("Loaded from cache");
                        cache.setDisable(false);
                        reload.setDisable(false);
                        analyse.setDisable(false);
                    }
                });
                return 0;
            }
        });
    }
}
