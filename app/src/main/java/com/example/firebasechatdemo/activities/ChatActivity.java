package com.example.firebasechatdemo.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.firebasechatdemo.R;
import com.example.firebasechatdemo.GetTimeAgo;
import com.example.firebasechatdemo.adapters.MessageAdapter;
import com.example.firebasechatdemo.models.Message;
import com.example.firebasechatdemo.utils.PicassoCache;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    @Bind(R.id.chat_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.chat_bar_display_name_tv)
    TextView mTitleTv;
    @Bind(R.id.chat_bar_last_seen_tv)
    TextView mLastSeenTv;
    @Bind(R.id.chat_bar_img)
    CircleImageView mProfileImg;
    @Bind(R.id.add_img_btn)
    ImageButton addImgBtn;
    @Bind(R.id.chat_msg_tv)
    TextView chatMsg;
    @Bind(R.id.send_msg_btn)
    ImageButton sendMsgBtn;
    @Bind(R.id.msgs_list)
    RecyclerView messagesList;
    private String mChatUser;
    private String mChatUserName;
    private DatabaseReference mRootref;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private List<Message> mMsgList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter msgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("user_name");
        mAuth = FirebaseAuth.getInstance();
        mRootref = FirebaseDatabase.getInstance().getReference();
        if (mTitleTv != null) {
            mTitleTv.setText(mChatUserName);
        }
        linearLayoutManager = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayoutManager);
        msgAdapter = new MessageAdapter(mMsgList);
        messagesList.setAdapter(msgAdapter);
        mRootref.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                PicassoCache.get(ChatActivity.this)
                        .load(image)
                        .placeholder(R.mipmap.default_avatar)
                        .into(mProfileImg);
                if (online.equals("true"))
                    mLastSeenTv.setText("Online Now");
                else {
                    long lastTime = Long.parseLong(online);
                    String lastSeen = GetTimeAgo.getTimeAgo(lastTime);
                    mLastSeenTv.setText(lastSeen);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mRootref.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message msg = dataSnapshot.getValue(Message.class);
                mMsgList.add(msg);
                msgAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mRootref.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatAddMap = new HashMap<>();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("time_stamp", ServerValue.TIMESTAMP);
                    Map chatUserMap = new HashMap<>();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);
                    mRootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d(TAG, "onComplete Error: " + databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @OnClick(R.id.send_msg_btn)
    void sendMessage() {
        String msg = chatMsg.getText().toString();
        if (!TextUtils.isEmpty(msg)) {
            String currentUserRef = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chatUserRef = "messages/" + mChatUser + "/" + mCurrentUserId;
            DatabaseReference userMsgPush = mRootref.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();
            String pushId = userMsgPush.getKey();
            Map msgMap = new HashMap<>();
            msgMap.put("message", msg);
            msgMap.put("seen", false);
            msgMap.put("type", "text");
            msgMap.put("time_stamp", ServerValue.TIMESTAMP);
            Map msgUserMap = new HashMap<>();
            msgUserMap.put(currentUserRef + "/" + pushId, msgMap);
            msgUserMap.put(chatUserRef + "/" + pushId, msgMap);
            mRootref.updateChildren(msgUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    chatMsg.setText("");
                    if (databaseError != null)
                        Log.d(TAG, "onComplete Error: " + databaseError.getMessage());
                }
            });

        }
    }
}
