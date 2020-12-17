package it.systemslab.mqttonbiottest;

public class MqttEnum {
    public enum CommandType {
        Sync,
        Alarm
    }

    public enum CommandDanger {
        Flooding,
        GasDetected,
        GenericAlert,
        NoiseDetected,
        MaximumCapacity,
        BackToCabin,
        BackToOffices,
        EvacuationOffices,
        Ring
    }
}
