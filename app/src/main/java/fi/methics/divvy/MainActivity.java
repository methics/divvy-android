package fi.methics.divvy;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import fi.methics.divvy.app.DivvyApp;
import fi.methics.divvy.ui.main.CouplingCompleteFragment;
import fi.methics.divvy.ui.main.CouplingFragment;
import fi.methics.divvy.ui.main.KeygenFragment;
import fi.methics.divvy.ui.main.MainFragment;
import fi.methics.divvy.util.PollCallback;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.api.MusapException;
import fi.methics.musap.sdk.internal.datatype.MusapLink;
import fi.methics.musap.sdk.internal.util.MLog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.askNotificationPermission();

        FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                MLog.e("Fetching FCM registration token failed", task.getException());
                enrollMusapLink(null);
                return;
            }
            // Get new FCM registration token
            String token = task.getResult();
            MLog.d("Token: " + token);
            enrollMusapLink(token);
        });

        if (savedInstanceState == null) {
            MLog.d("Parties" + MusapClient.listRelyingParties());

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

    @Override
    protected void onResume() {
        super.onResume();
        MusapClient.pollLink(new PollCallback(this));
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

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    MLog.d("Push notification permission granted");
                } else {
                    MLog.d("Push notification permission not granted");
                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void enrollMusapLink(String fcmToken) {

        String musapId = MusapClient.getMusapId();
        // If MUSAP is not enrolled, enroll it
        if (musapId == null) {
            MLog.d("Enrolling to MUSAP Link");
            MusapClient.enableLink(DivvyApp.LINK_URL, fcmToken, new MusapCallback<MusapLink>() {
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