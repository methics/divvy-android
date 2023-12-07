package fi.methics.divvy.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import fi.methics.divvy.R;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.api.MusapException;
import fi.methics.musap.sdk.internal.datatype.MusapSignature;
import fi.methics.musap.sdk.internal.sign.SignatureReq;
import fi.methics.musap.sdk.internal.util.MLog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignatureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignatureFragment extends Fragment {

    private static final String SIG_REQ = "sigreq";

    private long lastClickTime;

    private SignatureReq sigReq;

    public SignatureFragment() {
        // Required empty public constructor
    }

    public static SignatureFragment newInstance(SignatureReq signatureReq) {
        SignatureFragment fragment = new SignatureFragment();
        Bundle args = new Bundle();

        // Convert sig req to string because args doesn't take any objects.
        String sigReqJson = new Gson().toJson(signatureReq);
        args.putString(SIG_REQ, sigReqJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String sigReqJson = getArguments().getString(SIG_REQ);
            MLog.d("Got signature request " + sigReqJson);
            this.sigReq = new Gson().fromJson(sigReqJson, SignatureReq.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_signature, container, false);

        TextView displayText = v.findViewById(R.id.text_display_text);
        displayText.setText(this.sigReq.getDisplayText());

        TextView dtbsText = v.findViewById(R.id.text_dtbs_value);
        dtbsText.setText(Base64.encodeToString(this.sigReq.getData(), Base64.DEFAULT));

        // SDK requires the activity in order to display dialog
        this.sigReq.setActivity(this.getActivity());

        Button cancelButton = v.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignatureFragment.this.finish();
            }
        });

        Button b = v.findViewById(R.id.button_sign);
        b.setOnClickListener(view -> {

            if (SystemClock.elapsedRealtime() - lastClickTime < 500){
                return;
            }
            this.lastClickTime = SystemClock.elapsedRealtime();

            MusapClient.sign(SignatureFragment.this.sigReq, new MusapCallback<MusapSignature>() {
                @Override
                public void onSuccess(MusapSignature musapSignature) {
                    MLog.d("Signed successfully");
                    MusapClient.sendSignatureCallback(musapSignature, sigReq.getTransId());
                    SignatureFragment.this.finish();
                }

                @Override
                public void onException(MusapException e) {
                    MLog.e("Sign failed", e);
                }
            });
        });

        return v;
    }

    private void finish() {
        // Go back to polling fragment
        SignatureFragment.this.getActivity().getSupportFragmentManager().beginTransaction()
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