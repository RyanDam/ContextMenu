package com.rstudio.widget.contextmenu;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by Ryan on 10/25/15.
 */
public class ContextMenu {

    private Builder mBuilder;
    View clickView = null;

    private boolean isShowed;

    protected ContextMenu(Builder builder) {
        mBuilder = builder;
    }

    public void dismiss() {
        removeContextMenu(mBuilder.mContextMenu);
    }

    public void clipToView(@NonNull View v) {
        clickView = v;
        clickView.setOnTouchListener(mBuilder.clickTouchListener);
    }

    public void setDirection(Direction d) {
        mBuilder.setDirection(d);
    }

    public void setViewPadding(int p) {
        mBuilder.padding = p;
    }

    public View getView() {
        return mBuilder.mContextMenu;
    }

    public boolean isShowed() {
        return isShowed;
    }

    private void showContextMenu(int x, int y) {
        if (!isShowed) {
            mBuilder.mParam.x = x;
            mBuilder.mParam.y = y;
            mBuilder.wmn.addView(mBuilder.mContextMenu, mBuilder.mParam);
            isShowed = true;
        }
    }

    private void removeContextMenu(View view) {
        if (isShowed) {
            mBuilder.wmn.removeView(view);
            isShowed = false;
        }
    }

    public enum Direction {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        CENTER
    }

    public static class Builder {

        Context mContext;
        ContextMenu mMenu;
        View mContextMenu;
        View clickView;
        int customContextAnime = 0;

        WindowManager wmn;
        WindowManager.LayoutParams mParam;
        int screenWidth, screenHeight;
        LayoutInflater inf;
        float backgroundAlpha = -1f;

        int layoutId = 0;
        Direction direction = Direction.LEFT;
        int padding = 0;

        View.OnTouchListener customTouchListener = null;
        View.OnTouchListener touchListener = new View.OnTouchListener() {

            int initX, initY;
            float initTouchX, initTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (customTouchListener != null)
                    customTouchListener.onTouch(v, event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = mParam.x;
                        initY = mParam.y;
                        initTouchX = event.getRawX();
                        initTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if ((mParam.x - initX < 10) && (mParam.y - initY < 10)) {
                            if (mMenu.isClickOutside(initX, initY, initTouchX, initTouchY)) {
                                mMenu.onClickOutside(mContextMenu);
                            }
                        }
                        return true;
                }
                return false;
            }
        };
        View.OnTouchListener clickDefTouchListener = null;
        View.OnTouchListener clickTouchListener = new View.OnTouchListener() {

            int[] location = new int[2];

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (clickDefTouchListener != null)
                    clickDefTouchListener.onTouch(v, event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.getLocationInWindow(location);
                        if (direction == Direction.LEFT) {
                            mParam.gravity = Gravity.TOP | Gravity.RIGHT;
                            mMenu.showContextMenu(screenWidth - location[0] + 10 + padding, location[1] - v.getHeight() / 2);
                        }
                        else if (direction == Direction.RIGHT) {
                            mParam.gravity = Gravity.TOP | Gravity.LEFT;
                            mMenu.showContextMenu(location[0] + v.getWidth() + 10 + padding, location[1] - v.getHeight() / 2);
                        }
                        else if (direction == Direction.TOP) {
                            mParam.gravity = Gravity.BOTTOM | Gravity.LEFT;
                            mMenu.showContextMenu(location[0], screenHeight - location[1] + padding);
                        }
                        else if (direction == Direction.BOTTOM){
                            mParam.gravity = Gravity.TOP | Gravity.LEFT;
                            mMenu.showContextMenu(location[0], location[1] + v.getHeight() / 2 + padding);
                        }
                        else { // Direction.CENTER
                            mParam.gravity = Gravity.TOP | Gravity.LEFT;
                            mMenu.showContextMenu(location[0] + padding, location[1] - v.getHeight() / 2);
                        }
                        return true;
                }
                return false;
            }
        };

        public Builder(@NonNull Context context) {
            mContext = context;
            wmn = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

            DisplayMetrics d = new DisplayMetrics();
            wmn.getDefaultDisplay().getMetrics(d);
            screenWidth = d.widthPixels;
            screenHeight = d.heightPixels;

            inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public ContextMenu build() {
            if (mParam == null) {
                mParam = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT
                        , WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                        , WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        , PixelFormat.TRANSLUCENT);

                if (backgroundAlpha == -1f)
                    mParam.dimAmount = 0.0f;
                else
                    mParam.dimAmount = backgroundAlpha;

                if (customContextAnime == 0)
                    mParam.windowAnimations = android.R.style.Animation_Translucent;
                else
                    mParam.windowAnimations = customContextAnime;

                mParam.gravity = Gravity.TOP| Gravity.RIGHT;
            }

            mContextMenu = inf.inflate(layoutId, null);
            mContextMenu.setOnTouchListener(touchListener);

            mMenu = new ContextMenu(this);
            return mMenu;
        }

        public Builder setDirection(Direction d) {
            this.direction = d;
            return this;
        }

        public Builder setContextMenuView(int r) {
            this.layoutId = r;
            return this;
        }

        public Builder setWindowBacgroundAlpha(float alpha) {
            backgroundAlpha = alpha;
            return this;
        }

        // who need this?
//        public Builder setOntouchListener(View.OnTouchListener l) {
//            this.customTouchListener = l;
//            return this;
//        }

        public Builder setCustomWindowAnimations(int animations) {
            customContextAnime = animations;
            return this;
        }

        public Builder setViewPadding(int p) {
            padding = p;
            return this;
        }

    }

    protected void onClickOutside(View contextMenu) {
        removeContextMenu(contextMenu);
    }

    private boolean isClickOutside(int initX, int initY, float initTouchX, float initTouchY) {
        if (initX >= initTouchX
                || (initX + mBuilder.mParam.width) <= initTouchX) {
            if (initY >= initTouchY
                    || (initY + mBuilder.mParam.height) <= initTouchY) {
                return true;
            }
        }
        else {
            if (initY >= initTouchY
                    || (initY + mBuilder.mParam.height) <= initTouchY) {
                return true;
            }
        }
        if (!(initY >= initTouchY
                || (initY + mBuilder.mParam.height) <= initTouchY)) {
            if (initX >= initTouchX
                    || (initX + mBuilder.mParam.width) <= initTouchX) {
                return true;
            }
        }
        return false;
    }

}
