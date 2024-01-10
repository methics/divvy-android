package fi.methics.divvy.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import fi.methics.divvy.R;
import fi.methics.divvy.app.DivvyApp;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.api.MusapException;
import fi.methics.musap.sdk.internal.datatype.MusapKey;
import fi.methics.musap.sdk.internal.datatype.coupling.PollResponsePayload;
import fi.methics.musap.sdk.internal.util.MLog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CouplingCompleteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CouplingCompleteFragment extends Fragment {

    public CouplingCompleteFragment() {
        // Required empty public constructor
    }

    public static CouplingCompleteFragment newInstance() {
        CouplingCompleteFragment fragment = new CouplingCompleteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coupling_complete, container, false);

        Button b = v.findViewById(R.id.button_poll);
        b.setOnClickListener(view -> {
                MusapCallback<PollResponsePayload> callback = new MusapCallback<PollResponsePayload>() {
                    @Override
                    public void onSuccess(PollResponsePayload pollResp) {
                        MLog.d("Got payload " + pollResp);

                        if (pollResp != null)  {

                            boolean shouldGenerate = MusapClient.listKeys().isEmpty() || !"sign".equals(pollResp.getMode());

                            if (shouldGenerate) {
                                MLog.d("Generating keys");
                                FragmentActivity activity = CouplingCompleteFragment.this.getActivity();
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
                                FragmentActivity activity = CouplingCompleteFragment.this.getActivity();
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
                };

                MusapClient.pollLink(DivvyApp.LINK_URL, callback);
        });

        return v;
    }

}