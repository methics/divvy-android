package fi.methics.divvy.util;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import fi.methics.divvy.R;
import fi.methics.divvy.ui.main.CouplingCompleteFragment;
import fi.methics.divvy.ui.main.KeygenFragment;
import fi.methics.divvy.ui.main.SignatureFragment;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.api.MusapException;
import fi.methics.musap.sdk.internal.datatype.MusapKey;
import fi.methics.musap.sdk.internal.datatype.coupling.PollResponsePayload;
import fi.methics.musap.sdk.internal.util.MLog;

public class PollCallback implements MusapCallback<PollResponsePayload> {

    private FragmentActivity activity;

    public PollCallback(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onSuccess(PollResponsePayload pollResp) {
        MLog.d("Got payload " + pollResp);

        if (pollResp != null) {
            boolean shouldGenerate = MusapClient.listKeys().isEmpty() || pollResp.shouldGenerateKey();

            if (shouldGenerate) {
                MLog.d("Generating keys");
                FragmentActivity activity = this.activity;
                if (activity != null) {
                    activity.getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in,  // enter
                                    R.anim.fade_out,  // exit
                                    R.anim.fade_in,   // popEnter
                                    R.anim.slide_out  // popExit
                            )
                            .replace(R.id.container, KeygenFragment.newInstance(pollResp))
                            .commitNow();
                }
            } else {
                MLog.d("Found a key");
                String keyId = pollResp.getKeyId();
                MusapKey key;
                if (keyId != null) {
                    Log.d("poll", "Using key id "+ keyId);
                    key = MusapClient.getKeyByKeyID(keyId);
                    if (key == null) {
                        Log.d("poll", "No key for " + keyId);
                        Toast.makeText(activity, "Unknown key", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    MLog.d("Using default key");
                    // TODO: User should choose key.
                    key = MusapClient.listKeys().get(0);
                }

                FragmentActivity activity = this.activity;
                if (activity != null) {
                    try {
                        activity.getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.slide_in,  // enter
                                        R.anim.fade_out,  // exit
                                        R.anim.fade_in,   // popEnter
                                        R.anim.slide_out  // popExit
                                )
                                .replace(R.id.container, SignatureFragment.newInstance(pollResp.toSignatureReq(key)))
                                .commitNow();
                    } catch (MusapException e) {
                        Log.e("poll", "Failed to sign", e);
                        throw new RuntimeException(e);
                    }
                }

            }
        }
    }

    @Override
    public void onException(MusapException e) {
        MLog.e("Failed", e);
    }

}
