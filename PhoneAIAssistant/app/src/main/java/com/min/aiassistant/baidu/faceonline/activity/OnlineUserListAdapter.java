package com.min.aiassistant.baidu.faceonline.activity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.min.aiassistant.R;
import com.min.aiassistant.utils.FileUtils;

import java.util.List;

public class OnlineUserListAdapter extends ArrayAdapter<UserItem> {
    private int mResourceID;

    class ViewHolder {
        TextView mNumberIndexTextView;
        TextView mUserNameTextView;
        ImageView mHeaderImageView;
        CheckBox mDeleteCheckBox;
    }

    OnlineUserListAdapter(Context context, int textViewResourceID, List<UserItem> objects) {
        super(context, textViewResourceID, objects);
        mResourceID = textViewResourceID;
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final UserItem userItem = getItem(position);
        View view;
        final ViewHolder viewHolder;
        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceID, null);

            viewHolder = new ViewHolder();
            viewHolder.mNumberIndexTextView = view.findViewById(R.id.number_index_text_view);
            viewHolder.mUserNameTextView = view.findViewById(R.id.user_name_text_view);
            viewHolder.mHeaderImageView = view.findViewById(R.id.header_image_view);
            viewHolder.mDeleteCheckBox = view.findViewById(R.id.delete_checkbox);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (null != userItem) {
            viewHolder.mNumberIndexTextView.setText(String.valueOf(position + 1));
            String info = userItem.getUserInfo() + " (" + userItem.getUserID() + ")";
            viewHolder.mUserNameTextView.setText(info);
            if (FileUtils.isFileExist(userItem.getFaceLocalImagePath())) {
                viewHolder.mHeaderImageView.setImageBitmap(
                        BitmapFactory.decodeFile(userItem.getFaceLocalImagePath()));
            } else {
                viewHolder.mHeaderImageView.setImageResource(R.drawable.baseline_face_white_48dp);
            }
            viewHolder.mDeleteCheckBox.setChecked(userItem.isChecked());

            //注意这里设置的不是onCheckedChangListener，还是值得思考一下的
            viewHolder.mDeleteCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userItem.setChecked(viewHolder.mDeleteCheckBox.isChecked());
                }
            });
        }
        return view;
    }
}
