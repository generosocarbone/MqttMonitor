package it.systemslab.mqttonbiottest;

public class MqttCloudToDeviceAdditionalData {

    private MqttEnum.CommandDanger danger;
    private Integer area;
    private String collectionPoint;
    private String alias;

    public MqttEnum.CommandDanger getDanger() {
        return danger;
    }

    public void setDanger(MqttEnum.CommandDanger danger) {
        this.danger = danger;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public String getCollectionPoint() {
        return collectionPoint;
    }

    public void setCollectionPoint(String collectionPoint) {
        this.collectionPoint = collectionPoint;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
