/*
 * Copyright 2017 isanwenyu@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.isanwenyu.blurdialog.library;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import io.github.isanwenyu.blurdialog.library.utils.BitmapUtils;
import io.github.isanwenyu.blurdialog.library.utils.RenderScriptBlurHelper;

/**
 * Created by isanwenyu on 2017/9/13.
 */
public class BlurDialog extends Dialog {

    public static final int DIALOG_BLUR_BG_LAYOUT = R.layout.dialog_blur_bg_layout;
    public static final int LAYOUT_BLUR_DIALOG_ID = R.id.layout_blur_dialog;
    private Activity mOwnerActivity; // 这个Dialog依附的Activity
    private ImageView blurImage; // 显示模糊的图片
    private ImageView blurAlpha; // 显示透明度
    private FrameLayout showView; // 要往上面添加布局的父控件
    private AlphaAnimation alphaAnimation; // 透明变化
    private Animation dialogInAnim; // Dialog进入动画
    private Bitmap bitmap;
    private int mRadius = 5;

    public BlurDialog(Activity activity) {
        super(activity, R.style.TransparentDialog);
        mOwnerActivity = activity;
        setContentView(DIALOG_BLUR_BG_LAYOUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 默认设置透明颜色为半透明黑色
        blurAlpha.setBackgroundColor(0x66000000);

        // 背景透明动画
        alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);

    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View view = View.inflate(getContext(), layoutResID, null);
        setContentView(view);
    }

    @Override
    public void setContentView(@NonNull View view) {
        setContentView(view, null);
    }

    @Override
    public void setContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {

        if (LAYOUT_BLUR_DIALOG_ID == view.getId()) {
            if (params != null) {
                super.setContentView(view, params);
            } else {
                super.setContentView(view);
            }

            initBlurLayout();
        } else {
            if (params != null) {
                showView.addView(view, params);
            } else {
                showView.addView(view);
            }
        }
    }

    private void initBlurLayout() {
        blurImage = (ImageView) findViewById(R.id.iv_blur_show);
        blurAlpha = (ImageView) findViewById(R.id.iv_blur_alpha);
        showView = (FrameLayout) findViewById(R.id.fl_add_views);
    }

    protected void setShowInAnimation(Animation dialogInAnim) {
        this.dialogInAnim = dialogInAnim;
    }

    /**
     * 设置高斯模糊的前景颜色
     *
     * @param color 前景的颜色（使用ARGB来设置）
     */
    protected void setFilterColor(int color) {
        blurAlpha.setBackgroundColor(color);
    }

    public BlurDialog setRadius(int radius) {
        this.mRadius = radius;
        return this;
    }

    public Activity getDialogActivity() {
        return mOwnerActivity;
    }

    /**
     * 显示Dialog类，同时进行动画播放
     */
    @Override
    public void show() {
        super.show();
        // 开始截屏并进行高斯模糊
        new BlurAsyncTask().execute();
        // 背景开始渐变
        if (alphaAnimation != null) {
            blurAlpha.startAnimation(alphaAnimation);
        }
        // 框弹出的动画
        if (dialogInAnim != null) {
            showView.startAnimation(dialogInAnim);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        // 要设置下面这个，不然下次截图会返回上次的画面，不能实时更新
        mOwnerActivity.getWindow().getDecorView().setDrawingCacheEnabled(false);
        // 对bitmap进行回收
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 实现高斯模糊的任务
     */
    private class BlurAsyncTask extends AsyncTask {

        private int measuredWidth;
        private int measuredHeight;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 截图
            mOwnerActivity.getWindow().getDecorView().setDrawingCacheEnabled(true);
            bitmap = mOwnerActivity.getWindow().getDecorView().getDrawingCache();
            measuredWidth = mOwnerActivity.getWindow().getDecorView().getMeasuredWidth();
            measuredHeight = mOwnerActivity.getWindow().getDecorView().getMeasuredHeight();
//            BitmapUtils.saveBitmapToFile(bitmap, getContext().getCacheDir().getPath() + File.separator + "blur_wallet.jpg", 75);
        }

        @Override
        protected Object doInBackground(Object[] params) {

            if (bitmap != null) {
                //压缩图片
                bitmap = BitmapUtils.compressBitmap(bitmap, 75, 5);
                // 进行高斯模糊
                bitmap = RenderScriptBlurHelper.doBlur(bitmap, mRadius, false, mOwnerActivity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (bitmap != null) {
                blurImage.setImageBitmap(bitmap);
            }
        }
    }

}
