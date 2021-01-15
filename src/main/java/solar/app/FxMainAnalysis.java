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

public class FxMainAnalysis extends Application {

    public static final Insets INSETS = new Insets(20, 20, 20, 20);
    public static int SPACING = 20;

    public FxMainAnalysis() {
        super();
    }

    @Override
    public void init() {
    }

    @Override
    public void start(Stage stage) {

        TabPane tabPane = new TabPane();
        FxImportTab importTab = new FxImportTab();
        FxSummaryTab summary = new FxSummaryTab();
        FxAnalysisDailyTab power = new FxAnalysisDailyTab();
        FxAnalysisTab energy = new FxAnalysisTab(summary);
        FxBatteryTab battery = new FxBatteryTab();

        importTab.addListener(power);
        importTab.addListener(energy);
        importTab.addListener(battery);

        tabPane.getTabs().add(tab("Introduction", new FxIntroTab(), "User guide"));
        tabPane.getTabs().add(tab("Source", importTab, "Reload data"));
        tabPane.getTabs().add(tab("Power", power, "Logged data throughout the day averaged over a month"));
        tabPane.getTabs().add(tab("Energy", energy, "Logged data throughout the year"));
        tabPane.getTabs().add(tab("Battery", battery, "Variation in battery usage during the year"));
        tabPane.getTabs().add(tab("Summary", summary, "Tabulated monthly results"));
        //tabPane.getTabs().add(tab("Inverters (M)", new FxInverterTab(), "Variation of predicted maximum (full sun) power output during the day"));

        VBox vBox = new VBox(tabPane);
        vBox.setPrefSize(1500, 1000);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Solar PV Analysis Tool");

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
