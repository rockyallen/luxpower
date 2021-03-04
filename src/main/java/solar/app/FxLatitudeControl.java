package solar.app;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * Returns integers +/- 90.
 *
 * @design JavaFX doesn't seem to have an integer slider, so encapsulate one and
 * attach it to an integer property.
 *
 *
 * @author rocky
 */
public class FxLatitudeControl extends VBox {

    private final Slider slider = new Slider(-90, 90, 1);
    private final IntegerProperty intProperty = new SimpleIntegerProperty();
    private final Label label = new Label();

    public FxLatitudeControl(int initVal) {
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(10);
        slider.setShowTickLabels(true);
        slider.setPrefSize(400, 20);
        slider.setSnapToTicks(true);
        slider.setTooltip(new Tooltip("Latitude, degrees"));

        // Get the listeners right: If you want the label to update continuously when the slider is moved, 
        // but the intProperty not to change until you release the slider, put the setLabel() call in the slider listener.
        // If you aren't filtering on slider.isChanging, put it in the int listener.
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                //   if (!slider.isValueChanging()) {
                int val = Math.round(new_val.floatValue());
                intProperty.setValue(val);
                // }
            }
        });

        intProperty.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                setLabel();
            }
        });

        getChildren().add(label);
        getChildren().add(slider);

        slider.adjustValue(initVal);
        intProperty.setValue(initVal);
        setLabel();
    }

    private void setLabel() {
        label.setText("Latitude " + intProperty.getValue().intValue() + " degrees");
    }

    public int getValue() {
        return intProperty.get();
    }

    public ReadOnlyIntegerProperty getProperty() {
        return intProperty;
    }
}
