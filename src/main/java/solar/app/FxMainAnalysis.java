package solar.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import solar.model.DataSource;

public class FxMainAnalysis extends Application {

    public static final Insets INSETS = new Insets(20, 20, 20, 20);
    public static int SPACING = 20;

    public FxMainAnalysis() {
        super();
        ds = new DataSource();
    }
    final DataSource ds;

    @Override
    public void start(Stage stage) {

        TabPane tabPane = new TabPane();

        FxSummaryTab summary = new FxSummaryTab(ds);
        tabPane.getTabs().add(tab("Introduction", new FxIntroTab(), "User guide"));
        tabPane.getTabs().add(tab("Import", new FxImportTab(ds), "Reload data"));
        tabPane.getTabs().add(tab("Power (A)", new FxAnalysisDailyTab(ds), "Logged data throughout the day averaged over a month"));
        tabPane.getTabs().add(tab("Energy (A)", new FxAnalysisTab(ds, summary), "Logged data throughout the year"));
        tabPane.getTabs().add(tab("Battery (A)", new FxBatteryTab(ds), "Variation in battery usage during the year"));
        tabPane.getTabs().add(tab("Summary (A)", summary, "Tabulated monthly results"));
        tabPane.getTabs().add(tab("Inverters (M)", new FxInverterTab(), "Variation of predicted maximum (full sun) power output during the day"));
        tabPane.getTabs().add(tab("Power (M)", new FxDailyModelTab(), "Variation of predicted maximum (full sun) energy output during the year"));
        tabPane.getTabs().add(tab("Enery (M)", new FxAnnualModelTab(), "Inverter efficiency, 2 parameter model"));

        VBox vBox = new VBox(tabPane);
        vBox.setPrefSize(1500, 1000);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Solar PV analysis tool");

        stage.show();
    }

    private Tab tab(String name, Pane p, String toolTip) {
        Tab tab = new Tab(name, p);
        tab.setClosable(false);
        tab.setTooltip(new Tooltip(toolTip));
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
