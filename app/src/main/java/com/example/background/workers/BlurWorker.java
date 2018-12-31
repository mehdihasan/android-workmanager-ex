package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.background.Constants;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.support.constraint.Constraints.TAG;

/**
 * Copyright (c) 2018 Cefalo AS. All rights reserved.
 * OneTimeWorkRequest: A WorkRequest that will only execute once.
 * PeriodicWorkRequest: A WorkRequest that will repeat on a cycle.
 */
public class BlurWorker extends Worker {

    public BlurWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        // ADD THIS LINE
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        try {
            // REPLACE THIS CODE:
            // Bitmap picture = BitmapFactory.decodeResource(
            //        applicationContext.getResources(),
            //        R.drawable.test);
            // WITH
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }
            ContentResolver resolver = applicationContext.getContentResolver();
            // Create a bitmap
            Bitmap picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));
            // Blur the bitmap
            Bitmap output = WorkerUtils.blurBitmap(picture, applicationContext);
            // Write bitmap to a temp file
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output);
            WorkerUtils.makeStatusNotification("Output is "
                    + outputUri.toString(), applicationContext);
            // If there were no errors, return SUCCESS
            return Result.success(new Data.Builder().putString(
                    Constants.KEY_IMAGE_URI, outputUri.toString()).build());
        } catch (Throwable throwable) {
            // Technically WorkManager will return Worker.Result.FAILURE
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }
}
