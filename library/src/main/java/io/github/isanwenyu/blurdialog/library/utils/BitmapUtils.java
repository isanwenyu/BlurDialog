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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by isanwenyu on 2017/9/13.
 */
public class BitmapUtils {

    public static void compressBitmapToFile(Bitmap bitmap, String filePath, int quality) {
        File file = new File(filePath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap compressBitmap(Bitmap bitmap, int quality, int inSampleSize) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            Bitmap compressedBitmap = BitmapFactory.decodeStream(bis, null, options);
            bos.close();
            bis.close();
            return compressedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap compressBitmapFromFile(String filePath, int inSampleSize) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(filePath, options);

    }
}
