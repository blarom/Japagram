package com.japagram.resources;

import android.app.Application;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainApplication extends Application {

    //private static final boolean ALLOW_USE_OF_LEAK_CANARY = false;

    @Override public void onCreate() {
        super.onCreate();

        //activateLeakCanary();
        //activateBridgeAndIcePick();
        activateFirebase();

    }
    @Override protected void attachBaseContext(Context base) {
        //Inspired by: https://gunhansancar.com/change-language-programmatically-in-android/
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }


    /*
    private RefWatcher refWatcher;
    public static RefWatcher getRefWatcher(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();
        return application.refWatcher;
    }
    private void activateLeakCanary() {

        if (!ALLOW_USE_OF_LEAK_CANARY) return;

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Normal app init code...
    }
    */
    /*
    private void activateBridgeAndIcePick() {

        Bridge.initialize(getApplicationContext(), new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                Icepick.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                Icepick.restoreInstanceState(target, state);
            }
        });
    }
    */
    private void activateFirebase() {

        /* Enable disk persistence  */
        FirebaseDatabase database = UtilitiesDb.getDatabase();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signInWithEmailAndPassword(Globals.firebaseEmail, Globals.firebasePass);

        /*Note: two ways are demonstrated here to set the persistence of Firebase without problems: as a singleton (Utilities) or using an activity that loads before all others*/
    }
}