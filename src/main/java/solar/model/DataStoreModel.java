package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import javafx.concurrent.Task;
import solar.model.Calculator.Weather;

/**
 * System model.
 *
 * @author rocky
 */
public class DataStoreModel extends Task {

    // Include the effect of weather?
    private Calculator.Weather weather;
    private Components components;
    private final Calculator calculator = new Calculator();

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

        super.updateMessage("Running model...");
        Collection<Record> records = new ArrayList<>();
        int dday = 0;

        // Asemble components
        for (int month = 0; month < 12; month++) {
            updateMessage("Modelling month " + month);
            for (int date = 1; date <= Calculator.daysPerMonth(month); date++) {
                // accumulators for each array
                float pv3DayTotal = 0;
                float pv2DayTotal = 0;
                float pv1DayTotal = 0;
                // accumulator for inverter output
                float inverterDayTotal = 0;
                float importedTotal = 0;
                float exportedTotal = 0;
                //float generatedDayTotal = 0;
                // time resolution, hours
                float stepSize = 0.25f;
                
                int dayNumber = Calculator.dayNumber(month, date);
                float weatherFactor = weather.getWeatherFactor(dayNumber);

                for (int solarHour = 0; solarHour < 24; solarHour += 1) {
                    // instantaneous power for each array
                    for (float fraction = 0; fraction < 0.99; fraction += stepSize) { // WRONG?
                        float pv1Power = (float) components.getPv1().availablePower(dday, solarHour + fraction) * weatherFactor;
                        float pv2Power = (float) components.getPv2().availablePower(dday, solarHour + fraction) * weatherFactor;
                        float pv3Power = (float) components.getPv3().availablePower(dday, solarHour + fraction) * weatherFactor;
                        // accumulate over day
                        pv1DayTotal += pv1Power * stepSize;
                        pv2DayTotal += pv2Power * stepSize;
                        pv3DayTotal += pv3Power * stepSize;

                        float selfUsePower = 0.0f;
                        float importPower = 0.0f;
                        float exportPower = 0.0f;
                        float pcharge = 0.0f;
                        float pdischarge = 0.0f;
                        float inverterOutputPower = 0.0f;

                        float maxAvailableWithoutBattery = (float) (components.getInv12().pout(pv1Power + pv2Power) + components.getInv12().pout(pv3Power));

                        float consumptionPower = (float) consumption.getDemand(solarHour);

                        if (maxAvailableWithoutBattery > consumptionPower) { // excess
                            // user first
                            selfUsePower = consumptionPower;
                            // then battery
                            float powerLeft = maxAvailableWithoutBattery - selfUsePower;
                            pcharge = (float) components.getBattery().store(powerLeft, stepSize);
                            pdischarge = 0.0f;
                            // then export
                            exportPower = powerLeft - pcharge;
                            importPower = 0.0f;
                            inverterOutputPower = consumptionPower + exportPower - importPower;
                        } else { // deficit
                            // user first
                            selfUsePower = maxAvailableWithoutBattery;
                            // then from battery
                            float powerNeeded = consumptionPower - selfUsePower;
                            //float energyNeeded = powerNeeded * stepSize;
                            pdischarge = (float) components.getBattery().demand(powerNeeded, stepSize);
                            pcharge = 0.0f;
                            importPower = powerNeeded - pdischarge;
                            exportPower = 0.0f;
                            inverterOutputPower = consumptionPower + exportPower - importPower;
                        }
                        importedTotal += importPower * stepSize;
                        exportedTotal += exportPower * stepSize;
                        inverterDayTotal += inverterOutputPower * stepSize;

                        Record r = new Record();
                        Date d = new Date(100, month, date);
                        d.setHours(solarHour);
                        d.setMinutes((int) (60 * fraction));
                        r.setDate(d);
                        r.setPpv1(pv1Power);
                        r.setPpv2(pv2Power);
                        r.setPpv3(pv3Power);
                        r.setPinv((inverterOutputPower));
                        r.setpToGrid(exportPower);
                        r.setpToUser(importPower);
                        r.setpCharge(pcharge);
                        r.setpDisCharge(pdischarge);
                        r.setpLoad((consumptionPower));

                        r.setePv1Day(pv1DayTotal / 1000);
                        r.setePv2Day(pv2DayTotal / 1000);
                        r.setePv3Day(pv3DayTotal / 1000);
                        r.seteInvDay(inverterDayTotal / 1000);
                        r.seteToUserDay(importedTotal / 1000);
                        r.seteToGridDay(exportedTotal / 1000);
                        r.seteChgDay((float) components.getBattery().getCharge() / 1000);
                        r.seteDisChgDay((float) components.getBattery().getDischarge() / 1000);
                        records.add(r);
                    }
                }
                dday++;
                components.getBattery().resetLog();
            }
        }
        updateMessage("Records=" + records.size());
        return records;
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
