package com.goon.flashcards.ListHelper;

import static com.goon.flashcards.ListHelper.DeckRecyclerViewHelper.get_deck_list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.goon.flashcards.R;

import java.util.Collections;
import java.util.List;

/**
 * Pulls data from the deck file folder and populates the adapter with it, displaying
 * all relevant file info.
 */
public class DeckFileAdapter extends RecyclerView.Adapter<DeckFileAdapter.ViewHolder>
{
    // This is used to inflate view cards.
    Context context = null;

    // Differentiate backgrounds for selected v. unselected cards.
    Drawable selected_drawable = null;
    Drawable unselected_drawable = null;

    // List of file data.
    List<DeckFileData> list = Collections.emptyList();

    // Keep track of what is being selected. Used to load and delete decks.
    private static int adapter_last_selected = -1;

    /**
     * The ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * Identifies different fields on the given layout card for individual decks.
     */
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        // Each card will display file name and info.
        TextView file_name = null;
        TextView num_cards_str = null;

        // This is the view in which data will be placed.
        View cardView = null;

        // Constructor.
        ViewHolder(View itemView)
        {
            super(itemView);

            // Find all items that need to be dynamically populated.
            file_name = (TextView) itemView.findViewById(R.id.deck_title);
            num_cards_str = (TextView) itemView.findViewById(R.id.num_cards_str);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    // Need to keep track of which one is selected.
                    adapter_last_selected = getAdapterPosition();

                    // Notify that something was selected, so its card color can change to reflect it.
                    notifyDataSetChanged();
                }
            });

            cardView  = itemView;
        }
    }

    /**
     * Adapter constructor.
     * @param list The list of data to use to populate the adapter.
     * @param context The view in which we are acting.
     * @param selected_drawable The drawable that will be used on item select.
     * @param unselected_drawable The drawable for non-selected items.
     */
    public DeckFileAdapter(List<DeckFileData> list, Context context,
                           Drawable selected_drawable, Drawable unselected_drawable)
    {
        this.list = list;
        this.context = context;
        this.selected_drawable = selected_drawable;
        this.unselected_drawable = unselected_drawable;
    }

    // Inflate the card into the individual view.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Using the context, inflate the view card.
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View deckView = inflater.inflate(R.layout.deck_file_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(deckView);

        return viewHolder;
    }

    // Bind the data (list of deck names - iqFileData) to the view.
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position)
    {
        // Set all info given from the iqFileData object.
        viewHolder.file_name.setText(list.get(position).file_name);
        viewHolder.num_cards_str.setText(list.get(position).num_cards_str);

        // This function iterates over all cards. Depending on what item in the list was selected,
        // change that card's color.
        if (adapter_last_selected == position)
        {
            // Change selected item background color.
            viewHolder.cardView.setBackground(selected_drawable);
        }
        else
        {
            // Change  unselected item background color.
            viewHolder.cardView.setBackground(unselected_drawable);
        }

        return;
    }

    /**
     * Gets the number of deck objects that we need to display.
     * @return The integer size of the data list.
     */
    @Override
    public int getItemCount()
    {
        return list.size();
    }

    /**
     * Returns the last selected card by index, starting from the top of the recycler view.
     * @return Last selected index, possibly -1 if nothing is selected.
     */
    public int getLastSelected()
    {
        return adapter_last_selected;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Removes the selected data from the list, and updates the adapter.
     * @param position
     */
    public void removeAt(int position)
    {
        list.remove(position);
        adapter_last_selected = -1; // reset item selection.

        // Remove the object's card from the GUI.
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());

        return;
    }

    /**
     * Refreshes the list from the custom deck directory, and updates the adapter.
     */
    public void update()
    {
        list.clear();
        list = get_deck_list();

        notifyDataSetChanged();

        return;
    }

}

// End of file.
