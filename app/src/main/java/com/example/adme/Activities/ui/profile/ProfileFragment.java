package com.example.adme.Activities.ui.profile;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.adme.Activities.LandingActivity;
import com.example.adme.Activities.LoginActivity;
import com.example.adme.Activities.MainActivity;
import com.example.adme.Activities.RegistrationActivity;
import com.example.adme.Architecture.FirebaseUtilClass;
import com.example.adme.Helpers.CookieTechUtilityClass;
import com.example.adme.Helpers.User;
import com.example.adme.R;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.Observable;
import androidx.lifecycle.Observer;


public class ProfileFragment extends Fragment {

    private ProfileViewModel mViewModel;
    private CardView cardLogout, cardContacts, cardPrivacySettings, cardNotification, cardAccount, cardChangeMode, cardHelp;
    private GoogleSignInClient mGoogleSignInClient;
    private ConstraintLayout ratingHolder;

    Button editProfileBtn;
    TextView txtProfileName,txtSinceTime;
    EditText editProfileName;

    private User mCurrentUser;
    private String isClient;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();




    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeFields(root);


        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        cardContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Contacts.class));
            }
        });

        cardPrivacySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PrivacySettings.class));
            }
        });
        cardNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NotificationSettings.class));
            }
        });
        cardAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AccountSubscriptions.class));
            }
        });
        cardChangeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ChangeMode.class));
            }
        });
        cardHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Helps.class));
            }
        });

        cardLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
            if(account != null){
                /*user already signed in*/
                googleSignOut();

            }

            else if (isLoggedIn){
                facebookSignOut();
            }

            else {
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }



        });

        return  root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //mCurrentUser = ((LandingActivity)requireActivity()).getmCurrentUser();
        //if(mCurrentUser == null)Log.e("null user:","current user seems null");
        //isClient = ((LandingActivity)requireActivity()).isClient();
        //updateUiData();
    }



    private void initializeFields(View root) {

        editProfileBtn = root.findViewById(R.id.editProfileBtn);
        txtProfileName = root.findViewById(R.id.profileName);
        txtSinceTime = root.findViewById(R.id.sinceTime);

        cardContacts = root.findViewById(R.id.cardContacts);
        cardPrivacySettings = root.findViewById(R.id.cardPrivacySettings);
        cardNotification = root.findViewById(R.id.cardNotification);
        cardAccount = root.findViewById(R.id.cardAccount);
        cardChangeMode = root.findViewById(R.id.cardChangeMode);
        cardLogout = root.findViewById(R.id.cardLogout);
        cardHelp = root.findViewById(R.id.cardHelp);
        ratingHolder = root.findViewById(R.id.profile_rating_holder);

        //For Google SignUp
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    private void googleSignOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //mAuth.signOut();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                    }
                });
    }

    private void facebookSignOut(){
        LoginManager.getInstance().logOut();
        //mAuth.signOut();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel

        final Observer<User> userDataObserver = new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mCurrentUser = user;
                Log.d("view-model", "onChanged:  bottom details" + user.getStatus());
                updateUiData();
            }
        };

        mViewModel.getUserData().observe(requireActivity(),userDataObserver);

    }



    private void updateUiData() {
        //txtProfileName.setText(mCurrentUser.getUsername());
        if(mAuth.getCurrentUser() != null){
            long timeStamp = mAuth.getCurrentUser().getMetadata().getCreationTimestamp();
            String date = CookieTechUtilityClass.getDate(timeStamp);
            txtProfileName.setText(mCurrentUser.getUser_name());
            txtSinceTime.setText("Member since "+ date);
            isClient = mCurrentUser.getMode();
            if(isClient.equals("Client")){
                ratingHolder.setVisibility(View.GONE);
            }

        }





    }




}
