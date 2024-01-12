package fi.methics.divvy.util;

import android.app.Activity;

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

            boolean shouldGenerate = MusapClient.listKeys().isEmpty() || !"sign".equals(pollResp.getMode());

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
                // TODO: We only support 1 key atm
                MusapKey key = MusapClient.listKeys().get(0);
                FragmentActivity activity = this.activity;
                if (activity != null) {
                    // TODO: Probably not the best way to navigate between fragments...
                    activity.getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in,  // enter
                                    R.anim.fade_out,  // exit
                                    R.anim.fade_in,   // popEnter
                                    R.anim.slide_out  // popExit
                            )
                            .replace(R.id.container, SignatureFragment.newInstance(pollResp.toSignatureReq(key)))
                            .commitNow();
                }

            }
        }
    }

    @Override
    public void onException(MusapException e) {
        MLog.e("Failed", e);
    }

}
