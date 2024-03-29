package fi.methics.divvy.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import fi.methics.divvy.R;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.api.MusapException;
import fi.methics.musap.sdk.internal.datatype.KeyAlgorithm;
import fi.methics.musap.sdk.internal.datatype.MusapKey;
import fi.methics.musap.sdk.internal.datatype.coupling.PollResponsePayload;
import fi.methics.musap.sdk.internal.keygeneration.KeyGenReq;
import fi.methics.musap.sdk.internal.util.MLog;
import fi.methics.musap.sdk.internal.util.MusapSscd;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KeygenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeygenFragment extends Fragment {

    private static final String SIG_REQ = "sigreq";

    private PollResponsePayload pollResp;

    public KeygenFragment() {
        // Required empty public constructor
    }

    public static KeygenFragment newInstance(PollResponsePayload payload) {
        KeygenFragment fragment = new KeygenFragment();
        Bundle args = new Bundle();
        String sigReqJson = new Gson().toJson(payload);
        args.putString(SIG_REQ, sigReqJson);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String sigReqJson = getArguments().getString(SIG_REQ);
            MLog.d("Got signature payload " + sigReqJson);
            this.pollResp = new Gson().fromJson(sigReqJson, PollResponsePayload.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_keygen, container, false);

        Button b = v.findViewById(R.id.button_generate);

        b.setOnClickListener(view -> {
            KeyGenReq req;
            if (pollResp.shouldGenerateKey()) {
                req = pollResp.toKeygenReq();
                if (req.getAlgorithm() == null) {
                    Log.d("keygen", "Using default algorithm");
                    req.setKeyAlgorithm(KeyAlgorithm.ECC_ED25519);
                }  else {
                    Log.d("keygen", "Req algorithm " + req.getAlgorithm().toString());
                }
                req.setActivity(this.getActivity());
                req.setView(this.getView());
            } else {
                Log.d("keygen", "Creating key gen req");
                req = new KeyGenReq.Builder()
                        .setActivity(this.getActivity())
                        .setView(this.getView())
                        .setKeyAlias("Yubico")
                        .setKeyAlgorithm(KeyAlgorithm.ECC_ED25519)
                        .createKeyGenReq();
            }


            MusapSscd sscd = MusapClient.listEnabledSscds().get(0);
            MusapClient.generateKey(sscd, req, new MusapCallback<MusapKey>() {
                @Override
                public void onSuccess(MusapKey musapKey) {
                    if (pollResp == null) {
                        KeygenFragment.this.requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, CouplingCompleteFragment.newInstance())
                                .commitNow();
                    } else {
                        MLog.d("Musap key=" + new Gson().toJson(musapKey));
                        MLog.d("Payload=" + new Gson().toJson(pollResp));

                        // Key attestation is an optional feature here, so it should not fail anything
                        try {
                            MLog.d("Attestation=" + sscd.getKeyAttestation().getAttestationData(musapKey).toJson());
                        } catch (Exception e) {
                            MLog.d("Invalid attestation", e);
                        }

                        if (pollResp.shouldSign()) {
                            // Go to signing view
                            try {
                                KeygenFragment.this.requireActivity().getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(
                                                R.anim.slide_in,  // enter
                                                R.anim.fade_out,  // exit
                                                R.anim.fade_in,   // popEnter
                                                R.anim.slide_out  // popExit
                                        )
                                        .replace(R.id.container, SignatureFragment.newInstance(pollResp.toSignatureReq(musapKey)))
                                        .commitNow();
                            } catch (Exception e) {
                                Log.e("poll", "Failed to sign", e);
                                Toast.makeText(KeygenFragment.this.getActivity(), "Failed to start signing", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Go to home view
                            MusapClient.sendKeygenCallback(musapKey, pollResp.getTransId());
                            KeygenFragment.this.requireActivity().getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in,  // enter
                                            R.anim.fade_out,  // exit
                                            R.anim.fade_in,   // popEnter
                                            R.anim.slide_out  // popExit
                                    )
                                    .replace(R.id.container, CouplingCompleteFragment.newInstance())
                                    .commitNow();
                        }
                    }
                }

                @Override
                public void onException(MusapException e) {
                    Toast.makeText(KeygenFragment.this.getActivity(), "Key generation failed", Toast.LENGTH_SHORT).show();
                    MLog.e("Failed keygen", e);
                }
            });
        });

        return v;
    }
}