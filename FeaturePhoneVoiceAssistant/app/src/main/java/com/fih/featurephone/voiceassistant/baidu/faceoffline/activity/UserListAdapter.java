package com.fih.featurephone.voiceassistant.baidu.faceoffline.activity;

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

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.User;
import com.fih.featurephone.voiceassistant.utils.FileUtils;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {
    private int mResourceID;

    class ViewHolder {
        TextView mNumberIndexTextView;
        TextView mUserNameTextView;
        ImageView mHeaderImageView;
        CheckBox mDeleteCheckBox;
    }

    UserListAdapter(Context context, int textViewResourceID, List<User> objects) {
        super(context, textViewResourceID, objects);
        mResourceID = textViewResourceID;
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final User user = getItem(position);
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

        if (null != user) {
            viewHolder.mNumberIndexTextView.setText(String.valueOf(position + 1));
            viewHolder.mUserNameTextView.setText(user.getUserName());
            if (FileUtils.isFileExist(user.getFaceImagePath())) {
                viewHolder.mHeaderImageView.setImageBitmap(
                        BitmapFactory.decodeFile(user.getFaceImagePath()));
            }
            viewHolder.mDeleteCheckBox.setChecked(user.getChecked());

            //注意这里设置的不是onCheckedChangListener，还是值得思考一下的
            viewHolder.mDeleteCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user.setChecked(viewHolder.mDeleteCheckBox.isChecked());
                }
            });
        }
        return view;
    }
}
