package com.fih.featurephone.voiceassistant.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fih.featurephone.voiceassistant.R;

import java.util.List;

public class MsgAdapter extends ArrayAdapter<Msg> {
    private int mResourceID;
    private boolean mHideHeadPic = false;

    public MsgAdapter(Context context, int textViewResourceID, List<Msg> objects) {
        super(context, textViewResourceID, objects);
        mResourceID = textViewResourceID;
    }

    public void setHideHeadPic(boolean hide) {
        mHideHeadPic = hide;
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Msg msg = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceID, null);
            viewHolder = new ViewHolder();
            viewHolder.mSendLayout = (RelativeLayout)view.findViewById(R.id.send_layout);
            viewHolder.mReceiveLayout = (RelativeLayout)view.findViewById(R.id.receive_layout);
            viewHolder.mSendMsg = (TextView)view.findViewById(R.id.send_msg);
            viewHolder.mReceiveMsg = (TextView)view.findViewById(R.id.receive_msg);
            viewHolder.mSendHead = (ImageView)view.findViewById(R.id.send_head);
            viewHolder.mReceiveHead = (ImageView)view.findViewById(R.id.receive_head);
            if (mHideHeadPic) {
                viewHolder.mSendHead.setVisibility(View.GONE);
                viewHolder.mReceiveHead.setVisibility(View.GONE);
            }
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (null != msg) {
            if (msg.getType() == Msg.TYPE_SEND) {
                viewHolder.mSendMsg.setText(msg.getContent());
                viewHolder.mSendLayout.setVisibility(View.VISIBLE);
                viewHolder.mReceiveLayout.setVisibility(View.GONE);
            } else if (msg.getType() == Msg.TYPE_RECEIVED) {
                viewHolder.mReceiveMsg.setText(msg.getContent());
                viewHolder.mReceiveLayout.setVisibility(View.VISIBLE);
                viewHolder.mSendLayout.setVisibility(View.GONE);
            }
        }
        return view;
    }

    class ViewHolder {
        RelativeLayout mSendLayout;
        RelativeLayout mReceiveLayout;
        TextView mSendMsg;
        TextView mReceiveMsg;
        ImageView mSendHead;
        ImageView mReceiveHead;
    }
}
