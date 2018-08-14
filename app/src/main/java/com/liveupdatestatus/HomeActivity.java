package com.liveupdatestatus;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.liveupdatestatus.model.Status;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mStatusDB;
    private DatabaseReference mUserDB;
    private RecyclerView mRecyclerView;

    private void goToLoginActivity() {
        finish();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

    private void goToPostActivity() {
        startActivity(new Intent(HomeActivity.this, PostActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu:
                mAuth.signOut();
                goToLoginActivity();
                return true;
            case R.id.addNewMenu:
                goToPostActivity();
                return true;
            case R.id.myProfileMenu:
                Intent goToProfile = new Intent(HomeActivity.this, ProfileActivity.class);
                goToProfile.putExtra("USER_ID", mAuth.getCurrentUser().getUid());
                startActivity(goToProfile);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuth.getCurrentUser() == null) {
            goToLoginActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null) {
            goToLoginActivity();
        }
        setContentView(R.layout.activity_home);

        mStatusDB = FirebaseDatabase.getInstance().getReference().child("Status");
        mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.homeRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //take the latest data to RecyclerView
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = mStatusDB;
        FirebaseRecyclerOptions<Status> options = new FirebaseRecyclerOptions.Builder<Status>()
                .setQuery(query, Status.class)
                .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Status, StatusViewHolder>(options) {


                    @NonNull
                    @Override
                    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater
                                .from(parent.getContext())
                                .inflate(R.layout.status_row, parent, false);

                        return new StatusViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final StatusViewHolder holder, int position, @NonNull final Status model) {
//                        holder.setUserName(model.getUserId());
                        holder.setUserStatus(model.getUserStatus());

                        //query the user with the model id which is the row's user id
                        mUserDB.child(model.getUserId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String userName = dataSnapshot.child("displayName").getValue(String.class);
                                String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);

                                holder.setUserName(userName);

                                try {
                                    holder.setUserPhotoUrl(getApplicationContext(), photoUrl);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //listen to image button clicks
                        holder.userImageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //go to ProfileActivity
                                Intent goToProfile = new Intent(HomeActivity.this, ProfileActivity.class);
                                goToProfile.putExtra("USER_ID", model.getUserId());
                                startActivity(goToProfile);
                            }
                        });
                    }
                };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {

        View view;
        public ImageButton userImageButton;

        public StatusViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            userImageButton = view.findViewById(R.id.userImageButton);
        }

        public void setUserPhotoUrl(Context context, String imageUrl) {
            ImageButton userImageButton = view.findViewById(R.id.userImageButton);
            Picasso.with(context).load(imageUrl).placeholder(R.mipmap.ic_launcher).into(userImageButton);

        }

        public void setUserName(String name) {
            TextView userNameTextView = view.findViewById(R.id.userNameTextView);
            userNameTextView.setText(name);
        }

        public void setUserStatus(String status) {
            TextView userStatusTextView = view.findViewById(R.id.userStatusTextView);
            userStatusTextView.setText(status);
        }
    }
}
