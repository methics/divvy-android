package fi.methics.divvy;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import fi.methics.divvy.ui.main.CouplingCompleteFragment;
import fi.methics.divvy.ui.main.CouplingFragment;
import fi.methics.divvy.ui.main.KeygenFragment;
import fi.methics.divvy.ui.main.MainFragment;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // If coupling is not done, show coupling fragment
            if (MusapClient.listRelyingParties() == null || MusapClient.listRelyingParties().isEmpty()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, CouplingFragment.newInstance())
                        .commitNow();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, CouplingCompleteFragment.newInstance())
                        .commitNow();
            }

            // Horrible way of implementing back button press on fragments
            getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Determine which is the "home" fragment
                    boolean couplingDone = MusapClient.listRelyingParties() != null && !MusapClient.listRelyingParties().isEmpty();
                    Class<?> homeClass = couplingDone ? CouplingCompleteFragment.class : CouplingFragment.class;

                    // We are in home fragment, back button should finish
                    if (getVisibleFragment().getClass().equals(homeClass)) {
                        finish();
                    } else {
                        // We are not in a home fragment, navigate back there.
                        if (couplingDone) {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in,  // enter
                                            R.anim.fade_out,  // exit
                                            R.anim.fade_in,   // popEnter
                                            R.anim.slide_out  // popExit
                                    )
                                    .replace(R.id.container, CouplingCompleteFragment.newInstance())
                                    .commitNow();
                        } else {
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in,  // enter
                                            R.anim.fade_out,  // exit
                                            R.anim.fade_in,   // popEnter
                                            R.anim.slide_out  // popExit
                                    )
                                    .replace(R.id.container, CouplingFragment.newInstance())
                                    .commitNow();
                        }
                    }
                }
            });
        }
    }
    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }
}