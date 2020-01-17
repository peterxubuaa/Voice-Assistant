package com.min.aiassistant.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.min.aiassistant.R;
import com.min.aiassistant.utils.CommonUtil;

import java.util.List;

public class MsgAdapter extends ArrayAdapter<Msg> {
    private final int SEND_IMAGE_RADIO = 2;
    private final int RECEIVE_IMAGE_RADIO = 4;

    private int mResourceID;
    private boolean mHideHeadPic = false;
    private Point mDisplaySize;
    private Handler mMainClickHandler = new Handler();

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

    public MsgAdapter(Context context, int textViewResourceID, List<Msg> objects) {
        super(context, textViewResourceID, objects);
        mResourceID = textViewResourceID;
        mDisplaySize = CommonUtil.getDisplaySize(context);
    }

    public void setHideHeadPic(boolean hide) {
        mHideHeadPic = hide;
    }

    @SuppressLint("ClickableViewAccessibility")
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
            viewHolder.mSendImage.setOnTouchListener(mSendImageTouchListener);

            viewHolder.mReceiveLayout = view.findViewById(R.id.receive_layout);
            viewHolder.mReceiveHead = view.findViewById(R.id.receive_head_image_view);
            viewHolder.mReceiveMsg = view.findViewById(R.id.receive_msg_text_view);
            viewHolder.mReceiveImage = view.findViewById(R.id.receive_extra_image_view);
            viewHolder.mReceiveImage.setOnTouchListener(mReceiveImageTouchListener);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        if (null != msg) {
            switch (msg.getType()) {
                case Msg.TYPE_SEND_TEXT:
                    viewHolder.mSendLayout.setVisibility(View.VISIBLE);
                    viewHolder.mSendMsg.setVisibility(View.VISIBLE);
                    viewHolder.mSendImage.setVisibility(View.GONE);
                    if (mHideHeadPic) viewHolder.mSendHead.setVisibility(View.GONE);
                    viewHolder.mSendMsg.setText(msg.getContent());

                    viewHolder.mReceiveLayout.setVisibility(View.GONE);
                    break;
                case Msg.TYPE_RECEIVED_TEXT:
                    viewHolder.mSendLayout.setVisibility(View.GONE);

                    viewHolder.mReceiveLayout.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveMsg.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveImage.setVisibility(View.GONE);
                    if (mHideHeadPic) viewHolder.mReceiveHead.setVisibility(View.GONE);
                    viewHolder.mReceiveMsg.setText(msg.getContent());
                    break;
                case Msg.TYPE_SEND_IMAGE:
                    viewHolder.mSendLayout.setVisibility(View.VISIBLE);
                    viewHolder.mSendMsg.setVisibility(View.GONE);
                    viewHolder.mSendImage.setVisibility(View.VISIBLE);
                    if (mHideHeadPic) viewHolder.mSendHead.setVisibility(View.GONE);
                    viewHolder.mSendImage.setImageBitmap(msg.getImageBitmap());
                    adjustImageLayout(viewHolder.mSendImage, msg.getImageBitmap(), SEND_IMAGE_RADIO);

                    viewHolder.mReceiveLayout.setVisibility(View.GONE);
                    break;
                case Msg.TYPE_RECEIVED_IMAGE:
                    viewHolder.mSendLayout.setVisibility(View.GONE);

                    viewHolder.mReceiveLayout.setVisibility(View.VISIBLE);
                    viewHolder.mReceiveMsg.setVisibility(View.GONE);
                    viewHolder.mReceiveImage.setVisibility(View.VISIBLE);
                    if (mHideHeadPic) viewHolder.mReceiveHead.setVisibility(View.GONE);
                    viewHolder.mReceiveImage.setImageBitmap(msg.getImageBitmap());
                    adjustImageLayout(viewHolder.mReceiveImage, msg.getImageBitmap(), RECEIVE_IMAGE_RADIO);
                    break;
            }
        }
        return view;
    }

    //图片显示范围屏幕宽度的一半
    private void adjustImageLayout(ImageView imageView, Bitmap bitmap, int ratio) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = mDisplaySize.x / ratio;
        layoutParams.height = (int)(bitmap.getHeight() * (float)layoutParams.width / bitmap.getWidth());
        if (layoutParams.height * ratio > mDisplaySize.y) {
            layoutParams.height = mDisplaySize.y / ratio;
            layoutParams.width = (int)(bitmap.getWidth() * (float)layoutParams.height / bitmap.getHeight());
        }
        imageView.setLayoutParams(layoutParams);
    }

    private ImageTouchListener mSendImageTouchListener = new ImageTouchListener(SEND_IMAGE_RADIO, mMainClickHandler);
    private ImageTouchListener mReceiveImageTouchListener = new ImageTouchListener(RECEIVE_IMAGE_RADIO, mMainClickHandler);

    class ImageTouchListener implements View.OnTouchListener {
        private int mRadio;
        private Handler mClickHandler;
        private int mClickCount = 0;

        ImageTouchListener(int ratio, Handler clickHandler) {
            mRadio = ratio;
            mClickHandler = clickHandler;
        }

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            final int TIMEOUT = 400; ////双击间四百毫秒延时ms
            v.performClick();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mClickCount++;
                mClickHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (2 == mClickCount) {//双击
                            switchImageLayout(v);
                        }
                        //清空handler延时，并防内存泄漏
                        mClickHandler.removeCallbacksAndMessages(null);
                        //计数清零
                        mClickCount = 0;
                    }
                    //延时timeout后执行run方法中的代码
                }, TIMEOUT);
            }
            return false;
        }

        private void switchImageLayout(View view) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();

            if (layoutParams.width == mDisplaySize.x / mRadio || layoutParams.height == mDisplaySize.y / mRadio) {
                layoutParams.width = layoutParams.width * mRadio;
                layoutParams.height = layoutParams.height * mRadio;
            } else if (layoutParams.width == mDisplaySize.x || layoutParams.height == mDisplaySize.y) {
                layoutParams.width = layoutParams.width / mRadio;
                layoutParams.height = layoutParams.height / mRadio;
            }

            view.setLayoutParams(layoutParams);
        }
    }
}
