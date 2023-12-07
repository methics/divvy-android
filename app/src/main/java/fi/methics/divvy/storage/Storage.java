package fi.methics.divvy.storage;

import android.content.Context;

public class Storage {

    private final Context c;

    public Storage(Context c) {
        this.c = c;
    }
    public void storeCouplingCode(String couplingCode) {

    }

    public String getCouplingCode() {
        return "123";
    }


    public boolean isCouplingComplete() {
        return false;
    }

}
