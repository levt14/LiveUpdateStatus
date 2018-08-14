package com.liveupdatestatus;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_PHOTO_CAPTURE = 1;
    private static final int REQUEST_PHOTO_PICK = 2;

    private ImageView mUserImageView;
    private EditText mUserNameEditText;
    private Button mUpdateButton;

    private String passedUserId;
    private DatabaseReference mUsersDB;
    private FirebaseUser currentUser;
    private Uri mPhotoUri;
    private StorageReference mStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUserImageView = findViewById(R.id.userImageViewProfile);
        mUserNameEditText = findViewById(R.id.userNameEditTextProfile);
        mUpdateButton = findViewById(R.id.updateProfileButton);

        passedUserId = getIntent().getStringExtra("USER_ID");
        mUsersDB = FirebaseDatabase.getInstance().getReference().child("Users");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();

        mUsersDB.child(passedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("displayName").getValue(String.class);
                String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);

                mUserNameEditText.setText(name);
                try {
                    Picasso.with(ProfileActivity.this).load(photoUrl).placeholder(R.drawable.com_facebook_profile_picture_blank_square).into(mUserImageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!passedUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            mUserImageView.setEnabled(false);
            mUserNameEditText.setFocusable(false);
            mUpdateButton.setVisibility(View.GONE);
        }
        else {
            //if it is the user's profile
            mUserImageView.setEnabled(true);
            mUserNameEditText.setFocusable(true);
            mUpdateButton.setVisibility(View.VISIBLE);

        }

        //listen to imageView click
        mUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setMessage("How would you like to add your photo?");

                builder.setPositiveButton("Take photo", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //take photo
                        dispatchTakePhotoIntent();
                    }
                });
                builder.setNegativeButton("Choose photo", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Choose photo
                        dispatchChoosePhotoIntent();
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.dismiss();
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();


                // set the buttons to the center of the screen
                final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

                LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                LinearLayout.LayoutParams negativeButtonLL = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();
                LinearLayout.LayoutParams neutralButtonLL = (LinearLayout.LayoutParams) neutralButton.getLayoutParams();

                positiveButtonLL.gravity = Gravity.CENTER;
                negativeButtonLL.gravity = Gravity.CENTER;
                neutralButtonLL.gravity = Gravity.CENTER;

                positiveButton.setLayoutParams(positiveButtonLL);
                negativeButton.setLayoutParams(negativeButtonLL);
                neutralButton.setLayoutParams(neutralButtonLL);

            }
        });
        //listen to update button click
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUserName = mUserNameEditText.getText().toString().trim();

                if(TextUtils.isEmpty(newUserName)) {
                    Toast.makeText(getApplicationContext(), "Can't update empty name", Toast.LENGTH_SHORT).show();
                }
                else {
                    updateUserName(newUserName);

                    if(mPhotoUri != null) {
                        updateUserPhoto(mPhotoUri);
                    }
                }

            }
        });
        //will show the name in the authentication
//        Toast.makeText(getApplicationContext(), currentUser.getDisplayName(), Toast.LENGTH_SHORT).show();

    }
    private void updateUserName(final String newUserName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUserName)
                .build();
        //updating in the authentication
        currentUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //updating in database
                Map<String, Object> updateUserNameMap = new HashMap<>();
                updateUserNameMap.put("displayName",newUserName);
                mUsersDB.child(passedUserId).updateChildren(updateUserNameMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "You have successfully updated your name", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private void updateUserPhoto(Uri newPhotoUri) {
        StorageReference userImageRef = mStorage.child("userImages").child(currentUser.getUid())
                .child(newPhotoUri.getLastPathSegment());
        userImageRef.putFile(newPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

//              Uri uploadedImageUri = task.getResult().getDownloadUrl();
//
                Task<Uri> uploadedImageUri = task.getResult().getMetadata().getReference().getDownloadUrl();

                uploadedImageUri.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri uploadedImageUri = task.getResult();




                        Map<String, Object> updatePhotoMap = new HashMap<>();
                        updatePhotoMap.put("photoUrl", uploadedImageUri.toString());
                        mUsersDB.child(currentUser.getUid()).updateChildren(updatePhotoMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfileActivity.this, "You have successfully updated your photo", Toast.LENGTH_SHORT).show();
                            }
                        });




                    }
                });
//                Log.i("url:",uploadedImageUri.getResult().toString());

//                UploadTask.TaskSnapshot uploadedImageUri = task.getResult();

//                Uri uploadedImageUri = task.getResult().getUploadSessionUri();






            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void dispatchTakePhotoIntent() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_PHOTO_CAPTURE);
        }
        else  {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePhotoIntent, REQUEST_PHOTO_CAPTURE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void dispatchChoosePhotoIntent() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PHOTO_PICK);
        }
        else {
            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK);
            choosePhotoIntent.setType("image/*");
            startActivityForResult(choosePhotoIntent, REQUEST_PHOTO_PICK);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PHOTO_CAPTURE && resultCode == RESULT_OK) {
            //success taking photo
            mPhotoUri = data.getData();
            mUserImageView.setImageURI(mPhotoUri);
        }
        else if(requestCode == REQUEST_PHOTO_PICK && resultCode == RESULT_OK) {
            //success choosing photo
            mPhotoUri = data.getData();
            mUserImageView.setImageURI(mPhotoUri);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHOTO_CAPTURE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
//                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    dispatchTakePhotoIntent();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case REQUEST_PHOTO_PICK : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
//                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    dispatchChoosePhotoIntent();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
