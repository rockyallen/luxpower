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
import solar.model.Calculator;

/**
 * Encapsulates a slider and a label to choose a month.
 *
 * The slider runs 1-12 and the label adjusts in tandem. Listen to the
 * ValueProperty to get changes.
 *
 * @author rocky
 */
public class FxMonthControl extends VBox {

    private final Slider slider = new Slider(1, 12, 1);
    private final Label label = new Label("                  ");
    private final IntegerProperty valueProperty = new SimpleIntegerProperty();

    public FxMonthControl(int initVal) {
        //label.setPrefWidth(200);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setPrefSize(400, 20);
        slider.setSnapToTicks(true);
        slider.setTooltip(new Tooltip("Month 1-12"));

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                double val = Math.round(new_val.floatValue());
                if (val < 1) {
                    throw new IllegalStateException("" + val);
                }
                valueProperty.setValue(val);
            }
        });
        valueProperty.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
            setLabel();
            }
        });

        getChildren().add(label);
        getChildren().add(slider);

        slider.adjustValue(initVal);
        valueProperty.setValue(initVal);
        setLabel();
    }

    private void setLabel() {
       label.setText("Month " + getMonthName());
    }
    
    /**
     * Month
     *
     * @see getMonthName()
     * @return Selected month, 0-11
     */
    public int getValue() {
        return valueProperty.get() - 1;
    }

    /**
     * For debugging only
     * 
     * @return 
     */
    public String getLabelText() {
        return label.getText();
    }

    public ReadOnlyIntegerProperty getProperty() {
        return valueProperty;
    }

    /**
     * Month name as three letter abbreviation.
     *
     * @see #getValue()
     * @return Selected month, 0-11
     */
    public String getMonthName() {
        return Calculator.monthName(getValue());
    }

}
