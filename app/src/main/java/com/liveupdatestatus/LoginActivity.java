package com.liveupdatestatus;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;

    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;


    public void login(View view) {

        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if(TextUtils.isEmpty(email)) {
            showAlertDialog("Error!", "Email cannot be empty.");
        }
        else if(TextUtils.isEmpty(password)) {
            showAlertDialog("Error!", "Password cannot be empty.");
        }
        else {
            //sign in with firebase
            mDialog.setMessage("Please wait...");
            mDialog.show();
            mDialog.setCancelable(false);

            loginViaFirebase(email, password);
        }

    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void loginViaFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.dismiss();

                if(!task.isSuccessful()) {
                    //error registering users
                    Log.i("info", "error Login users");
                    Log.i("info", task.getException().getMessage());

                    showAlertDialog("Error", task.getException().getMessage());
                }
                else {
                    //Login success
                    //take the user to HomeActivity
                    finish();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createAccountMenu:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEditText = findViewById(R.id.emailEditTextLogin);
        mPasswordEditText = findViewById(R.id.passwordEditTextLogin);
        mLoginButton = findViewById(R.id.loginButton);

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);

    }
}
