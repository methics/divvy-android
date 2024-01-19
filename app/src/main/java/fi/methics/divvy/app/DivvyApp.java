package fi.methics.divvy.app;

import android.app.Application;

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

    // KWM MUSAP Link URL
    public static final String LINK_URL = "https://demo.methics.fi/musapdemo/";

    // Demo MUSAP Link URl
//    public static final String LINK_URL = "https://demo.methics.fi/musap/";

    @Override
    public void onCreate() {
        super.onCreate();

        MusapClient.init(this);
        MusapClient.enableSscd(new YubiKeyOpenPgpSscd(this));

        String musapId = MusapClient.getMusapId();

        // If MUSAP is not enrolled, enroll it
        if (musapId == null) {
            MusapClient.enrolLDataWithLink(LINK_URL, new MusapCallback<MusapLink>() {
                @Override
                public void onSuccess(MusapLink link) {
                    MLog.d("Enrolled data");
                }

                @Override
                public void onException(MusapException e) {
                    MLog.e("Failed to enroll", e);
                }
            });
        }

    }
}
