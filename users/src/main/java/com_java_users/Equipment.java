package com_java_users;

import java.io.Serial;
import java.io.Serializable;

import com_java_users.Enums.EquipStatus;

public class Equipment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String equipName;
    private String equipId;
    private String labId;
    private EquipStatus status;
    private Integer qtyOnHand;
    private Integer qtyAvailable;

    public Equipment() {
        this.equipId = "";
        this.equipName = "";
        this.labId = "";
        this.status = EquipStatus.UNKNOWN;
        this.qtyOnHand = null;
        this.qtyAvailable = null;
    }
    public Equipment(String equipId, String equipName, String labId, EquipStatus status, int qtyOnHand, int qtyAvailable) {
        this.equipId = equipId;
        this.equipName = equipName;
        this.labId = labId;
        this.status = status;
        this.qtyOnHand = qtyOnHand;
        this.qtyAvailable = qtyAvailable;
    }
    // Default where equipment is available
     public Equipment(String equipId, String equipName, String labId, int qtyOnHand, int qtyAvailable) {
        this.equipId = equipId;
        this.equipName = equipName;
        this.labId = labId;
        this.status = EquipStatus.AVAILABLE;
        this.qtyOnHand = qtyOnHand;
        this.qtyAvailable = qtyAvailable;
    }

    public String getEquipName(){ return equipName; }
    public void setEquipName(String equipName){ this.equipName = equipName; }
    public String getEquipId(){ return equipId; }
    public void setEquipId(String equipId){ this.equipId = equipId; }
    public String getLabId(){ return labId; }
    public void setLabId(String labId){ this.labId = labId; }
    public EquipStatus getStatus(){ return status; }
    public void setStatus(EquipStatus status){ this.status = status; }
    public int getQtyAvailable(){ return qtyAvailable; }
    public void setQtyAvailable(int qtyAvailable){ this.qtyAvailable = qtyAvailable; }
    public int getQtyOnHand(){ return qtyOnHand; }
    public void setQtyOnHand(int qtyOnHand){ this.qtyOnHand = qtyOnHand; }
    @Override
    public String toString() {
        return "Equipment Name:" + equipName + "\nID:" + equipId + "\nStatus:" + status + "\nQty On Hand:" + qtyOnHand + "\nQty Available:" + qtyAvailable;
    }

    

}
