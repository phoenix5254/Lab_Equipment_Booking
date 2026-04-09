package model;

import java.io.Serial;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import model.EquipStatus;
@Entity
@Table(name="equipment")

public class Equipment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name="equipId")
    private String equipId;
    private String equipName;//
    @ManyToOne
    @JoinColumn(name="labId")
    private Lab lab;
    private String status;
    private Integer qtyOnHand;
    private Integer qtyAvailable;

    public Equipment() {
        this.equipId = "";
        this.equipName = "";
        this.lab = new Lab();
        this.status = EquipStatus.UNKNOWN.toString();
        this.qtyOnHand = null;    
        this.qtyAvailable = null;
    }
    public Equipment(String equipId, String equipName, Lab lab, EquipStatus status, int qtyOnHand, int qtyAvailable) {
        this.equipId = equipId;
        this.equipName = equipName;
        this.lab = lab;
        this.status = status.toString();
        this.qtyOnHand = qtyOnHand;
        this.qtyAvailable = qtyAvailable;
    }
    // Default where equipment is available
     public Equipment(String equipId, String equipName, Lab lab, int qtyOnHand, int qtyAvailable) {
        this.equipId = equipId;
        this.equipName = equipName;
        this.lab = lab;
        this.status = EquipStatus.AVAILABLE.toString();
        this.qtyOnHand = qtyOnHand;
        this.qtyAvailable = qtyAvailable;
    }

    public String getEquipName(){ return equipName; }
    public void setEquipName(String equipName){ this.equipName = equipName; }
    public String getEquipId(){ return equipId; }
    public void setEquipId(String equipId){ this.equipId = equipId; }
    public Lab getLab(){ return lab; }
    public void setLab(Lab lab){ this.lab = lab; }
    public String getStatus(){ return status; }
    public void setStatus(String status){ this.status = status; }
    public int getQtyAvailable(){ return qtyAvailable; }
    public void setQtyAvailable(int qtyAvailable){ this.qtyAvailable = qtyAvailable; }
    public int getQtyOnHand(){ return qtyOnHand; }
    public void setQtyOnHand(int qtyOnHand){ this.qtyOnHand = qtyOnHand; }
    @Override
    public String toString() {
        return "Equipment Name:" + equipName + "\nID:" + equipId + "\nStatus:" + status + "\nQty On Hand:" + qtyOnHand + "\nQty Available:" + qtyAvailable;
    }

    

}