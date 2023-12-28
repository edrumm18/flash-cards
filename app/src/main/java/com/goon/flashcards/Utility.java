package com.goon.flashcards;

import static com.goon.flashcards.Logger.elogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * This class contains utility functions used across the application.
 */
public class Utility
{
    // Use class name in logs.
    public static final String tag = Utility.class.getSimpleName();

    // Return codes.
    public final static int SUCCESS = 0;
    public final static int FAILURE = -1;


    public Utility()
    {
        // Need public constructor, but since this is utility, all the functions in this
        // file should be static, meaning we can call them without creating a FluxUtility instance.
    }



    /**
     * Wrapper for showing Toast notifications, used when attempting to show from non-UI thread.
     * Accounts for an unhandled exception bug in API 25.
     * @param message The text to be shown in the Toast notification.
     * @param activity The root activity.
     * @param context A Context allows access to application-specific resources and classes.
     */
    public static void makeToast(String message, Activity activity, Context context)
    {
        // Need to run/show Toast on the UI thread.
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run()
            {
                try
                {
                    // If not API 25, just use regular Toast.
                    Toast.makeText(context, message,Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    elogger(tag, e.toString());
                }
            }
        });
    }

    /**
     * Wrapper for showing Toast notifications, used when attempting to show from non-UI thread.
     * Accounts for an unhandled exception bug in API 25.
     * @param message The text to be shown in the Toast notification.
     * @param activity The root activity.
     * @param context A Context allows access to application-specific resources and classes.
     */
    public static void makeLongToast(String message, Activity activity, Context context)
    {
        // Need to run/show Toast on the UI thread.
        // In the context of Flux, sometimes we want to show notifications from SpecANetControlThread or other
        // non-UI objects.
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run()
            {
                // If not API 25, just use regular Toast.
                Toast.makeText(context, message,Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * Copies a file from assets to the specified output directory.
     * @param path  path of the file to be copied.
     * @param target_location The directory in which to place the file.
     * @param activity The root activity.
     */
    public static int copy_file(String path, String target_location, Activity activity)
    {
        int retcode = SUCCESS;

        // Validate.
        if (null == path || null == target_location || null == activity)
        {
            elogger(tag, "gave null args to copy_file");
            return FAILURE;
        }

        int index = path.lastIndexOf(File.separator);
        String filename = path.substring(index + 1);


        // Get controller for assets.
        final AssetManager assetManager = activity.getAssets();

        // Set up streams for file data.
        InputStream in = null;
        OutputStream out = null;

        try
        {
            // Open the file to be copied.
            in = assetManager.open(path);

            // Get the path for the file's new location.
            final String newFileName = target_location + filename;

            // Create the file in its new location.
            out = new FileOutputStream(newFileName);

            Log.v("COPY", "Copying file: " + filename);

            // Copy in 1024 byte chunks.
            byte[] buffer = new byte[1024];
            int read = FAILURE;

            do
            {
                // Read a chunk of file data.
                read = in.read(buffer);
                if (read != FAILURE)
                {
                    // Write it to the file in the new location.
                    out.write(buffer, 0, read);
                }
            }
            while (read != FAILURE);

            buffer = null; // No longer need buffer.


            // Shut down IO streams.
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return FAILURE;
        }

        return retcode;
    }

    /**
     * Given a file path, get its name without the extension.
     * @param filepath The path to the target file.
     * @return The string of the name without the extension, or null on error.
     */
    public static String get_base_filename(String filepath)
    {
        String answer = null;

        // Validate.
        if (null == filepath)
        {
            elogger(tag, "gave null string to get_base_filename.");
            return answer;
        }

        final File f_object = new File(filepath);
        final String base_file = f_object.getName();

        // Get index of very last period.
        int extension_index = base_file.lastIndexOf('.');

        if (extension_index <= 0)
        {
            elogger(tag, "could not get base file name.");
            return answer;
        }

        answer = base_file.substring(0, extension_index);

        return answer;
    }


    /**
     * For a given button, show or hide it.
     * @param button The button to act on.
     * @param status True if we want to show the button, false otherwise.
     */
    public static void set_button_visibility(Button button, boolean status)
    {
        // Validate.
        if (null == button)
        {
            elogger(tag, "gave null arg to set_button_visibility.");
            return;
        }

        // We want to see the button.
        if (status)
        {
            button.setVisibility(View.VISIBLE);
        }
        else
        {
            button.setVisibility(View.GONE);
        }
    }


    /**
     * Check if file exists.
     * @param filepath The file path which to check.
     * @return True if file exists at path, false otherwise.
     */
    public static boolean java_file_exists(String filepath)
    {
        if (null == filepath)
        {
            elogger(tag, "null arg.");
            return false;
        }

        final File checkfile = new File(filepath);
        if (!checkfile.exists())
        {
            elogger(tag, "file DNE: " + filepath);
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * This should be called when there is an unresolvable error encountered in the app. This will
     * display whatever error occurred, and then close the app.
     * @param message The error messsage to display.
     * @param activity The target activity.
     * @param context The target context.
     */
    public static void critical_app_error(String message, Activity activity, Context context)
    {
        // Validate.
        if (null == message || null == activity || null == context)
        {
            elogger(tag, "args null.");
            return;
        }

        if (0 == message.length())
        {
            elogger(tag, "message must be > 0");
            return;
        }

        elogger(tag, message);

        // Dialog builder/helper.
        androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(activity);

        // Need to be able to run this from anywhere, so say runonUIthread.
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run()
            {
                try
                {
                    final androidx.appcompat.app.AlertDialog ad = adb.create();
                    LinearLayout ll = new LinearLayout(activity);
                    LinearLayout.LayoutParams layoutParams = new
                            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ll.setLayoutParams(layoutParams);
                    ad.setCancelable(false);
                    ad.setTitle("Critical Error");
                    ad.setMessage("\nWe have encountered a critical error and need to close.\nError: " + message);
                    ad.setIcon(activity.getResources().getDrawable(R.drawable.ic_baseline_sentiment_dissatisfied_24));
                    ad.setView(ll);

                    // Set what happens when the Install button is clicked.
                    ad.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialogInterface, i) ->
                    {
                        // After user hits okay, close app.
                        dialogInterface.dismiss();

                        activity.finish();

                    });

                    // Finally, show the dialog we just defined.
                    ad.show();
                }
                catch (Exception e)
                {
                    elogger(tag, e.toString());
                }
            }
        });

    }

}

// End of file.
