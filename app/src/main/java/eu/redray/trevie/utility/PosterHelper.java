/*
 * Copyright (C) 2016 Mateusz Widuch
 */
package eu.redray.trevie.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides methods for retrieving and deleting movie poster images
 */
class PosterHelper {
    private final static String FILE_EXTENSION = ".png";
    private final static String DIRECTORY = "Posters";

    /**
     * Saves poster image to internal storage
     *
     * @param context the context of the app giving access to file storage
     * @param bitmap  the poster of a movie provided as bitmap
     * @param title   the title of a movie
     * @return        the path to saved image
     */
    public static String savePoster(Context context, Bitmap bitmap, String title) {
        OutputStream fOut = null;
        Uri outputFileUri = null;
        try {
            File root = new File(context.getFilesDir()
                    + File.separator + DIRECTORY + File.separator);
            // Create root directory if it doesn't exist
            if (!root.exists()) {
                //noinspection ResultOfMethodCallIgnored
                root.mkdirs();
            }
            File sdImageMainDirectory = new File(root, title + FILE_EXTENSION);
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);
        } catch (FileNotFoundException e) {
            Log.e(PosterHelper.class.getSimpleName(), e.getMessage(), e);
            e.printStackTrace();
        } finally {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                Log.e(PosterHelper.class.getSimpleName(), e.getMessage(), e);
                e.printStackTrace();
            }
        }

        return outputFileUri.toString();
    }

    /**
     * Deletes poster image from internal storage
     *
     * @param context the context of the app giving access to file storage
     * @param title   the title of a movie
     * @return        true if successful
     */
    public static boolean deletePoster(Context context, String title) {
        File file = new File(context.getFilesDir() + File.separator + DIRECTORY + File.separator,
                title + FILE_EXTENSION);
        return file.delete();
    }
}
