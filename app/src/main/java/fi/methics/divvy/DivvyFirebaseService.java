package fi.methics.divvy;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import fi.methics.musap.sdk.internal.util.MLog;

public class DivvyFirebaseService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        MLog.d("New token", token);
    }

}
