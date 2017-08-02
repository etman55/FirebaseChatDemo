package com.example.firebasechatdemo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.firebasechatdemo.R;
import com.example.firebasechatdemo.models.Message;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Etman on 8/2/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessageList = new ArrayList<>();

    public MessageAdapter(List<Message> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sender_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message msg = mMessageList.get(position);
        holder.setSenderMsgTv(msg.getMessage());

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.sender__chat_img)
        CircleImageView senderImg;
        @Bind(R.id.sender_msg_tv)
        TextView senderMsgTv;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setSenderImg(String img, Context ctx) {

        }

        private void setSenderMsgTv(String msg) {
            senderMsgTv.setText(msg);
        }
    }

}
