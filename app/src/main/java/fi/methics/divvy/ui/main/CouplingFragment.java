package fi.methics.divvy.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import fi.methics.divvy.R;
import fi.methics.divvy.app.DivvyApp;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.api.MusapException;
import fi.methics.musap.sdk.internal.datatype.RelyingParty;
import fi.methics.musap.sdk.internal.util.MLog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CouplingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CouplingFragment extends Fragment {

    public CouplingFragment() {
        // Required empty public constructor
    }

    public static CouplingFragment newInstance() {
        CouplingFragment fragment = new CouplingFragment();
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
        View v = inflater.inflate(R.layout.fragment_coupling, container, false);

        Button b = v.findViewById(R.id.button_couple);
        b.setOnClickListener(view -> {
            TextView textView = v.findViewById(R.id.text_coupling_code);
            String couplingCode = textView.getText().toString();

            MusapCallback<RelyingParty> callback =  new MusapCallback<RelyingParty>() {
                @Override
                public void onSuccess(RelyingParty rp) {
                    if (rp != null) {
                        Toast.makeText(CouplingFragment.this.getContext(), "Coupling successful", Toast.LENGTH_SHORT).show();

                        // TODO: Probably not the best way to navigate between fragments...
                        CouplingFragment.this.getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, CouplingCompleteFragment.newInstance())
                            .commitNow();
                    } else {
                        Toast.makeText(CouplingFragment.this.getContext(), "Coupling failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onException(MusapException e) {
                    Toast.makeText(CouplingFragment.this.getContext(), "Coupling failed", Toast.LENGTH_SHORT).show();
                }
            };

            MLog.d("Coupling code=" + couplingCode);
            try {
                MusapClient.coupleWithLink(DivvyApp.LINK_URL, couplingCode, callback);
            } catch (Exception e) {
                MLog.e("Failed", e);
            }
        });

        return v;
    }
}