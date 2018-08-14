package com.liveupdatestatus;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liveupdatestatus.model.Status;

public class PostActivity extends AppCompatActivity {

    private EditText mStatusEditText;
    private Button mPostButton;

    private DatabaseReference mStatusDB;
    private ProgressDialog mDialog;



    public void post(View view) {
        String status = mStatusEditText.getText().toString();

        if(TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please write something.", Toast.LENGTH_SHORT).show();
        }
        else {
            //proceed

            mDialog.setMessage("Please wait...");
            mDialog.show();
            mDialog.setCancelable(false);

            postStatusToFirebase(FirebaseAuth.getInstance().getCurrentUser().getUid(), status);
        }

    }

    private void postStatusToFirebase(String userId, String userStatus) {
        //creation of an unic id
        Status status = new Status(userId, userStatus);
        mStatusDB.push().setValue(status).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mStatusEditText.setText(null);
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Your status has successfully posted.", Toast.LENGTH_LONG).show();
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mStatusEditText = findViewById(R.id.postEditText);
        mPostButton = findViewById(R.id.postButton);

        mStatusDB = FirebaseDatabase.getInstance().getReference().child("Status");
        mDialog = new ProgressDialog(this);

    }
}
