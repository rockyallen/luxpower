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
 * Returns positive odd integers suitable for driving a tophat filter.
 *
 * @design JavaFX doesn't seem to have an integer slider, so encapsulate one and
 * attach it to an integer property.
 *
 * @author rocky
 */
public class FxSmoothingControl extends VBox {

    private final Slider slider = new Slider(1, 31, 1);
    private final IntegerProperty intProperty = new SimpleIntegerProperty();
    private final Label label = new Label();

    public FxSmoothingControl(int initVal) {
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(2);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setPrefSize(400, 20);
        slider.setSnapToTicks(true);
        slider.setTooltip(new Tooltip("Moving average. The number is the width of the window in days."));

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    int odd = ((int) (new_val.floatValue()) / 2) * 2 + 1;
                    setLabel(odd);
                if (!slider.isValueChanging()) {
                    intProperty.setValue(odd);
              }
            }
        });

//        intProperty.addListener(new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
//                    setLabel(intProperty.getValue().intValue());
//            }
//
//        });

        getChildren().add(label);
        getChildren().add(slider);

        slider.adjustValue(initVal);
        intProperty.setValue(initVal);
        setLabel(initVal);
    }

    private void setLabel(int val) {
        label.setText("Smoothing " + val + " days");
    }

    public int getValue() {
        return intProperty.get();
    }

    public ReadOnlyIntegerProperty getProperty() {
        return intProperty;
    }
}
