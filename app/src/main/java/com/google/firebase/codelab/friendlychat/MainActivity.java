package com.google.firebase.codelab.friendlychat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {
    static final int SIGN_IN_REQUEST = 1;
    final ArrayList<Beacon> tempList = new ArrayList<>();
    FirebaseUser user;
    private DrawerLayout drawer;

    private static final String TAG = "MainActivityTag";
    private String mUsername;
    //    private String mPhotoUrl;
    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MessagesFragment()).commit();

        createBeacons();

        firebaseSetup();

        mAuthStateListener.onAuthStateChanged(FirebaseAuth.getInstance());

        user = mFirebaseAuth.getCurrentUser();
        View otherView = getLayoutInflater().inflate(R.layout.nav_header, null);
//        TextView username = otherView.findViewById(R.id.screenName);
//        username.setText(user.getDisplayName());
    }

    private void firebaseSetup() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setupUserSignedIn(user);
                } else {
                    Intent intent = AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()
                                    )
                            ).build();
                    startActivityForResult(intent, SIGN_IN_REQUEST);
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult: Sign in SUCCEEDED");
                user = mFirebaseAuth.getCurrentUser();
                setupUserSignedIn(user);
                View otherView = getLayoutInflater().inflate(R.layout.nav_header, null);
                TextView username = otherView.findViewById(R.id.screenName);
                username.setText("");
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: Sign in FAILED");
                finish();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment newFrag;
        switch (menuItem.getItemId()) {
            case R.id.nav_message:
                newFrag = new MessagesFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        newFrag).commit();
                break;
            case R.id.nav_location:
                newFrag = new LocationFragment();
                getIntent().putExtra("beacons", tempList);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        newFrag).commit();
                break;
            case R.id.nav_feedback:
                newFrag = new FeedbackFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        newFrag).commit();
                break;
            case R.id.sign_out:
                AuthUI.getInstance().signOut(this);
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = null;
                Intent intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()
                                )
                        ).build();
                startActivityForResult(intent, SIGN_IN_REQUEST);
                break;
            default:

        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void createBeacons() {
        Beacon hemmingson = new Beacon("Hemmingson",
                "The hub of Gonzaga University's campus",
                47.667136,
                -117.399103);
        Beacon herak = new Beacon("Herak",
                "The primary building of Gonzaga University's School of Engineering",
                47.666745,
                -117.402179);
        Beacon paccar = new Beacon("Paccar",
                "The primary building of Gonzaga University's School of Applied Science",
                47.666367,
                -117.402161);
        Beacon jundt = new Beacon("Jundt",
                "The art museum building on Gonzaga University's campus",
                47.666363,
                -117.406914);
        Beacon arcOfSpokane = new Beacon("The Arc of Spokane",
                "A consignment store for the Spokane community",
                47.665083,
                -117.409781);
        Beacon Dooley = new Beacon("Dooley",
                "Gonzaga University residence hall",
                47.669187,
                -117.405437);
        tempList.add(hemmingson);
        tempList.add(herak);
        tempList.add(paccar);
        tempList.add(jundt);
        tempList.add(arcOfSpokane);
        tempList.add(Dooley);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void setupUserSignedIn(FirebaseUser user) {
        mUsername = user.getDisplayName();

    }
}