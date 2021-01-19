package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import javafx.concurrent.Task;

/**
 *
 * @author rocky
 */
public class DataStoreModel extends Task {

    // Include the effect of weather?
    private boolean weather = true;
    private EnergyStore battery = SystemData.battery;
    private SolarArray pv1 = SystemData.west;
    private SolarArray pv2 = SystemData.east;
    private SolarArray pv3 = SystemData.garage;
    private Inverter inv12 = SystemData.LuxPower;
    private Inverter inv3 = SystemData.SunnyBoy;

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
            for (int date = 0; date < Calculator.daysPerMonth[month]; date++) {
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
                    weatherFactor = SystemData.sunnyDays[month];
                } else {
                    weatherFactor = 1.0;
                }

                for (int solarHour = 0; solarHour < 24; solarHour += 1) {
                    // instantaneous power for each array
                    for (double fraction = 0; fraction < 0.99; fraction += stepSize) { // WRONG?
                        double pv1Power = pv1.availablePower(dday, solarHour + fraction) * weatherFactor;
                        double pv2Power = pv2.availablePower(dday, solarHour + fraction) * weatherFactor;
                        double pv3Power = pv3.availablePower(dday, solarHour + fraction) * weatherFactor;
                        double generated = inv12.pout(pv1Power + pv2Power) + inv3.pout(pv3Power);
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
                        double consumptionPower = SystemData.HOURLY_CONSUMPTION[solarHour] * 1000;

                        if (generated > consumptionPower) { // excess
                            // user first
                            selfUsePower = consumptionPower;
                            // then battery
                            double powerLeft = generated - selfUsePower;
                            pcharge = battery.store(powerLeft, stepSize);
                            // then export
                            exportPower = powerLeft - pcharge;
                            importPower = 0.0;
                        } else { // deficit
                            // user first
                            selfUsePower = generated;
                            // then from battery
                            double powerNeeded = consumptionPower - selfUsePower;
                            //double energyNeeded = powerNeeded * stepSize;
                            pdischarge = battery.demand(powerNeeded, stepSize);
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
                        r.seteChgDay((float) battery.getCharge() / 1000);
                        r.seteDisChgDay((float) battery.getDischarge() / 1000);
                        records.add(r);
                    }
                }
                dday++;
                battery.resetLog();
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

    /**
     * @param battery the battery to set
     */
    public void setBattery(EnergyStore battery) {
        Objects.nonNull(battery);
        this.battery = battery;
    }

    /**
     * @param pv1 the pv1 to set
     */
    public void setPv1(SolarArray pv1) {
        Objects.nonNull(pv1);
        this.pv1 = pv1;
    }

    /**
     * @param pv2 the pv2 to set
     */
    public void setPv2(SolarArray pv2) {
        Objects.nonNull(pv2);
        this.pv2 = pv2;
    }

    /**
     * @param pv3 the pv3 to set
     */
    public void setPv3(SolarArray pv3) {
        Objects.nonNull(pv3);
        this.pv3 = pv3;
    }

    /**
     * @param inv12 the inv12 to set
     */
    public void setInv12(Inverter inv12) {
        Objects.nonNull(inv12);
        this.inv12 = inv12;
    }

    /**
     * @param inv3 the inv3 to set
     */
    public void setInv3(Inverter inv3) {
        Objects.nonNull(inv3);
        this.inv3 = inv3;
    }
}
