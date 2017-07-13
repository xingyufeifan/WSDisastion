package com.nandi.wsdisastion;

/**
 * Created by ChenPeng on 2017/7/11.
 */

public class DisasterPoint {

    /**
     * id : 62684
     * dis_name : 关山沟滑坡
     * dis_lon : 106.8088
     * dis_lat : 28.8683
     */

    private int id;
    private String dis_name;
    private String dis_lon;
    private String dis_lat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDis_name() {
        return dis_name;
    }

    public void setDis_name(String dis_name) {
        this.dis_name = dis_name;
    }

    public String getDis_lon() {
        return dis_lon;
    }

    public void setDis_lon(String dis_lon) {
        this.dis_lon = dis_lon;
    }

    public String getDis_lat() {
        return dis_lat;
    }

    public void setDis_lat(String dis_lat) {
        this.dis_lat = dis_lat;
    }

    @Override
    public String toString() {
        return "DisasterPoint{" +
                "id=" + id +
                ", dis_name='" + dis_name + '\'' +
                ", dis_lon='" + dis_lon + '\'' +
                ", dis_lat='" + dis_lat + '\'' +
                '}';
    }
}
