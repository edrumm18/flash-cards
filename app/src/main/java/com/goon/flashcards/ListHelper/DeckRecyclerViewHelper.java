package com.goon.flashcards.ListHelper;

import static com.goon.flashcards.Logger.elogger;
import static com.goon.flashcards.Logger.logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.goon.flashcards.MainActivity;
import com.goon.flashcards.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * This class contains functions used to read, write, and load preset files.
 */
public class DeckRecyclerViewHelper
{
    public static final String tag = DeckRecyclerViewHelper.class.getSimpleName();

    // Deck files will have this extension.
    public static final String deck_file_ext = "csv";

    // Setting max length of deck file name to 50, so it fits in the list properly.
    public static final int max_deck_name_length = 50;


    // Constructor.
    public DeckRecyclerViewHelper()
    {
    }

    /**
     * Check if the file has the valid extension ".settings".
     * @param filepath The full path of the file as a string.
     * @return True if the file has the valid settings extension, false otherwise.
     */
    public static boolean is_valid_deck_file(String filepath)
    {
        // Validate.
        if (null == filepath)
        {
            elogger(tag, "gave null string to is_valid_settings_file.");
            return false;
        }

        // Get index of very last period.
        int index = filepath.lastIndexOf('.');

        if (index <= 0)
        {
            elogger(tag, "could not get extension.");
            return false;
        }

        // Collect string remaining after extension.
        String extension = filepath.substring(index + 1); // is extension without "."

        // Settings file only valid if ending in "settings"
        if (extension.equals(deck_file_ext))
        {
            return true;
        }
        else
        {
            logger(tag, "Found invalid extension " + extension + " for: " + filepath);
            return false;
        }

    }


    /**
     * Given a CSV files, count how many lines it has and return a string.
     * @param filename The file to get line count from.
     * @return The string of how many lines are in the csv file, greater than 0 or null.
     */
    public static String count_csv_lines(String filename)
    {
        String result = null;

        // Validate.
        if (null == filename)
        {
            elogger(tag, "given filename is null.");
            return result;
        }

        if (!is_valid_deck_file(filename))
        {
            elogger(tag, "invalid file name. Cannot pull.");
            return result;
        }

        // Check that file actually exists.
        File file = new File(filename);
        if (!file.exists())
        {
            elogger(tag, "file DNE.");
            return result;
        }

        // Check that there is something in the file.
        if (file.length() <= 0)
        {
            elogger(tag, "file size 0, no lines.");
            return result;
        }

        // Open the file stream.
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (Exception exception)
        {
            elogger(tag, "file could not be opened: " + filename);
            return result;
        }

        InputStreamReader input_stream = new InputStreamReader(fis);
        BufferedReader buffered_reader = new BufferedReader(input_stream);

        // Only iterate to count lines.
        String line = null;
        int count = 0;

        do
        {
            try
            {
                line = buffered_reader.readLine();

                if (line != null)
                {
                    count = count + 1;
                }
            }
            catch (Exception e)
            {
                elogger(tag, "error occurred in file read.");
                return result;
            }
        }
        while (line != null);

        // Close file.
        try
        {
            buffered_reader.close();

        }
        catch (Exception e)
        {
            elogger(tag, "error occurred in file close.");
        }

        // Convert number to string, for display.
        if (count > 0)
        {
            result = String.format("%d", count);
        }

        return result;
    }


    /**
     * Get file information from the custom preset folder and return a list of objects.
     * @return
     */
    public static ArrayList<DeckFileData> get_deck_list()
    {
        ArrayList<DeckFileData> results = null;

        // Check that the dir still exists.
        final File dir = new File(MainActivity.dir_csv);
        if (!dir.exists())
        {
            boolean made_directory = dir.mkdir();
            if (!made_directory)
            {
                elogger(tag, "could find directory: " + MainActivity.dir_csv);
                return results;
            }
        }

        // Get list of files, and make file data objects with them.
        results = new ArrayList<DeckFileData>();

        File directory = new File(MainActivity.dir_csv);
        File[] files = directory.listFiles();

        if (null == files)
        {
            elogger(tag, "could not get files from csv dir.");
            return results;
        }

        // Sort files by name alphabetically before continuing.
        List<File> listFile = Arrays.asList(files);
        Collections.sort(listFile);
        for (int index = 0; index < files.length; index++)
        {
            files[index] = (File) listFile.get(index);
        }

        for (int index = 0; index < files.length; index++)
        {
            String full_filename = MainActivity.dir_csv + files[index].getName();

            // Check if we have a valid extension
            if (is_valid_deck_file(full_filename))
            {
                // If valid extension, strip off the base file name.
                final String base_filename = Utility.get_base_filename(full_filename);
                if (null == base_filename)
                {
                    elogger(tag, "could not get base file name.");
                    return null;
                }

                // Count how many lines are in the CSV file.
                final String num_cards = count_csv_lines(full_filename);
                if (null == num_cards)
                {
                    elogger(tag, "could not count lines in deck file.");
                    return null;
                }

                // Hand all data to the object.
                DeckFileData current_file = new DeckFileData(base_filename, num_cards);

                results.add(current_file);
            }
            else
            {
                // Skip the file if it does not have valid extension.
                continue;
            }

        }

        return results;
    }

    /**
     * Takes action to delete a preset file from the system as well as from being shown in the
     * adapter. Will be called when the user presses "Yes" to confirm deletion.
     * @param file_name The full path of the file to be deleted.
     */
//    public static void confirmed_preset_deletion(String file_name, Activity activity,
//                                                 Context context, presetFileAdapter custom_preset_adapter)
//    {
//        // Validate.
//        if (null == file_name || null == custom_preset_adapter || null == activity ||
//                null == context)
//        {
//            elogger(tag, "gave null args to confirmed_preset_deletion.");
//            return;
//        }
//
//        if (!FluxUtility.kalibash_file_exists(file_name))
//        {
//            elogger(tag, "file DNE to delete.");
//            return;
//        }
//
//        // Use kali to delete file.
//        new BootKali("rm " + file_name).run();
//
//        // Need to notify that we should reload the adapter list.
//        custom_preset_adapter.removeAt(custom_preset_adapter.getLastSelected());
//
//        // Display confirmation to user.
//        FluxUtility.makeToast("Deleted: " +
//                FluxUtility.get_base_filename(file_name), activity, context);
//
//        return;
//    }

    /**
     * Detects what preset is selected in the list, and confirms that the user wants to delete it
     * before proceeding.
     * @param rootView The view in which we are acting.
     */
//    public static void delete_selected_preset(View rootView, Activity activity, Context context,
//                                              presetFileAdapter custom_preset_adapter,
//                                              RecyclerView custom_preset_recyclerview)
//    {
//        // Validate.
//        if (null == rootView || null == activity || null == context ||
//                null == custom_preset_adapter || null == custom_preset_recyclerview)
//        {
//            elogger(tag, "gave null args to delete_selected_preset.");
//            return;
//        }
//
//        // Get the file path from the card selected in the preset recycler view.
//        final String file_name = get_selected_customp_file_path(custom_preset_adapter,
//                custom_preset_recyclerview, activity, context);
//        if (null == file_name)
//        {
//            elogger(tag, "unable to grab file name from selected preset.");
//            return;
//        }
//
//        // Ensure that selected file exists before deletion attempt.
//        boolean exists = FluxUtility.kalibash_file_exists(file_name);
//        if (!exists)
//        {
//            elogger(tag, "file " + file_name + " does not exist, cannot delete.");
//            return;
//        }
//
//        final String base_filename = FluxUtility.get_base_filename(file_name);
//        if (null == base_filename)
//        {
//            elogger(tag, "could not get base file name for " + file_name);
//            return;
//        }
//
//        /**
//         * Build and show confirmation dialog.
//         */
//
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//        alertDialogBuilder.setTitle("Confirm Deletion");
//        alertDialogBuilder.setMessage("Are you sure you want to delete this?\n\n" + base_filename);
//        alertDialogBuilder.setCancelable(false); // Make sure they can't click away, force yes or no choice.
//
//        // Set behavior for positive choice.
//        alertDialogBuilder.setPositiveButton("Confirm",
//                new DialogInterface.OnClickListener()
//                {
//                    public void onClick(DialogInterface dialog,int id)
//                    {
//                        // Take relevant actions to delete the file.
//                        confirmed_preset_deletion(file_name, activity, context,
//                                custom_preset_adapter);
//                    }
//                });
//
//        // Set behavior for negative choice.
//        alertDialogBuilder.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener()
//                {
//                    public void onClick(DialogInterface dialog,int id)
//                    {
//                        // If the user does not want to delete, do nothing except close dialog.
//                        dialog.cancel();
//                    }
//                });
//
//        // After setting up dialog box, show it to the user.
//        alertDialogBuilder.show();
//
//        return;
//    }

    /**
     * From the currently selected custom preset file, return the full string of the preset file name.
     * @return The string of the file name such as "\sdcard\fluxfolder\wifi_settings.settings"
     */
//    public static String get_selected_customp_file_path(presetFileAdapter custom_preset_adapter,
//                                                         RecyclerView custom_preset_recyclerview,
//                                                         Activity activity, Context context)
//    {
//        String answer = null;
//
//        if (null == custom_preset_adapter || null == custom_preset_recyclerview ||
//                null == activity || null == context)
//        {
//            elogger(tag, "cannot access preset adapter to get file name.");
//            return answer;
//        }
//
//        // Get index of selected card.
//        int selected = custom_preset_adapter.getLastSelected();
//        if (-1 == selected)
//        {
//            logger(tag, "No item selected.");
//            FluxUtility.makeToast("No preset selected.", activity, context);
//            return answer;
//        }
//
//        // Get the viewholder from the adapter.
//        RecyclerView.ViewHolder current_view = custom_preset_recyclerview.findViewHolderForAdapterPosition(selected);
//        if (null == current_view)
//        {
//            elogger(tag, "could not get current card view.");
//            return answer;
//        }
//
//        // Get the currently selected textview.
//        TextView file_name_view = current_view.itemView.findViewById(R.id.preset_card_text);
//        if (null == file_name_view)
//        {
//            elogger(tag, "could not get file view.");
//            return answer;
//        }
//
//        // Finally, get the actual string from the textview.
//        answer =  file_name_view.getText().toString();
//        if (null == answer)
//        {
//            elogger(tag, "could not get file name from viewholder.");
//            return answer;
//        }
//
//        // Add ".settings" extension back on.
//        answer = custom_preset_location + answer + "." + com.flux.basefragments.specatabs.custompresethelper.DeckRecyclerViewHelper.settings_suffix;
//
//        return answer;
//    }

    /**
     * Load the preset selected in the recycler view into the specA settings fields.
     * @param rootView The View in which we are acting.
     */
//    public void load_selected_preset(View rootView, presetFileAdapter custom_preset_adapter,
//                                            RecyclerView custom_preset_recyclerview, Activity activity,
//                                            Context context, RxSettings rx_settings,
//                                            LinearLayout extra_params)
//    {
//        if (null == custom_preset_adapter || null == activity || null == context ||
//                null == custom_preset_recyclerview || null == extra_params || null == rx_settings)
//        {
//            elogger(tag, "gave null args to load_custom_preset.");
//            return;
//        }
//
//        // Get the file path from the card selected in the preset recycler view.
//        final String file_name = com.flux.basefragments.specatabs.custompresethelper.DeckRecyclerViewHelper.get_selected_customp_file_path(custom_preset_adapter,
//                custom_preset_recyclerview, activity, context);
//        if (null == file_name)
//        {
//            elogger(tag, "unable to grab file name from selected preset.");
//            return;
//        }
//
//        // Get RxSettings object from selected file.
//        RxSettings selected_settings = com.flux.basefragments.specatabs.custompresethelper.DeckRecyclerViewHelper.get_settings_from_file(file_name);
//        if (null == selected_settings)
//        {
//            elogger(tag, "could not get settings from file: " + file_name);
//            return;
//        }
//
//        // Apply current radio series, model, and channel mode to the object pulled from file.
//        int retcode = selected_settings.set_radio_series_code(rx_settings.get_radio_series_code());
//        if (FAILURE == retcode)
//        {
//            elogger(tag, "could not set radio series.");
//            return;
//        }
//
//        retcode = selected_settings.set_radio_model_code(rx_settings.get_radio_model_code(),
//                selected_settings.get_radio_series_code());
//        if (FAILURE == retcode)
//        {
//            elogger(tag, "could not set radio model.");
//            return;
//        }
//
//        retcode = selected_settings.set_channel_mode(rx_settings.get_channel_mode(),
//                selected_settings.get_antenna_code(),
//                selected_settings.get_radio_series_code());
//        if (FAILURE == retcode)
//        {
//            elogger(tag, "could not set channel mode.");
//            return;
//        }
//
//        rx_settings = selected_settings;
//
//        // Load it.
//        gui_loader.gui_load_from_object(rootView, rx_settings, activity, extra_params);
//
//        FluxUtility.makeToast("Loaded preset.", activity, context);
//
//        return;
//    }


    /**
     * Once a valid preset file name is gotten from the user, this function should be called
     * to actually create the file and write the RxSettings contents.
     * @param file_name The base file name such as "wifi_settings".
     * @param target_settings The RxSettings object to pull parameters from.
     */
//    public static void confirmed_create_preset(String file_name, RxSettings target_settings,
//                                               presetFileAdapter custom_preset_adapter,
//                                               Activity activity, Context context)
//    {
//        // Validate.
//        if (null == file_name || null == target_settings)
//        {
//            elogger(tag, "gave null args to confirmed_create_preset");
//            return;
//        }
//
//        if (!valid_base_file_name(file_name))
//        {
//            elogger(tag, "invalid file name.");
//            return;
//        }
//
//        // Get full file path to create.
//        final String fullpath = make_path_from_basename(file_name);
//        if (null == fullpath)
//        {
//            elogger(tag, "could not get path of file.");
//            return;
//        }
//
//        // This line will be written to the corresponding file.
//        final String file_contents = get_preset_csv_string(target_settings);
//        if (null == file_contents)
//        {
//            elogger(tag, "could not get csv from settings object.");
//            return;
//        }
//
//        logger(tag, "Will create " + fullpath + " and write: " + file_contents);
//
//        // Go ahead and create file.
//        new BootKali("touch " + fullpath).run();
//
//        try
//        {
//            FileWriter file_writer = new FileWriter(fullpath);
//            file_writer.write(file_contents);
//            file_writer.close();
//        }
//        catch (IOException e)
//        {
//            elogger(tag, "error occurred in file write.");
//            return;
//        }
//
//        FluxUtility.makeToast("Created: " + file_name, activity, context);
//
//        // Finally, add the new preset to the adapter.
//        custom_preset_adapter.update();
//
//        return;
//    }


    /**
     * From an RxSettings object, get a CSV string containing antenna, sample rate, frequency,
     * gain, and bandwidth information.
     * @param settings The RxSettings object we will get info from.
     * @return The CSV string, or null on error.
     */
//    public static String get_preset_csv_string(RxSettings settings)
//    {
//        String result = null;
//
//        // Validate.
//        if (null == settings)
//        {
//            elogger(tag, "cannot make csv string from null.");
//            return result;
//        }
//
//        // Build string to write into file.
//        result = "";
//        final String separator = ",";
//
//        // Get raw data, no units. Comma-separated.
//
//        // Antenna as string.
//        result += settings.get_instance_antenna_string() + separator;
//
//        // sample rate.
//        result += String.valueOf(settings.get_sample_rate()) + separator;
//
//        // frequency.
//        result += String.valueOf(settings.get_frequency()) + separator;
//
//        // gain.
//        result += String.valueOf(settings.get_gain()) + separator;
//
//        // bandwidth.
//        result += String.valueOf(settings.get_bandwidth()) + separator;
//
//        return result;
//    }

    /**
     * From a base preset file name such as "wifi_settings", get the full folder path with extension.
     * @param basename Base preset name such as "wifi_settings".
     * @return The full file path, or null on error.
     */
//    public static String make_path_from_basename(String basename)
//    {
//        String answer = null;
//        if (null == basename)
//        {
//            elogger(tag, "got null basename");
//            return answer;
//        }
//
//        if (basename.length() <= 0)
//        {
//            elogger(tag, "cannot get path from length 0 name.");
//            return answer;
//        }
//
//        answer = custom_preset_location + basename + "." + com.flux.basefragments.specatabs.custompresethelper.DeckRecyclerViewHelper.settings_suffix;
//
//        return answer;
//    }

    /**
     * Check that string only contains A-Z, a-z, 0-9, -, _
     * Limit to 50 chars max.
     * File must not already exist in presets.
     * @param file_name The string to be checked.
     * @return True if the string only contains those chars, false otherwise.
     */
//    public static boolean valid_base_file_name(String file_name)
//    {
//        // Validate.
//        if (null == file_name)
//        {
//            elogger(tag, "gave null file name to valid_base_file_name.");
//            return false;
//        }
//
//        // Check that it's within length requirement.
//        if (file_name.length() > max_deck_name_length)
//        {
//            return false;
//        }
//
//        // Only allowed A-Z, a-z, 0-9, -, _
//        for (int i = 0; i < file_name.length(); i++)
//        {
//            char current_char = file_name.charAt(i);
//
//            if (!(current_char >= 'A' && current_char <= 'Z') &&
//                    !(current_char >= 'a' && current_char <= 'z') &&
//                    !(current_char >= '0' && current_char <= '9') &&
//                    (current_char != '-') &&
//                    (current_char != '_'))
//            {
//                return false;
//            }
//        }
//
//        // Check if file already exists.
//        if (FluxUtility.kalibash_file_exists(make_path_from_basename(file_name)))
//        {
//            return false;
//        }
//
//        return true;
//    }


    /**
     * From the shown settings, make an RxSettings object, and then after asking the user to name
     * the file, create it.
     *
     * File CSV content fields will be:
     * [antenna (RX2 or TX\RX],[sample rate(S/s)],[frequency (Hz)],[gain (dB)],[bandwidth (Hz)]
     * Example file contents:
     * RX2,1000000,89100000,30,400000
     */
//    public static void create_custom_preset(View rootView, Activity activity, Context context,
//                                            presetFileAdapter custom_preset_adapter)
//    {
//        // Pull current settings into an RxSettings object, if valid.
//        RxSettings current_settings = SpecASettingsGuiGetter.get_settings_from_gui(rootView,
//                activity, context);
//        if (null == current_settings)
//        {
//            elogger(tag, "could not get valid settings from GUI.");
//            FluxUtility.makeToast("Error: could not make custom preset from invalid settings.",
//                    activity, context);
//            return;
//        }
//
//
//        /**
//         * Get a valid name from the user.
//         */
//
//        // Set up the input field, an EditText.
//        final EditText input = new EditText(context);
//
//        // Set margins on the input field.
//        int margin = (int) Math.ceil(FluxUtility.convertDpToPixel(20, context));
//        FrameLayout container = new FrameLayout(activity);
//        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.setMargins(margin, 0, margin,  margin * 2);
//        input.setLayoutParams(params);
//        container.addView(input);
//
//        // Expect normal keyboard text input.
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
//
//        input.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count)
//            {
//                if (!valid_base_file_name(s.toString()))
//                {
//                    input.setError("Error: invalid file name.");
//                }
//                else
//                {
//                    input.setError(null);
//                }
//            }
//        });
//
//        // Create the dialog to show the file name input field.
//        final AlertDialog dialog = new AlertDialog.Builder(context)
//                .setView(container)
//                .setTitle("Name Preset")
//                .setMessage("(A-Z, a-z, 0-9, -, _)\nLength limit: <= 50 characters")
//                .setPositiveButton("Create", null) //Set to null. We override the onclick
//                .setNegativeButton(android.R.string.cancel, null)
//                .setCancelable(false)
//                .create();
//
//        // Must set behavior on show, to be able to keep dialog up after click.
//        dialog.setOnShowListener(new DialogInterface.OnShowListener()
//        {
//            @Override
//            public void onShow(DialogInterface dialogInterface)
//            {
//                // Set behavior for positive button ("Create").
//                Button button_pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
//                button_pos.setOnClickListener(new View.OnClickListener()
//                {
//                    @Override
//                    public void onClick(View view)
//                    {
//                        final String file_name = input.getText().toString();
//
//                        if (valid_base_file_name(file_name))
//                        {
//                            // Create the given file.
//                            confirmed_create_preset(file_name, current_settings, custom_preset_adapter, activity, context);
//
//                            dialog.dismiss();
//                        }
//
//                    }
//                });
//
//                // Set behavior for negative button ("Cancel").
//                Button button_neg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
//                button_neg.setOnClickListener(new View.OnClickListener()
//                {
//                    @Override
//                    public void onClick(View view)
//                    {
//                        // Just close dialog.
//                        dialog.cancel();
//                    }
//                });
//            }
//        });
//
//        // Finally, show the dialog to the user.
//        dialog.show();
//
//        return;
//    }

}

// End of file.
