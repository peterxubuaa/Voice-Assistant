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
            viewHolder.mSendLayout = view.findViewById(R.id.send_layout);
            viewHolder.mSendHead = view.findViewById(R.id.send_head_image_view);
            viewHolder.mSendMsg = view.findViewById(R.id.send_msg_text_view);
            viewHolder.mSendImage = view.findViewById(R.id.send_extra_image_view);

            viewHolder.mReceiveLayout = view.findViewById(R.id.receive_layout);
            viewHolder.mReceiveHead = view.findViewById(R.id.receive_head_image_view);
            viewHolder.mReceiveMsg = view.findViewById(R.id.receive_msg_text_view);
            viewHolder.mReceiveImage = view.findViewById(R.id.receive_extra_image_view);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        if (null != msg) {
            switch (msg.getType()) {
                case Msg.TYPE_SEND_TEXT:
                    viewHolder.mSendLayout.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveLayout.setVisibility(View.GONE);
                    viewHolder.mSendMsg.setText(msg.getContent());
                    viewHolder.mSendMsg.setVisibility(View.VISIBLE);
                    viewHolder.mSendImage.setVisibility(View.GONE);
                    if (mHideHeadPic) viewHolder.mSendHead.setVisibility(View.GONE);
                    break;
                case Msg.TYPE_RECEIVED_TEXT:
                    viewHolder.mSendLayout.setVisibility(View.GONE);
                    viewHolder.mReceiveLayout.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveMsg.setText(msg.getContent());
                    viewHolder.mReceiveMsg.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveImage.setVisibility(View.GONE);
                    if (mHideHeadPic) viewHolder.mReceiveHead.setVisibility(View.GONE);
                    break;
                case Msg.TYPE_SEND_IMAGE:
                    viewHolder.mSendLayout.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveLayout.setVisibility(View.GONE);
                    viewHolder.mSendMsg.setVisibility(View.GONE);
                    viewHolder.mSendImage.setImageBitmap(msg.getImageBitmap());
                    viewHolder.mSendImage.setVisibility(View.VISIBLE);
                    break;
                case Msg.TYPE_RECEIVED_IMAGE:
                    viewHolder.mSendLayout.setVisibility(View.GONE);
                    viewHolder.mReceiveLayout.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveMsg.setVisibility(View.GONE);
                    viewHolder.mReceiveImage.setImageBitmap(msg.getImageBitmap());
                    viewHolder.mReceiveImage.setVisibility(View.VISIBLE);
                    break;
            }
        }
        return view;
    }

    class ViewHolder {
        RelativeLayout mSendLayout;
        TextView mSendMsg;
        ImageView mSendHead;
        ImageView mSendImage;

        RelativeLayout mReceiveLayout;
        TextView mReceiveMsg;
        ImageView mReceiveHead;
        ImageView mReceiveImage;
    }
}
