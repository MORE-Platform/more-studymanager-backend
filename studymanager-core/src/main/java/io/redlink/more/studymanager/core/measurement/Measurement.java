package io.redlink.more.studymanager.core.measurement;

public class Measurement {
    public enum Type {
        INTEGER,DOUBLE,STRING,BOOLEAN,DATE,OBJECT
    }

    private String id;
    private Type type;

    public static Measurement Any = new Measurement(null, Type.OBJECT);

    public Measurement(String id, Type type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }
}
