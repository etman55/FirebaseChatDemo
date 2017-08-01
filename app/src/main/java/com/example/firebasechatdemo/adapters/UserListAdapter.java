package com.example.firebasechatdemo.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.firebasechatdemo.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import butterknife.ButterKnife;

/**
 * Created by Etman on 7/30/2017.
 */

public class UserListAdapter extends FirebaseRecyclerAdapter<User,UserListAdapter.MyUserViewHolder> {


    public UserListAdapter(Class<User> modelClass, int modelLayout, Class<MyUserViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(MyUserViewHolder viewHolder, User model, int position) {

    }

    public class MyUserViewHolder extends RecyclerView.ViewHolder {


        public MyUserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
