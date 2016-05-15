package fi.babywellness.babyhale.database;

import java.io.Serializable;

public class TestDatabaseEntry implements Serializable {

    String vnr;
    String name;
    String effects;

    public TestDatabaseEntry() {
    }

    public TestDatabaseEntry(String vnr, String name, String effects) {
        this.vnr = vnr;
        this.name = name;
        this.effects = effects;
    }

    public String getVnr() {
        return vnr;
    }

    public void setVnr(String vnr) {
        this.vnr = vnr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEffects() {
        return effects;
    }

    public void setEffects(String effects) {
        this.effects = effects;
    }

    @Override
    public String toString() {
        return "Vnr: " + getVnr() + ", name: " + getName() + ", effects: " + getEffects();
    }
}
