package solar.app;

import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

/**
 *
 * @author rocky
 */
public class FxMonthControl extends Slider {

    public FxMonthControl() {
        super(1, 12, 1);
        setBlockIncrement(1);
        setMajorTickUnit(1);
        setMinorTickCount(0);
        setShowTickLabels(true);
        setPrefSize(400, 20);
        setSnapToTicks(true);
        setTooltip(new Tooltip("Month 1-12"));
    }

    /**
     * Month 0-11
     * 
     * @return 
     */
    public int getMonth() {
        return (int) getValue()-1;
    }
}
