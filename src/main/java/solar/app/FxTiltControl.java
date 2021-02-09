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
 * Returns integers 0-90.
 *
 * @design JavaFX doesn't seem to have an integer slider, so encapsulate one and
 * attach it to an integer property.
 *
 * @author rocky
 */
public class FxTiltControl extends VBox {

    private final Slider slider = new Slider(0, 90, 1);
    private final IntegerProperty intProperty = new SimpleIntegerProperty();
    private final Label label = new Label();

    public FxTiltControl(int initVal) {
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(10);
        slider.setShowTickLabels(true);
        slider.setPrefSize(400, 20);
        slider.setSnapToTicks(true);
        slider.setTooltip(new Tooltip("Tilt, degrees"));

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                int val = Math.round(new_val.floatValue());
//                if (!slider.isValueChanging()) {
                    intProperty.setValue(val);
  //              }
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
        label.setText("Tilt " + intProperty.getValue().intValue() + " degrees");
    }

    public int getValue() {
        return intProperty.get();
    }

    public ReadOnlyIntegerProperty getProperty() {
        return intProperty;
    }
}
