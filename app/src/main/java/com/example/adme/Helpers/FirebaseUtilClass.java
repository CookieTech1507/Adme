package com.example.adme.Helpers;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtilClass {


    private static final String TAG = "FirebaseUtilClass";

    public static final String LOCATION = "langLat";
    public static final String CURRENT_USER_ID = "current_user";
    public static final String USER_COLLECTION_ID ="Adme_User";
    public static final String USER_MAIN_DATA_COLLECTION_NAME = "Main_Data";
    public static final String SERVICE_PROVIDER_DOCUMENT_NAME = "Service_Provider_Data";

    public static final String MODE_CLIENT = "Client";
    public static final String MODE_SERVICE_PROVIDER = "Service provider";
    public static final String STATUS_ONLINE = "Online";
    public static final String  STATUS_OFFLINE = "Offline";
    public static final String ENTRY_MONTHLY_SUBSCRIPTION_PAID = "Paid";

    public static final String ENTRY_APPOINTMENTS_TODAY = "appointments_today";
    public static final String ENTRY_PENDING_TODAY = "pending today";
    public static final String ENTRY_COMPLETED_TODAY = "completed_today";
    public static final String ENTRY_INCOME_TODAY = "income_today";
    public static final String ENTRY_INCOME_TOTAL = "income_total";
    public static final String ENTRY_DUE = "due";
    public static final String ENTRY_MONTHLY_SUBSCRIPTION = "monthly_subscription";
    public static final String ENTRY_SERVICE_REFERENCE = "service_reference";

    public static final String ENTRY_PHONE_NO_ONE = "phone_no_one";
    public static final String ENTRY_PHONE_NO_TWO = "phone_no_two";
    public static final String ENTRY_PHONE_NO_ONE_PRIVACY = "privacy_one";
    public static final String ENTRY_PHONE_NO_TWO_PRIVACY = "privacy_two";
    public static final String ENTRY_PHONE_NO_PRIVACY_PUBLIC = "Public";
    public static final String ENTRY_PHONE_NO_PRIVACY_PRIVATE = "Private";


    public static final String ENTRY_LOCATION_DISPLAY_NAME = "display_name";
    public static final String ENTRY_LOCATION_ADDRESS = "address";
    public static final String ENTRY_LOCATION_LATITUDE = "latitude";
    public static final String ENTRY_LOCATION_LONGITUDE = "longitude";

    public static final String ENTRY_SERVICE_TITLE = "service_title";
    public static final String ENTRY_SERVICE_DESCRIPTION = "service_description";
    public static final String ENTRY_SERVICE_PRICE = "service_price";


    //create database reference
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection(USER_COLLECTION_ID);
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private User expectedUser;



    public void createUser(FirebaseUser user,CreateUserCommunicator communicator){
        userRef.document(user.getUid()).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document = task1.getResult();


                if (document.exists()) {
                    User current_user = document.toObject(User.class);
                    // User is already exist in database
                    //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    communicator.userAlreadyExists(current_user);
                } else {
                    // User hasn't created yet
                    // create new user in database
                    Log.d(TAG, "No such document");
                    String username;
                    String email = null;
                    String NULL = "";
                    assert user != null;
                    if (user.getDisplayName() != null){
                        username = user.getDisplayName();
                    }
                    else{
                        username = "Adme_User";
                    }
                    if(user.getEmail() != null){
                        email = user.getEmail();
                    }

                    String joined = String.valueOf(user.getMetadata().getCreationTimestamp());
                    String user_id = user.getUid();
                    User new_user = new User(username,email,joined,user_id);
                    /*** Insert into fireStore database**/
                    userRef.document(user.getUid()).set(new_user).addOnSuccessListener(aVoid -> {
                        userRef.document(user.getUid()).collection(FirebaseUtilClass.USER_MAIN_DATA_COLLECTION_NAME).document(FirebaseUtilClass.SERVICE_PROVIDER_DOCUMENT_NAME).set(new ServiceProviderData()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: successfully created user");
                                communicator.onUserCreatedSuccessfully(new_user);
                            }
                        });

                    });
                }
            } else {
                Log.d(TAG, "get failed with ", task1.getException());
            }

        });
    }

    public void updateUserLocation(User user,MyPlaces place,UpdateLocationInfoCommunicator communicator) {
        Map<String,String> location = user.getLocation();
        location.put(ENTRY_LOCATION_LATITUDE,place.getLatitude());
        location.put(ENTRY_LOCATION_LONGITUDE,place.getLongitude());
        location.put(ENTRY_LOCATION_DISPLAY_NAME,place.getName());
        location.put(ENTRY_LOCATION_ADDRESS,place.getFormattedAddress());
        user.setLocation(location);

        userRef.document(user.getmUserId()).set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: location info updated");
                communicator.onLocationInfoUpdated(user);
            }
        });
    }

    public interface UpdateLocationInfoCommunicator{
        void onLocationInfoUpdated(User user);
    }



    public interface CreateUserCommunicator{
        void userAlreadyExists(User user);
        void onUserCreatedSuccessfully(User user);
    }

}
