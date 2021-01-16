package solar.app;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import solar.model.Calculator;

/**
 * Encapsulates a slider and a label to choose a month. The slider runs 1-12 and
 * the label adjusts in tandem. Listen to the monthProperty to get changes.
 *
 * @author rocky
 */
public class FxMonthControl extends HBox {

    private final Slider slider = new Slider(1, 12, 1);
    private final Label label = new Label("  Jan");
    // I think 99 ensure that it triggers on startup
    private final IntegerProperty monthProperty = new SimpleIntegerProperty(99);

    public FxMonthControl() {
        label.setPrefWidth(50);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setPrefSize(400, 20);
        slider.setSnapToTicks(true);
        slider.setTooltip(new Tooltip("Month 1-12"));
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                monthProperty.setValue(Math.round(new_val.floatValue()));
            }
        });
        monthProperty.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                label.setText("  " + getMonthName());
            }
        });
        this.getChildren().add(slider);
        this.getChildren().add(label);
    }

    /**
     * Month
     *
     * @see getMonthName()
     * @return Selected month, 0-11
     */
    public int getMonth() {
        return monthProperty.get() - 1;
    }

    public ReadOnlyIntegerProperty getMonthProperty() {
        return monthProperty;
    }

    /**
     * Month name as three letter abbreviation.
     *
     * @see getMonth()
     * @return Selected month, 0-11
     */
    public String getMonthName() {
        return Calculator.monthName(getMonth());
    }
}
