package eu.redray.trevie.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by frano on 23/01/2016.
 */
public class PosterHeper {
    private final static String FILE_EXTENSION = ".jpg";
    private final static String DIRECTORY = "Posters";

    /**
     * Saves poster image to internal storage
     * @param context Application's context
     * @param bitmap Movie poster to save
     * @param title Movie's title
     * @return Path to saved file
     */
    public static String savePoster(Context context, Bitmap bitmap, String title) {
        OutputStream fOut = null;
        Uri outputFileUri = null;
        try {
            File root = new File(context.getFilesDir()
                    + File.separator + DIRECTORY + File.separator);
            root.mkdirs();
            File sdImageMainDirectory = new File(root, title + FILE_EXTENSION);
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);
        } catch (Exception e) {
            Log.e(PosterHeper.class.getSimpleName(), e.getMessage(), e);
            e.printStackTrace();
        }

        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            Log.e(PosterHeper.class.getSimpleName(), e.getMessage(), e);
            e.printStackTrace();
        }

        return outputFileUri.toString();
    }

    public static boolean deletePoster(Context context, String title) {
        File file = new File(context.getFilesDir() + File.separator + DIRECTORY + File.separator,
                title + FILE_EXTENSION);
        return file.delete();
    }
}
