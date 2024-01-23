package fi.methics.divvy.app;

import android.app.Application;


import fi.methics.divvy.BuildConfig;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.sscd.yubikey.YubiKeyOpenPgpSscd;

/**
 * MUSAP requires an Application class to perform the init.
 * This must also be declared in the manifest.
 */
public class DivvyApp extends Application {
    public static final String LINK_URL = BuildConfig.LINK_URL;

    @Override
    public void onCreate() {
        super.onCreate();

        MusapClient.init(this);
        MusapClient.enableSscd(new YubiKeyOpenPgpSscd(this));

    }

}
