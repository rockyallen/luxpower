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

/**
 * Graphical main.
 *
 * @author rocky
 */
public class FxMainAnalysis extends Application {

    // to make it easier to keep common style
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

        FxImportTab importTab = new FxImportTab();
        FxSummaryTab summary = new FxSummaryTab();
        FxAnalysisDailyTab power = new FxAnalysisDailyTab();
        FxAnalysisTab energy = new FxAnalysisTab(summary);
        SolarTab solar = new SolarTab();
        SolarFactorsTab solarFactors = new SolarFactorsTab();
        Insolation solarIrradiance = new Insolation();
DailyInsolation dailyInsolation = new DailyInsolation();

        importTab.addListener(power);
        importTab.addListener(energy);

        TabPane tabPane = new TabPane();
        makeTab(tabPane, "User guide", new FxIntroTab(), "User guide");
        makeTab(tabPane, "Source", importTab, "Reload data");
        makeTab(tabPane, "Power", power, "Power variation during the day, averaged over each month");
        makeTab(tabPane, "Energy", energy, "Energy variation during the year");
        makeTab(tabPane, "Summary", summary, "Tabulated monthly results");
        makeTab(tabPane, "Sun position", solar, "Solar position by month");
        makeTab(tabPane, "Attenuation", solarFactors, "Solar irradiance reduction factors");
        makeTab(tabPane, "Irradiance daily", solarIrradiance, "Solar irradiance");
        makeTab(tabPane, "Irradiance hourly", dailyInsolation, "Solar irradiance daily");

        VBox vBox = new VBox(tabPane);
        vBox.setPrefSize(1500, 1000);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Solar PV Analysis Tool");

        stage.show();
    }

    /**
     * Common configuration for all tabs, and add to tabPane,
     *
     * @param name Tab title
     * @param pane Pane to add to
     * @param toolTip Tooltip to set
     * @return
     */
    private Tab makeTab(TabPane tabPane, String name, Pane pane, String toolTip) {
        Tab tab = new Tab(name, pane);
        tab.setClosable(false);
        tab.setTooltip(new Tooltip(toolTip));
        tabPane.getTabs().add(tab);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
