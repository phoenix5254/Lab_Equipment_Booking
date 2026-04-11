package model.resource;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "reservation_equipments")

public class ReserveEquipment implements Serializable{
	@Id
	@Column(name = "reservationNum")
	private int resevationNum;
	@OneToOne
	@JoinColumn(name = "equipName")
	private String equipName;
	
	
	public ReserveEquipment(int resevationNum, String equipName) {
		this.resevationNum = resevationNum;
		this.equipName = equipName;
	}


	public int getResevationNum() {
		return resevationNum;
	}


	public void setResevationNum(int resevationNum) {
		this.resevationNum = resevationNum;
	}


	public String getEquipName() {
		return equipName;
	}


	public void setEquipName(String equipName) {
		this.equipName = equipName;
	}
	
	
	
	

}
