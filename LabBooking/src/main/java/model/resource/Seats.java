package model.resource;

import java.io.Serializable;
import java.util.List;

public abstract interface Seats extends Serializable{//
       abstract List<SeatRecord> addSeats(Lab lab);      
}
