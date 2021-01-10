package solar.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author rocky
 */
public class Record implements Serializable , Comparable, DateProvider {

    private static final long serialVersionUID = 365777397454380L;
 
    private String Serial_number; // 0
//    public String Time;
    private String Status;
    private float vpv1; // V DC 3
    private float vpv2; // V DC 4
    private float vpv3; // V DC 5
    private float vBat; // V DC 6
    private float soc;  // formatted % 7
    private float ppv1; // kW
    private float ppv2; // kW
    private float ppv3; // 10
//    public String pCharge;
//    public String pDisCharge;
//    public String vacr;
//    public String vacs;
//    public String vact;
//    public String fac;
//    public String pinv;
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
//    public String pLoad;
    private float ePv1Day; //kWh
    private float ePv2Day; // kWh 30
    private float ePv3Day; // kWh
//    public String eInvDay;
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
    public String toString(){
        return(getDate()+": "+ getSoc());
    }

    @Override
    public int compareTo(Object t) {
        Record that = (Record)t;
        return this.getDate().compareTo(that.getDate());
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

    /**
     * @return the vpv1
     */
    public float getVpv1() {
        return vpv1;
    }

    /**
     * @param vpv1 the vpv1 to set
     */
    public void setVpv1(float vpv1) {
        this.vpv1 = vpv1;
    }

    /**
     * @return the vpv2
     */
    public float getVpv2() {
        return vpv2;
    }

    /**
     * @param vpv2 the vpv2 to set
     */
    public void setVpv2(float vpv2) {
        this.vpv2 = vpv2;
    }

    /**
     * @return the vpv3
     */
    public float getVpv3() {
        return vpv3;
    }

    /**
     * @param vpv3 the vpv3 to set
     */
    public void setVpv3(float vpv3) {
        this.vpv3 = vpv3;
    }

    /**
     * @return the vBat
     */
    public float getvBat() {
        return vBat;
    }

    /**
     * @param vBat the vBat to set
     */
    public void setvBat(float vBat) {
        this.vBat = vBat;
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

    /**
     * @return the ppv1
     */
    public float getPpv1() {
        return ppv1;
    }

    /**
     * @param ppv1 the ppv1 to set
     */
    public void setPpv1(float ppv1) {
        this.ppv1 = ppv1;
    }

    /**
     * @return the ppv2
     */
    public float getPpv2() {
        return ppv2;
    }

    /**
     * @param ppv2 the ppv2 to set
     */
    public void setPpv2(float ppv2) {
        this.ppv2 = ppv2;
    }

    /**
     * @return the ppv3
     */
    public float getPpv3() {
        return ppv3;
    }

    /**
     * @param ppv3 the ppv3 to set
     */
    public void setPpv3(float ppv3) {
        this.ppv3 = ppv3;
    }

    /**
     * @return the ePv1Day
     */
    public float getePv1Day() {
        return ePv1Day;
    }

    /**
     * @param ePv1Day the ePv1Day to set
     */
    public void setePv1Day(float ePv1Day) {
        this.ePv1Day = ePv1Day;
    }

    /**
     * @return the ePv2Day
     */
    public float getePv2Day() {
        return ePv2Day;
    }

    /**
     * @param ePv2Day the ePv2Day to set
     */
    public void setePv2Day(float ePv2Day) {
        this.ePv2Day = ePv2Day;
    }

    /**
     * @return the ePv3Day
     */
    public float getePv3Day() {
        return ePv3Day;
    }

    /**
     * @param ePv3Day the ePv3Day to set
     */
    public void setePv3Day(float ePv3Day) {
        this.ePv3Day = ePv3Day;
    }

    /**
     * @return the eChgDay
     */
    public float geteChgDay() {
        return eChgDay;
    }

    /**
     * @param eChgDay the eChgDay to set
     */
    public void seteChgDay(float eChgDay) {
        this.eChgDay = eChgDay;
    }

    /**
     * @return the eDisChgDay
     */
    public float geteDisChgDay() {
        return eDisChgDay;
    }

    /**
     * @param eDisChgDay the eDisChgDay to set
     */
    public void seteDisChgDay(float eDisChgDay) {
        this.eDisChgDay = eDisChgDay;
    }

    /**
     * @return the eToGridDay
     */
    public float geteToGridDay() {
        return eToGridDay;
    }

    /**
     * @param eToGridDay the eToGridDay to set
     */
    public void seteToGridDay(float eToGridDay) {
        this.eToGridDay = eToGridDay;
    }

    /**
     * @return the eToUserDay
     */
    public float geteToUserDay() {
        return eToUserDay;
    }

    /**
     * @param eToUserDay the eToUserDay to set
     */
    public void seteToUserDay(float eToUserDay) {
        this.eToUserDay = eToUserDay;
    }

    /**
     * @return the eChgAll
     */
    public float geteChgAll() {
        return eChgAll;
    }

    /**
     * @param eChgAll the eChgAll to set
     */
    public void seteChgAll(float eChgAll) {
        this.eChgAll = eChgAll;
    }

    /**
     * @return the eDisChgAll
     */
    public float geteDisChgAll() {
        return eDisChgAll;
    }

    /**
     * @param eDisChgAll the eDisChgAll to set
     */
    public void seteDisChgAll(float eDisChgAll) {
        this.eDisChgAll = eDisChgAll;
    }

    void validate() {
        if (date==null) throw new IllegalStateException("null date");
    }

    /**
     * @return the pToGrid
     */
    public float getpToGrid() {
        return pToGrid;
    }

    /**
     * @param pToGrid the pToGrid to set
     */
    public void setpToGrid(float pToGrid) {
        this.pToGrid = pToGrid;
    }

    /**
     * @return the pToUser
     */
    public float getpToUser() {
        return pToUser;
    }

    /**
     * @param pToUser the pToUser to set
     */
    public void setpToUser(float pToUser) {
        this.pToUser = pToUser;
    }
}
