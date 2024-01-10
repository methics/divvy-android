package fi.methics.divvy.app;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.api.MusapException;
import fi.methics.musap.sdk.internal.datatype.MusapLink;
import fi.methics.musap.sdk.internal.util.MLog;
import fi.methics.musap.sdk.sscd.yubikey.YubiKeyOpenPgpSscd;

/**
 * MUSAP requires an Application class to perform the init.
 * This must also be declared in the manifest.
 */
public class DivvyApp extends Application {
    public static final String LINK_URL = "https://demo.methics.fi/musapdemo/";

    @Override
    public void onCreate() {
        super.onCreate();

        MusapClient.init(this);
        MusapClient.enableSscd(new YubiKeyOpenPgpSscd(this));

    }
}
