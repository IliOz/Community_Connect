package com.example.trafic001;

import java.io.Serializable;

public class Passenger implements Serializable {
    public String getTarg_Station() {
        return Targ_Station;
    }

    public String getPasType() {
        return pasType;
    }

    public int getStart_Hour() {
        return Start_Hour;
    }

    private final String Targ_Station;
    private final String pasType;
    private final int Start_Hour;

    public Passenger(String Targ_Station, String pasType, int Start_Hour) {
        this.Targ_Station = Targ_Station;
        this.pasType = pasType;
        this.Start_Hour = Start_Hour;
    }


    @Override
    public String toString() {
        return "Passenger{" +
                "Targ_Station='" + Targ_Station + '\'' +
                ", pasType='" + pasType + '\'' +
                ", Start_Hour=" + Start_Hour;
    }

}
