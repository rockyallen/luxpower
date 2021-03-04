package solar.model;

import java.io.Serializable;
import java.util.Date;
import javax.measure.Quantity;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import static solar.model.SolarUnitsAndConstants.KILO_WATT_HOUR;
import tech.units.indriya.quantity.Quantities;
import static tech.units.indriya.unit.Units.VOLT;
import static tech.units.indriya.unit.Units.WATT;

/**
 * Row from the LuxPower export spreadsheets.
 *
 * @author rocky
 */
public class Record implements Serializable, Comparable, DateProvider {

    private static final long serialVersionUID = 365777397454380L;
    private String Serial_number; // 0
    //    public String Time;
    private String Status;
    private float vpv1; // V DC 3
    private float vpv2; // V DC 4
    private float vpv3; // V DC 5
    private float vBat; // V DC 6
    private float soc; // formatted % 7
    private float ppv1; // W
    private float ppv2; // W
    private float ppv3; // W 10
    private float pCharge;
    private float pDisCharge;
    //    public String vacr;
    //    public String vacs;
    //    public String vact;
    //    public String fac;
    private float pinv; // W
    //    public String prec;
    //    public String pf;
    //    public String vepsr; // 20
    //    public String vepss;
    //    public String vepst;
    //    public String feps;
    //    public String peps;
    //    public String seps;
    private float pToGrid;
    private float pToUser;
    private float pLoad;
    private float ePv1Day; //kWh
    private float ePv2Day; // kWh 30
    private float ePv3Day; // kWh
    private float eInvDay;
    //    public String eRecDay;
    private float eChgDay; // kWh
    private float eDisChgDay; // kWh
    //    public String eEpsDay;
    private float eToGridDay; // kWh
    private float eToUserDay;
    //    public String ePv1All;
    //    public String ePv2All; // 40
    //    public String ePv3All;
    //    public String eInvAll;
    //    public String eRecAll;
    private float eChgAll; // kWh
    private float eDisChgAll; // kWh
    //    public String eEpsAll;
    //    public String eToGridAll;
    //    public String eToUserAll;
    //    public String faultCode;
    //    public String warningCode; // 50
    //    public String tinner;
    //    public String tradiator1;
    //    public String tradiator2;
    //    public String tBat;
    //    public String vBus1;
    //    public String vBus2;
    //    public String maxChgCurr;
    //    public String maxDischgCurr;
    //    public String chargeVoltRef;
    //    public String dischgCutVolt; // 60
    //    public String batStatus0;
    //    public String batStatus1;
    //    public String batStatus2;
    //    public String batStatus3;
    //    public String batStatus4;
    //    public String batStatus5;
    //    public String batStatus6;
    //    public String batStatus7;
    //    public String batStatus8;
    //    public String batStatus9; // 70
    //    public String batStatusInv;
    //    public String internalFault;
    //    public String BatCurrent;
    //    public String BMSEvent1;
    //    public String BMSEvent2;
    //    public String MaxCellVoltage;
    //    public String MinCellVoltage;
    //    public String MaxCellTemp;
    //    public String MinCellTemp;
    //    public String BMSFWUpdateState; // 80
    //    public String CycleCnt;
    //    public String Vbat_Inv;
    private Date date;

    @Override
    public String toString() {
        return getDate() + ": " + getSoc();
    }

    /**
     * Non-standard implementation. 
     * 
     * Only care about the date.
     * 
     * @param t
     * 
     * @return 
     */
    @Override
    public int compareTo(Object t) {
        Record that = (Record) t;
        return this.getDate().compareTo(that.getDate());
    }

    /**
     * Non-standard implementation. 
     * 
     * Only care about the date.
     * 
     * @param t
     * 
     * @return 
     */
    @Override
    public boolean equals(Object t) {
        Record that = (Record) t;
        return this.getDate().equals(that.getDate());
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the Serial_number
     */
    public String getSerial_number() {
        return Serial_number;
    }

    /**
     * @param Serial_number the Serial_number to set
     */
    public void setSerial_number(String Serial_number) {
        this.Serial_number = Serial_number;
    }

    /**
     * @return the Status
     */
    public String getStatus() {
        return Status;
    }

    /**
     * @param Status the Status to set
     */
    public void setStatus(String Status) {
        this.Status = Status;
    }

    public Quantity<ElectricPotential> getVpv1() {
        return Quantities.getQuantity(vpv1, VOLT);
    }

    public void setVpv1(Quantity<ElectricPotential> v) {
        this.vpv1 = v.to(VOLT).getValue().floatValue();
    }

    public Quantity<ElectricPotential> getVpv2() {
        return Quantities.getQuantity(vpv2, VOLT);
    }

    public void setVpv2(Quantity<ElectricPotential> v) {
        this.vpv2 = v.to(VOLT).getValue().floatValue();
    }

    public Quantity<ElectricPotential> getVpv3() {
        return Quantities.getQuantity(vpv3, VOLT);
    }

    public void setVpv3(Quantity<ElectricPotential> v) {
        this.vpv3 = v.to(VOLT).getValue().floatValue();
    }

    public Quantity<ElectricPotential> getVBat() {
        return Quantities.getQuantity(vBat, VOLT);
    }

    public void setVbat(Quantity<ElectricPotential> v) {
        this.vBat = v.to(VOLT).getValue().floatValue();
    }

    /**
     * @return the soc
     */
    public float getSoc() {
        return soc;
    }

    /**
     * @param soc the soc to set
     */
    public void setSoc(float soc) {
        this.soc = soc;
    }

    public Quantity<Power> getPpv1() {
        return Quantities.getQuantity(ppv1, WATT);
    }

    public void setPpv1(Quantity<Power> p) {
        this.ppv1 = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getPpv2() {
        return Quantities.getQuantity(ppv2, WATT);
    }

    public void setPpv2(Quantity<Power> p) {
        this.ppv2 = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getPpv3() {
        return Quantities.getQuantity(ppv3, WATT);
    }

    public void setPpv3(Quantity<Power> p) {
        this.ppv3 = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getpToGrid() {
        return Quantities.getQuantity(pToGrid, WATT);
    }

    public void setpToGrid(Quantity<Power> p) {
        this.pToGrid = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getpToUser() {
        return Quantities.getQuantity(pToUser, WATT);
    }

    public void setpToUser(Quantity<Power> p) {
        this.pToUser = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getPinv() {
        return Quantities.getQuantity(pinv, WATT);
    }

    public void setPinv(Quantity<Power> p) {
        this.pinv = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getpCharge() {
        return Quantities.getQuantity(pCharge, WATT);
    }

    public void setpCharge(Quantity<Power> p) {
        this.pCharge = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getpDisCharge() {
        return Quantities.getQuantity(pDisCharge, WATT);
    }

    public void setpDisCharge(Quantity<Power> p) {
        this.pDisCharge = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Power> getpLoad() {
        return Quantities.getQuantity(pLoad, WATT);
    }

    public void setpLoad(Quantity<Power> p) {
        this.pLoad = p.to(WATT).getValue().floatValue();
    }

    public Quantity<Energy> getePv1Day() {
        return Quantities.getQuantity(ePv1Day, KILO_WATT_HOUR);
    }

    public void setePv1Day(Quantity<Energy> e) {
        this.ePv1Day = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> getePv2Day() {
        return Quantities.getQuantity(ePv2Day, KILO_WATT_HOUR);
    }

    public void setePv2Day(Quantity<Energy> e) {
        this.ePv2Day = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> getePv3Day() {
        return Quantities.getQuantity(ePv3Day, KILO_WATT_HOUR);
    }

    public void setePv3Day(Quantity<Energy> e) {
        this.ePv3Day = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> geteChgDay() {
        return Quantities.getQuantity(eChgDay, KILO_WATT_HOUR);
    }

    public void seteChgDay(Quantity<Energy> e) {
        this.eChgDay = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> geteDisChgDay() {
        return Quantities.getQuantity(eDisChgDay, KILO_WATT_HOUR);
    }

    public void seteDisChgDay(Quantity<Energy> e) {
        this.eDisChgDay = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> geteToGridDay() {
        return Quantities.getQuantity(eToGridDay, KILO_WATT_HOUR);
    }

    public void seteToGridDay(Quantity<Energy> e) {
        this.eToGridDay = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> geteToUserDay() {
        return Quantities.getQuantity(eToUserDay, KILO_WATT_HOUR);
    }

    public void seteToUserDay(Quantity<Energy> e) {
        this.eToUserDay = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> geteDisChgAll() {
        return Quantities.getQuantity(eDisChgAll, KILO_WATT_HOUR);
    }

    public void seteDisChgAll(Quantity<Energy> e) {
        this.eDisChgAll = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> geteChgAll() {
        return Quantities.getQuantity(eChgAll, KILO_WATT_HOUR);
    }

    public void seteChgAll(Quantity<Energy> e) {
        this.eChgAll = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    public Quantity<Energy> geteInvDay() {
        return Quantities.getQuantity(eInvDay, KILO_WATT_HOUR);
    }

    public void seteInvDay(Quantity<Energy> e) {
        this.eInvDay = e.to(KILO_WATT_HOUR).getValue().floatValue();
    }

    void validate() {
        if (date == null) {
            throw new IllegalStateException("null date");
        }
    }
}
