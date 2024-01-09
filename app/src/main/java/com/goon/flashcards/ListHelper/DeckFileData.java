package com.goon.flashcards.ListHelper;

// This defines individual data objects to be given to the file recycler view and adapter.
public class DeckFileData
{
    String file_name = null;
    String num_cards_str = null;

    /**
     * Constructor.
     * @param file_name The base name of the file minus the extension, such as "wifi_settings".
     * @param num_cards_str The number of cards in the deck.
     */
    public DeckFileData(String file_name, String num_cards_str)
    {
        this.file_name = file_name;
        this.num_cards_str = num_cards_str;
    }

}

// End of file.
