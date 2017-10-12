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

package io.github.isanwenyu.blurdialog.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

/**
 * Simple helper used to blur a bitmap thanks to render script.
 */
public final class RenderScriptBlurHelper {

    /**
     * Log cat
     */
    private static final String TAG = RenderScriptBlurHelper.class.getSimpleName();

    /**
     * Non instantiable class.
     */
    private RenderScriptBlurHelper() {

    }

    /**
     * blur a given bitmap
     *
     * @param sentBitmap       bitmap to blur
     * @param radius           blur radius
     * @param canReuseInBitmap true if bitmap must be reused without blur
     * @param context          used by RenderScript, can be null if RenderScript disabled
     * @return blurred bitmap
     */
    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap, Context context) {
        Bitmap bitmap;

        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (bitmap.getConfig() == Bitmap.Config.RGB_565) {
            // RenderScript hates RGB_565 so we convert it to ARGB_8888
            // (see http://stackoverflow.com/questions/21563299/
            // defect-of-image-with-scriptintrinsicblur-from-support-library)
            bitmap = convertRGB565toARGB888(bitmap);
        }

        try {
            final RenderScript rs = RenderScript.create(context);
            final Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        } catch (RSRuntimeException e) {
            Log.e(TAG, "RenderScript known error : https://code.google.com/p/android/issues/detail?id=71347 "
                    + "continue with the FastBlur approach.");
        }

        return null;
    }

    private static Bitmap convertRGB565toARGB888(Bitmap bitmap) {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }
}