package com.example.firebasechatdemo.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.firebasechatdemo.R;
import com.example.firebasechatdemo.activities.ChatActivity;
import com.example.firebasechatdemo.activities.ProfileActivity;
import com.example.firebasechatdemo.models.Friends;
import com.example.firebasechatdemo.utils.PicassoCache;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    @Bind(R.id.friends_list)
    RecyclerView friendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.item_single_user,
                        FriendsViewHolder.class,
                        mFriendsDatabase) {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                        viewHolder.setmStatus(model.getDate());
                        final String listUserId = getRef(position).getKey();
                        mUserDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String userName = dataSnapshot.child("name").getValue().toString();
                                String online = dataSnapshot.child("online").getValue().toString();

                                viewHolder.setName(userName);
                                viewHolder.setUserAvatar(dataSnapshot.child("thumb_image").getValue().toString(),
                                        getContext());
                                viewHolder.setOnlineStatus(online, getContext());
                                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send a message"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Select Option");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case 0:
                                                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                        profileIntent.putExtra("user_id", listUserId);
                                                        startActivity(profileIntent);
                                                        break;
                                                    case 1:
                                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                        chatIntent.putExtra("user_id", listUserId);
                                                        chatIntent.putExtra("user_name", userName);
                                                        startActivity(chatIntent);
                                                        break;
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };
        friendsList.setAdapter(friendsAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.user_name_txt)
        TextView displayName;
        @Bind(R.id.user_status_txt)
        TextView user_status;
        @Bind(R.id.user_avatar)
        CircleImageView avatar;
        @Bind(R.id.user_status_img)
        ImageView onlineStatus;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setName(String name) {
            displayName.setText(name);
        }

        public void setmStatus(String status) {
            user_status.setText(status);
        }

        public void setUserAvatar(String thumbUrl, Context context) {
            PicassoCache.get(context)
                    .load(thumbUrl)
                    .placeholder(R.mipmap.default_avatar)
                    .into(avatar);
        }

        public void setOnlineStatus(String status, Context context) {
            if (status.equals("true"))
                onlineStatus.setVisibility(View.VISIBLE);
            else
                onlineStatus.setVisibility(View.INVISIBLE);
        }
    }
}
