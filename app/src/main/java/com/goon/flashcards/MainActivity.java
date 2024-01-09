package com.goon.flashcards;

import static com.goon.flashcards.Logger.elogger;
import static com.goon.flashcards.Logger.logger;
import static com.goon.flashcards.Utility.critical_app_error;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.List;

import com.goon.flashcards.ListHelper.DeckFileAdapter;
import com.goon.flashcards.ListHelper.DeckFileData;
import com.goon.flashcards.ListHelper.DeckRecyclerViewHelper;

public class MainActivity extends AppCompatActivity
{
    final String tag = MainActivity.class.getSimpleName();

    Activity activity = this;
    Context context = null;

    // Directories.
    public static final String dir_sdcard = "sdcard";
    public static final String dir_flashcards = "flashcards";

    // This is where our CSV flash card decks will go.
    public static final String dir_csv = dir_sdcard + File.separator + dir_flashcards + File.separator;

    /**
     * UI items.
     */
    RecyclerView recyclerview_decks = null;
    DeckFileAdapter adapter_decks = null;
    DeckFileData decks_data_list[] = null;
    RecyclerView.LayoutManager mLayoutManager;
    Utility.LayoutManagerType mCurrentLayoutManagerType;

    private Button button_launch_deck = null;
    private Button button_delete_deck = null;
    private Button button_create_deck = null;
    private Button button_edit_deck = null;


    private void initialize_deck_list(Bundle savedInstanceState)
    {
        /**
         * Populate custom preset file list in its recycler view.
         */

        recyclerview_decks = (RecyclerView) findViewById(R.id.recyclerview_file_list);
        if (null == recyclerview_decks)
        {
            elogger(tag, "could not find recycler view.");
            return;
        }

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(this);

        mCurrentLayoutManagerType = Utility.LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (Utility.LayoutManagerType) savedInstanceState
                    .getSerializable(Utility.KEY_LAYOUT_MANAGER);
        }

        Utility.setRecyclerViewLayoutManager(mCurrentLayoutManagerType, recyclerview_decks,
                mLayoutManager, mCurrentLayoutManagerType, this);

        // Get file data.
        final List deck_list = DeckRecyclerViewHelper.get_deck_list();

        adapter_decks = new DeckFileAdapter(deck_list, context,
                getResources().getDrawable(R.drawable.recycler_card_selected),
                getResources().getDrawable(R.drawable.recycler_card_unselected));

        // Set CustomAdapter as the adapter for RecyclerView.
        recyclerview_decks.setAdapter(adapter_decks);

    }

    public static void debug_toast(Activity activity, Context context)
    {
        Utility.makeToast("TODO.", activity, context);
    }

    private void initialize_buttons()
    {
        /**
         * Set up functionality for custom preset buttons.
         */

        button_create_deck = (Button) findViewById(R.id.button_create);
        if (null == button_create_deck)
        {
            elogger(tag, "cannot find button_create_deck.");
            return;
        }
        button_create_deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                logger(tag, "button_create_deck clicked.");

                debug_toast(activity, context);

            }
        });

        button_launch_deck = (Button) findViewById(R.id.button_launch);
        if (null == button_launch_deck)
        {
            elogger(tag, "cannot find button_launch_deck.");
            return;
        }
        button_launch_deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                logger(tag, "button_launch_deck clicked.");

                debug_toast(activity, context);

            }
        });

        button_edit_deck = (Button) findViewById(R.id.button_edit);
        if (null == button_edit_deck)
        {
            elogger(tag, "cannot find button_edit_deck.");
            return;
        }
        button_edit_deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                logger(tag, "button_edit_deck clicked.");

                debug_toast(activity, context);

            }
        });

        button_delete_deck = (Button) findViewById(R.id.button_delete);
        if (null == button_delete_deck)
        {
            elogger(tag, "cannot find button_delete_deck.");
            return;
        }
        button_delete_deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                logger(tag, "button_delete_deck clicked.");

                debug_toast(activity, context);

            }
        });
    }

    private void initialize_ui(Bundle savedInstanceState)
    {
        initialize_deck_list(savedInstanceState);
        initialize_buttons();
    }


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

        /**
         * Populate the flash card decks from /sdcard/flashcards/ in the list.
         */
        initialize_ui(savedInstanceState);

    }
}
