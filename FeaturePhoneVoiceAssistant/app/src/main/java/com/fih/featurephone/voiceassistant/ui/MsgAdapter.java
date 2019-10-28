package com.fih.featurephone.voiceassistant.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fih.featurephone.voiceassistant.R;

import java.util.List;

public class MsgAdapter extends ArrayAdapter<Msg> {
    private int mResourceID;

    public MsgAdapter(Context context, int textViewResourceID, List<Msg> objects) {
        super(context, textViewResourceID, objects);
        mResourceID = textViewResourceID;
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
            viewHolder.sendLayout = (RelativeLayout)view.findViewById(R.id.send_layout);
            viewHolder.receiveLayout = (RelativeLayout)view.findViewById(R.id.receive_layout);
            viewHolder.sendMsg = (TextView)view.findViewById(R.id.send_msg);
            viewHolder.receiveMsg = (TextView)view.findViewById(R.id.receive_msg);
//            viewHolder.sendHead = (ImageView)view.findViewById(R.id.send_head);
//            viewHolder.receiveHead = (ImageView)view.findViewById(R.id.receive_head);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (null != msg) {
            if (msg.getType() == Msg.TYPE_SEND) {
                viewHolder.sendLayout.setVisibility(View.VISIBLE);
//            viewHolder.sendHead.setVisibility(View.VISIBLE);
                viewHolder.receiveLayout.setVisibility(View.GONE);
//            viewHolder.receiveHead.setVisibility(View.GONE);
                viewHolder.sendMsg.setText(msg.getContent());
            } else if (msg.getType() == Msg.TYPE_RECEIVED) {
                viewHolder.receiveLayout.setVisibility(View.VISIBLE);
//            viewHolder.receiveHead.setVisibility(View.VISIBLE);
                viewHolder.sendLayout.setVisibility(View.GONE);
//            viewHolder.sendHead.setVisibility(View.GONE);
                viewHolder.receiveMsg.setText(msg.getContent());
            }
        }
        return view;
    }

    class ViewHolder {
        RelativeLayout sendLayout;
        RelativeLayout receiveLayout;
        TextView sendMsg;
        TextView receiveMsg;
//        ImageView sendHead;
//        ImageView receiveHead;
    }
}
