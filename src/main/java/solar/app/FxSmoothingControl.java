package solar.app;

import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

/**
 *
 * @author rocky
 */
public class FxSmoothingControl extends Slider {

    public FxSmoothingControl() {
        super(1, 31, 1);
        setBlockIncrement(1);
        setMajorTickUnit(2);
        setMinorTickCount(0);
        setShowTickLabels(true);
        setPrefSize(400, 20);
        setSnapToTicks(true);
        setTooltip(new Tooltip("Moving average. The number is the width of the window in days."));
    }

    public int getSmoothingValue() {
        return (int) (getValue()+0.1);
    }
}
