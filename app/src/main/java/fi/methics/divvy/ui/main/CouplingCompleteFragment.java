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
import fi.methics.divvy.util.PollCallback;
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
        b.setOnClickListener(view -> MusapClient.pollLink(DivvyApp.LINK_URL, new PollCallback(this.getActivity())));

        return v;
    }

}