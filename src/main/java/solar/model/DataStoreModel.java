package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import javafx.concurrent.Task;

/**
 * System model.
 * 
 * @author rocky
 */
public class DataStoreModel extends Task {

    // Perfect inverter for comparison
    // public static final Inverter perfectInverter = Inverter.valueOf("Perfect", "", 100000, 1.0);
    // Total 9 kWh per day, guessed profile per hour in kW
    public static final double[] HOURLY_CONSUMPTION = {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 1.0, 2.8, 1.0, 0.2, 0.2, 0.2, 0.2};

    // Include the effect of weather?
    private boolean weather = true;
    private Components components;

    /**
     * Run simulation and convert the outputs into log records as if they had
     * come from the Lux Power.
     *
     * Pv3 is correctly modelled as attached to Inv3, but as there isn't a
     * separate field in Record for it, the output is added to the (single)
     * inverter output.
     *
     * @return
     */
    @Override
    public Collection<Record> call() {

        super.updateMessage("Running model...");
        Collection<Record> records = new ArrayList<>();
        int dday = 0;

        // Asemble components
        for (int month = 0; month < 12; month++) {
            updateMessage("Modelling month " + month);
            for (int date = 0; date < Calculator.daysPerMonth(month); date++) {
                // accumulators for each array
                double pv3DayTotal = 0;
                double pv2DayTotal = 0;
                double pv1DayTotal = 0;
                // accumulator for inverter output
                double inverterDayTotal = 0;
                double importedTotal = 0;
                double exportedTotal = 0;
                double generatedDayTotal = 0;
                // time resolution, hours
                double stepSize = 0.25;
                double weatherFactor = 0.0;
                if (weather) {
                    weatherFactor = Calculator.sunnyDays(month);
                } else {
                    weatherFactor = 1.0;
                }

                for (int solarHour = 0; solarHour < 24; solarHour += 1) {
                    // instantaneous power for each array
                    for (double fraction = 0; fraction < 0.99; fraction += stepSize) { // WRONG?
                        double pv1Power = components.getPv1().availablePower(dday, solarHour + fraction) * weatherFactor;
                        double pv2Power = components.getPv2().availablePower(dday, solarHour + fraction) * weatherFactor;
                        double pv3Power = components.getPv3().availablePower(dday, solarHour + fraction) * weatherFactor;
                        double generated = components.getInv12().pout(pv1Power + pv2Power + pv3Power);
                        // accumulate over day
                        pv1DayTotal += pv1Power * stepSize;
                        pv2DayTotal += pv2Power * stepSize;
                        pv3DayTotal += pv3Power * stepSize;
                        generatedDayTotal += generated * stepSize;

                        // construct and write record
                        double selfUsePower = 0.0;
                        double importPower = 0.0;
                        double exportPower = 0.0;
                        double pcharge = 0.0;
                        double pdischarge = 0.0;
                        double consumptionPower = HOURLY_CONSUMPTION[solarHour] * 1000;

                        if (generated > consumptionPower) { // excess
                            // user first
                            selfUsePower = consumptionPower;
                            // then battery
                            double powerLeft = generated - selfUsePower;
                            pcharge = components.getBattery().store(powerLeft, stepSize);
                            // then export
                            exportPower = powerLeft - pcharge;
                            importPower = 0.0;
                        } else { // deficit
                            // user first
                            selfUsePower = generated;
                            // then from battery
                            double powerNeeded = consumptionPower - selfUsePower;
                            //double energyNeeded = powerNeeded * stepSize;
                            pdischarge = components.getBattery().demand(powerNeeded, stepSize);
                            //pdischarge = energyTaken / stepSize;
                            importPower = powerNeeded - pdischarge;
                            exportPower = 0.0;
                        }
                        double inverterArrayPower = generated + pdischarge;
                        importedTotal += importPower * stepSize;
                        exportedTotal += exportPower * stepSize;
                        inverterDayTotal += inverterArrayPower * stepSize;
                        generatedDayTotal += generated * stepSize;

                        Record r = new Record();
                        Date d = new Date(100, month, date);
                        d.setHours(solarHour);
                        d.setMinutes((int) (60 * fraction));
                        r.setDate(d);
                        r.setPpv1((float) pv1Power);
                        r.setPpv2((float) pv2Power);
                        r.setPpv3((float) pv3Power);
                        r.setPinv((float) (inverterArrayPower));
                        r.setpToGrid((float) exportPower);
                        r.setpToUser((float) importPower);
                        r.setpCharge((float) pcharge);
                        r.setpDisCharge((float) pdischarge);

                        r.setePv1Day((float) pv1DayTotal / 1000);
                        r.setePv2Day((float) pv2DayTotal / 1000);
                        r.setePv3Day((float) pv3DayTotal / 1000);
                        r.seteInvDay((float) inverterDayTotal / 1000);
                        r.seteToUserDay((float) importedTotal / 1000);
                        r.seteToGridDay((float) exportedTotal / 1000);
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

    /**
     * Include the effects of weather?
     *
     * If true allow for the reduction in output caused by poor weather. If
     * false, outputs (unrealistic) sunny-days-always.
     *
     * @design Just scales by a month-determined amount. Not very realistic
     * since it means the inverters will never limit. Better to apply a
     * distribution with the same mean?
     *
     * @param weather the weather to set
     */
    public void setWeather(boolean weather) {
        this.weather = weather;
    }

    @Override
    public String toString() {
        return "Modelled. Weather " + (weather ? "" : "not") + " included.";
    }

    public void setComponents(Components componentsList) {
        Objects.nonNull(componentsList);
        this.components = componentsList;
    }
}
