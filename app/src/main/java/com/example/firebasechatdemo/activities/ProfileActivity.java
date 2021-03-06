package com.example.firebasechatdemo.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.firebasechatdemo.R;
import com.example.firebasechatdemo.utils.PicassoCache;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {
    @Bind(R.id.profile_display_name_tv)
    TextView displayName;
    @Bind(R.id.profile_avatar_iv)
    ImageView avatar;
    @Bind(R.id.profile_status_tv)
    TextView status;
    @Bind(R.id.profile_total_friends_tv)
    TextView totalFriends;
    @Bind(R.id.profile_send_request_btn)
    Button friendRequestBtn;
    @Bind(R.id.profile_decline_request_btn)
    Button declineBtn;
    private String uId = "";
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private String mCurrentState;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        uId = getIntent().getStringExtra("user_id");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentState = "not_friends";
        showLoading();
        declineBtn.setVisibility(View.INVISIBLE);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                displayName.setText(dataSnapshot.child("name").getValue().toString());
                status.setText(dataSnapshot.child("status").getValue().toString());
                PicassoCache.get(ProfileActivity.this)
                        .load(dataSnapshot.child("image").getValue().toString())
                        .placeholder(R.mipmap.default_avatar)
                        .into(avatar);
                progressDialog.dismiss();
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(uId)) {
                                    String reqType = dataSnapshot.child(uId).child("request_type").getValue().toString();
                                    if (reqType.equals("received")) {
                                        mCurrentState = "req_received";
                                        friendRequestBtn.setText("Accept Friend Request");
                                        declineBtn.setVisibility(View.VISIBLE);
                                    } else if (reqType.equals("sent")) {
                                        mCurrentState = "req_sent";
                                        friendRequestBtn.setText("Cancel Friend Request");
                                        declineBtn.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    mFriendDatabase.child(mCurrentUser.getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(uId)) {
                                                        mCurrentState = "friends";
                                                        friendRequestBtn.setText("UnFriend this person");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showLoading() {
        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("please wait while loading user data.");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @OnClick(R.id.profile_send_request_btn)
    void sendRequest() {
        friendRequestBtn.setEnabled(false);
        switch (mCurrentState) {
            case "not_friends":
                DatabaseReference notificationDatabase = mRootRef.child("notifications").child(uId).push();
                String notificationId = notificationDatabase.getKey();
                HashMap<String, String> notificationData = new HashMap<>();
                notificationData.put("from", mCurrentUser.getUid());
                notificationData.put("type", "request");
                Map requestMap = new HashMap<>();
                requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + uId + "/request_type", "sent");
                requestMap.put("Friend_req/" + uId + "/" + mCurrentUser.getUid() + "/request_type", "received");
                requestMap.put("notifications/" + uId + "/" + notificationId, notificationData);
                mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        friendRequestBtn.setEnabled(true);
                        mCurrentState = "req_sent";
                        friendRequestBtn.setText("Cancel Friend Request");
                        declineBtn.setVisibility(View.INVISIBLE);
                        if (databaseError != null)
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                    }
                });
                break;
            case "friends":
                friendRequestBtn.setEnabled(false);
                Map unfriendMap = new HashMap<>();
                unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + uId, null);
                unfriendMap.put("Friends/" + uId + "/" + mCurrentUser.getUid(), null);
                mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        friendRequestBtn.setEnabled(true);
                        mCurrentState = "not_friends";
                        friendRequestBtn.setText("Send Friend Request");
                        declineBtn.setVisibility(View.INVISIBLE);
                        if (databaseError != null)
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                    }
                });
                break;
            case "req_sent":
                friendRequestBtn.setEnabled(false);
                Map cancelRequest = new HashMap<>();
                cancelRequest.put("Friend_req/" + mCurrentUser.getUid() + "/" + uId, null);
                cancelRequest.put("Friend_req/" + uId + "/" + mCurrentUser.getUid(), null);
                mRootRef.updateChildren(cancelRequest, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        friendRequestBtn.setEnabled(true);
                        mCurrentState = "not_friends";
                        friendRequestBtn.setText("Send Friend Request");
                        declineBtn.setVisibility(View.INVISIBLE);
                        if (databaseError != null)
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                    }
                });
                break;
            case "req_received":
                final String currentDate = getCurrentDate();
                Map friendsMap = new HashMap<>();
                friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + uId + "/date", currentDate);
                friendsMap.put("Friends/" + uId + "/" + mCurrentUser.getUid() + "/date", currentDate);
                friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + uId, null);
                friendsMap.put("Friend_req/" + uId + "/" + mCurrentUser.getUid(), null);
                friendsMap.put("notifications/" + uId, null);
                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        friendRequestBtn.setEnabled(true);
                        mCurrentState = "friends";
                        friendRequestBtn.setText("UnFriend this person");
                        declineBtn.setVisibility(View.INVISIBLE);
                        if (databaseError != null)
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                    }
                });
                break;
        }
    }

    @OnClick(R.id.profile_decline_request_btn)
    void declineRequest() {
        friendRequestBtn.setEnabled(false);
        Map unfriendMap = new HashMap<>();
        unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + uId, null);
        unfriendMap.put("Friends/" + uId + "/" + mCurrentUser.getUid(), null);
        mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                friendRequestBtn.setEnabled(true);
                mCurrentState = "not_friends";
                friendRequestBtn.setText("Send Friend Request");
                declineBtn.setVisibility(View.INVISIBLE);
                if (databaseError != null)
                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT)
                            .show();
            }
        });
    }

    private String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return df.format(c.getTime());
    }
}
