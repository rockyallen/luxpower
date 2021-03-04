package solar.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;
import static solar.model.SolarUnitsAndConstants.*;
import tech.units.indriya.ComparableQuantity;

/**
 *
 * @author rocky
 */
public class Model {

    Collection<Record> run(Calculator.Weather weather, Consumption consumption, Components components, ComparableQuantity<Time> stepSize) {
        updateMessage("Running model...");
        Collection<Record> records = new ArrayList<>();
        int dday = 0;
        // Assemble components
        for (int month = 0; month < 12; month++) {
            updateMessage("Modelling month " + month);
            for (int date = 1; date <= Calculator.daysPerMonth(month); date++) {
                // accumulators for each array
                ComparableQuantity<Energy> pv1DayTotal = ZERO_ENERGY;
                ComparableQuantity<Energy> pv2DayTotal = ZERO_ENERGY;
                ComparableQuantity<Energy> pv3DayTotal = ZERO_ENERGY;
                // accumulator for inverter output
                ComparableQuantity<Energy> inverterDayTotal = ZERO_ENERGY;
                ComparableQuantity<Energy> importedTotal = ZERO_ENERGY;
                ComparableQuantity<Energy> exportedTotal = ZERO_ENERGY;

                int dayNumber = Calculator.dayNumber(month, date);
                float weatherFactor = weather.getWeatherFactor(dayNumber);
                for (int solarHour = 0; solarHour < 24; solarHour += 1) {
                    // instantaneous power for each array
                    ComparableQuantity<Time> t = ZERO_TIME;
                    while (t.getValue().doubleValue() < 1.0) {
                        t = t.add(stepSize);
                        double hour = solarHour + t.getValue().doubleValue();
                        ComparableQuantity<Power> pv1Power = components.getPv1().availablePower(dday, hour).multiply(weatherFactor);
                        ComparableQuantity<Power> pv2Power = components.getPv2().availablePower(dday, hour).multiply(weatherFactor);
                        ComparableQuantity<Power> pv3Power = components.getPv3().availablePower(dday, hour).multiply(weatherFactor);
                        // accumulate over day
                        pv1DayTotal = pv1DayTotal.add(pv1Power.multiply(stepSize).asType(Energy.class));
                        pv2DayTotal = pv1DayTotal.add(pv2Power.multiply(stepSize).asType(Energy.class));
                        pv3DayTotal = pv1DayTotal.add(pv3Power.multiply(stepSize).asType(Energy.class));
                        ComparableQuantity<Power> selfUsePower = ZERO_POWER;
                        ComparableQuantity<Power> importPower = ZERO_POWER;
                        ComparableQuantity<Power> exportPower = ZERO_POWER;
                        ComparableQuantity<Power> pcharge = ZERO_POWER;
                        ComparableQuantity<Power> pdischarge = ZERO_POWER;
                        ComparableQuantity<Power> inverterOutputPower = ZERO_POWER;
                        ComparableQuantity<Power> maxAvailableWithoutBattery
                                = components.getInv12().pout(pv1Power.add(pv2Power)).add(components.getInv12().pout(pv3Power));
                        ComparableQuantity<Power> consumptionPower = consumption.getDemand(solarHour);
                        if (maxAvailableWithoutBattery.compareTo(consumptionPower) > 0) {
                            // excess
                            // user first
                            selfUsePower = consumptionPower;
                            // then battery
                            ComparableQuantity<Power> powerLeft = maxAvailableWithoutBattery.subtract(selfUsePower);
                            pcharge = components.getBattery().store(powerLeft, stepSize);
                            pdischarge = ZERO_POWER;
                            // then export
                            exportPower = powerLeft.subtract(pcharge);
                            importPower = ZERO_POWER;
                            inverterOutputPower = consumptionPower.add(exportPower).subtract(importPower);
                        } else {
                            // deficit
                            // user first
                            selfUsePower = maxAvailableWithoutBattery;
                            // then from battery
                            ComparableQuantity<Power> powerNeeded = consumptionPower.subtract(selfUsePower);
                            pdischarge = components.getBattery().demand(powerNeeded, stepSize);
                            pcharge = ZERO_POWER;
                            importPower = powerNeeded.subtract(pdischarge);
                            exportPower = ZERO_POWER;
                            inverterOutputPower = consumptionPower.add(exportPower).subtract(importPower);
                        }
                        importedTotal = importedTotal.add(importPower.multiply(stepSize).asType(Energy.class));
                        exportedTotal = exportedTotal.add(exportPower.multiply(stepSize).asType(Energy.class));
                        inverterDayTotal = inverterDayTotal.add(inverterOutputPower.multiply(stepSize).asType(Energy.class));
                        Record r = new Record();
                        Date d = new Date(100, month, date);
                        d.setHours(solarHour);
                        d.setMinutes((int) (60 * t.getValue().doubleValue()));
                        r.setDate(d);
                        r.setPpv1(pv1Power);
                        r.setPpv2(pv2Power);
                        r.setPpv3(pv3Power);
                        r.setPinv(inverterOutputPower);
                        r.setpToGrid(exportPower);
                        r.setpToUser(importPower);
                        r.setpCharge(pcharge);
                        r.setpDisCharge(pdischarge);
                        r.setpLoad((consumptionPower));
                        r.setePv1Day(pv1DayTotal);
                        r.setePv2Day(pv2DayTotal);
                        r.setePv3Day(pv3DayTotal);
                        r.seteInvDay(inverterDayTotal);
                        r.seteToUserDay(importedTotal);
                        r.seteToGridDay(exportedTotal);
                        r.seteChgDay(components.getBattery().getCharge());
                        r.seteDisChgDay(components.getBattery().getDischarge());
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

    private void updateMessage(String s) {
        System.out.println(s);
    }
}
