package com.goon.flashcards;

import static com.goon.flashcards.Logger.elogger;
import static com.goon.flashcards.Logger.logger;
import static com.goon.flashcards.Utility.critical_app_error;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    final String tag = MainActivity.class.getSimpleName();

    Context context = null;

    // Directories.
    final String dir_sdcard = "sdcard";
    final String dir_flashcards = "flashcards";

    // This is where our CSV flash card decks will go.
    final String dir_csv = dir_sdcard + File.separator + dir_flashcards + File.separator;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        /**
         * App setup.
         * Check storage permission.
         */

        // On Android 11, request Manage external storage by opening android system window to grant perms.
        if (!Environment.isExternalStorageManager())
        {
            // If you don't have access, launch a new activity to show the user the system's dialog
            // to allow full access to the external storage.
            logger(tag, "Asking for MANAGE_EXTERNAL_STORAGE permissions.");
            Intent intent = new Intent();

            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);

            int t=0;

            startActivityForResult(intent, 456);

            while (!Environment.isExternalStorageManager())
            {
                try
                {
                    Thread.sleep(1000);
                    t++;
                    logger(tag, "Permissions missing. Waiting ...");
                }
                catch (InterruptedException e)
                {
                    elogger(tag, "Permissions missing. Waiting ...");
                }
                if (t >= 7)
                {
                    finishActivity(456);
                    critical_app_error("Timeout while requesting permissions"
                            + " or permissions were denied", this, context);
                    return;
                }

            }

            logger(tag, "Has MANAGE_EXTERNAL_STORAGE permissions.");
            // Exit that screen once granted.
            // may need to put back activity.finishactivity(456);
            finishActivity(456);
        }

        /**
         *
         * Check if our dir already exists on the sdcard.
         * If not, create it.
         */
        final File file_csv_dir = new File(dir_csv);
        if (!file_csv_dir.exists())
        {
            logger(tag, "Flash cards dir DNE. Creating.");
            boolean retcode = file_csv_dir.mkdirs();
            if (!retcode)
            {
                critical_app_error("could not make directory for flash card files.", this, context);
            }
            else
            {
                logger(tag, "made CSV file dir.");
            }
        }
        else
        {
            logger(tag, "Flash cards dir exists.");
        }




    }
}
