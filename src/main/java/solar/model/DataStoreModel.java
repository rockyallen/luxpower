package solar.model;

import java.util.Collection;
import java.util.Objects;
import javafx.concurrent.Task;
import javax.measure.Quantity;
import javax.measure.Unit;
import solar.model.Calculator.Weather;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

/**
 * System model.
 *
 * @author rocky
 */
public class DataStoreModel extends Task {

//    private static Quantity<T extends Unit> zero(T unit) {
//        return Quantities.getQuantity(0.0, unit);
//    }

    // Include the effect of weather?
    private Calculator.Weather weather;
    private Components components;
    private final Calculator calculator =  Calculator.getInstance();

    public DataStoreModel() {
        this.weather = Calculator.Weather.NOWEATHER;
    }

    /**
     * Run simulation and convert the outputs into log records as if they had
     * come from the Lux Power.
     *
     * Pv3 is correctly modelled as attached to Inv3, but as there isn't a
     * separate field in Record for it, the output is added to the (single)
     * inverter output.
     *
     * @returnDataStoreModel
     */
    @Override
    public Collection<Record> call() {

        return call2(true);
    }
    public Collection<Record> call2(boolean task) {

        return new Model().run(weather, consumption, components, Quantities.getQuantity(0.5,Units.HOUR));
    }

    @Override
    public String toString() {
        return "Modelled. Weather " + weather;
    }

    public void setComponents(Components componentsList) {
        Objects.nonNull(componentsList);
        this.components = componentsList;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    private Consumption consumption = null;

    /**
     * @param consumption the consumption to set
     */
    public void setConsumption(Consumption consumption) {
        this.consumption = consumption;
    }   
}
