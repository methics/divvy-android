package fi.methics.divvy;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import fi.methics.divvy.ui.main.CouplingCompleteFragment;
import fi.methics.divvy.ui.main.CouplingFragment;
import fi.methics.divvy.ui.main.KeygenFragment;
import fi.methics.divvy.ui.main.MainFragment;
import fi.methics.musap.sdk.api.MusapCallback;
import fi.methics.musap.sdk.api.MusapClient;
import fi.methics.musap.sdk.internal.util.MLog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
//        this.askNotificationPermission();
//
//        this.getToken();
//
//        int checkExistence = this.getResources().getIdentifier("google_api_key", "string", this.getPackageName());
//        MLog.d("Exists=" + checkExistence);
    }

//    private final ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    // FCM SDK (and your app) can post notifications.
//                    MLog.d("Granted");
//                } else {
//                    MLog.d("Not granted");
//                }
//            });
//
//
//    private void askNotificationPermission() {
//        // This is only necessary for API level >= 33 (TIRAMISU)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
//                    PackageManager.PERMISSION_GRANTED) {
//                // FCM SDK (and your app) can post notifications.
//            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
//                // TODO: display an educational UI explaining to the user the features that will be enabled
//                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
//                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
//                //       If the user selects "No thanks," allow the user to continue without notifications.
//            } else {
//                // Directly ask for the permission
//                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
//            }
//        }
//    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = "Got token " + token;
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}