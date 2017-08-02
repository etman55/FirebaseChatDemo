package com.example.firebasechatdemo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.firebasechatdemo.R;
import com.example.firebasechatdemo.models.User;
import com.example.firebasechatdemo.utils.PicassoCache;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {
    @Bind(R.id.user_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.user_list)
    RecyclerView mUserList;
    private DatabaseReference mUserDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<User, MyUserViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<User, MyUserViewHolder>(User.class,
                        R.layout.item_single_user,
                        MyUserViewHolder.class,
                        mUserDatabase) {
                    @Override
                    public int getItemCount() {
                        return super.getItemCount();
                    }

                    @Override
                    protected void populateViewHolder(MyUserViewHolder viewHolder, User model, int position) {
                        final String uId = getRef(position).getKey();
                        viewHolder.setName(model.getName());
                        viewHolder.setmStatus(model.getStatus());
                        viewHolder.setUserAvatar(model.getThumb_image(), UserActivity.this);
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(UserActivity.this, ProfileActivity.class);
                                i.putExtra("user_id", uId);
                                startActivity(i);
                            }
                        });
                    }
                };
        mUserList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MyUserViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.user_avatar)
        CircleImageView userAvatar;
        @Bind(R.id.user_name_txt)
        TextView mDisplayName;
        @Bind(R.id.user_status_txt)
        TextView mStatus;
        @Bind(R.id.user_status_img)
        ImageView onlineStatus;

        public MyUserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            onlineStatus.setVisibility(View.INVISIBLE);
        }

        public void setName(String name) {
            mDisplayName.setText(name);
        }

        public void setmStatus(String status) {
            mStatus.setText(status);
        }

        public void setUserAvatar(String thumbUrl, Context context) {
            PicassoCache.get(context)
                    .load(thumbUrl)
                    .placeholder(R.mipmap.default_avatar)
                    .into(userAvatar);
        }
    }
}
