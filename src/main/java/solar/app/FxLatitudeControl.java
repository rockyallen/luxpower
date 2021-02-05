package solar.app;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Returns integers +/- 90.
 *
 * @design JavaFX doesn't seem to have an integer slider, so encapsulate one and
 * attach it to an integer property.
 *
 * @author rocky
 */
public class FxLatitudeControl extends HBox {

    private final Slider slider = new Slider(-90, 90, 1);
    private final IntegerProperty intProperty = new SimpleIntegerProperty();

    public FxLatitudeControl() {
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setPrefSize(400, 20);
        slider.setSnapToTicks(true);
        slider.setTooltip(new Tooltip("Latitude, degrees"));
        intProperty.setValue(1);

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (!slider.isValueChanging()) {
                    intProperty.setValue(Math.round(new_val.floatValue()));
                }
            }
        });

        this.getChildren().add(slider);
    }

    public int getLatitudeValue() {
        return intProperty.get();
    }

    public ReadOnlyIntegerProperty getLatitudeProperty() {
        return intProperty;
    }
}
