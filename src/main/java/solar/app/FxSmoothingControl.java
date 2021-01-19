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

/**
 * Returns positive odd integers only
 *
 * @author rocky
 */
public class FxSmoothingControl extends HBox {

    private final Slider slider = new Slider(1, 31, 1);
    //private final Label label = new Label("  1");
    private final IntegerProperty monthProperty = new SimpleIntegerProperty();

    public FxSmoothingControl() {
  //      label.setPrefWidth(50);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(2);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setPrefSize(400, 20);
        slider.setSnapToTicks(true);
        slider.setTooltip(new Tooltip("Moving average. The number is the width of the window in days."));
        monthProperty.setValue(1);

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (!slider.isValueChanging()) {
                    int odd = ((int) (new_val.floatValue()) / 2) * 2 + 1;
                    monthProperty.setValue(odd);
                }
            }
        });

//        monthProperty.addListener(new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
//                label.setText("  " + monthProperty.getValue());
//            }
//        });
        this.getChildren().add(slider);
//        this.getChildren().add(label);
    }

    public int getSmoothingValue() {
        return monthProperty.get();
    }

    public ReadOnlyIntegerProperty getSmoothingProperty() {
        return monthProperty;
    }
}
