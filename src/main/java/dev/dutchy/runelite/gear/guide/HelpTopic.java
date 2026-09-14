package dev.dutchy.runelite.gear.guide;

import java.util.Objects;

/** One explanation of one thing on screen: a title and a sentence, read by tooltips and by guide steps alike. */
public enum HelpTopic {

    // list page
    LIST_PAGE("The setup list", "Every setup you have made, grouped in sections. Click a tile to show it in the bank."),
    NEW_SETUP("New setup", "Starts a setup: pick a name and a layout type, then fill its slots on the next page."),
    TILE_STYLE("List style", "Cycles the list between a grid of tiles, a denser grid, and a list with full names."),
    OVERFLOW_MENU("More", "Import and export files, backups, share codes and the guides live here."),
    BULK_DELETE("Select to delete", "Turns on selection so several setups or sections can be deleted at once."),
    VIEWED_ACCOUNT("Whose setups", "The list shows this owner's setups plus the shared ones. All accounts goes back to the accounts screen."),
    HOME_PAGE("Accounts", "Every account the plugin has seen and the setups shared with all of them. Click a card to manage its setups, logged in or not."),
    ACCOUNT_CARD("Account card", "The name as last seen at login, a short id, and how many setups it holds. Its menu can forget an account that holds none."),
    SEARCH("Search", "Filters setups by name or tag as you type. Use tag: to search tags only."),
    SECTION_HEADER("Section", "A named group of setups. Click to fold it, double-click to rename, right-click for more."),
    SECTION_ADD("Add to section", "Adds a setup to this section, empty or copied from what you wear right now."),
    SETUP_TILE("Setup tile", "Left-click shows the setup in the bank. Right-click opens its menu with edit, variants, duplicate, pin, hotkey, share and delete."),
    NEW_SECTION("New section", "Adds a section at the bottom. Sections can nest one level deep from their menu."),
    STATUS_LINE("Status line", "Says what just happened, and offers Undo for a few seconds after a change."),
    GUIDES("Guides", "Every guide and tutorial, with a tick beside the ones you have completed."),

    // editor
    EDITOR_PAGE("Setup editor", "Name, layout type and the details shown on the setup's tile and page."),
    EDITOR_NAME("Name", "What the setup is called on its tile and in the bank title."),
    EDITOR_TYPE("Layout type", "A Gear Layout is equipment plus inventory. A Bank Layout is two inventories. A Custom Layout is your own grid of cells."),
    EDITOR_ROWS("Rows", "How many rows the custom grid has, each two cells wide. You can change it later."),
    EDITOR_ICON("Icon", "An item drawn on the tile. Leave it empty for a lettered tile."),
    EDITOR_COLOUR("Colour", "A colour bar on the tile, for telling groups of setups apart at a glance."),
    EDITOR_TAGS("Tags", "Words to search by. Type one and press Enter."),
    EDITOR_NOTES("Notes", "A reminder shown at the top of the setup's page."),
    EDITOR_SPELLBOOK("Spellbook", "Warns in the bank when you are on a different spellbook than the setup needs."),
    EDITOR_QUICK_PRAYERS("Quick prayers", "The prayers your quick-prayer set should hold. The bank warns when yours are set differently."),
    EDITOR_SAVE("Create or save", "Saves the setup and opens its page, or keeps your changes to an existing one."),

    // contents page
    CONTENTS_PAGE("Setup contents", "The slots of this setup. Click a slot to set or change its item, drag to move, shift-click to select several."),
    CONTENTS_TOOLS("Page tools", "Find items to drag in, compare with another setup, share it, or look at earlier versions."),
    SYNC("Sync from game", "Replaces the slots with what you are wearing and carrying right now."),
    EQUIPMENT("Equipment", "The worn slots in their in-game arrangement. In the bank they draw on the left side."),
    INVENTORY("Inventory", "Twenty-eight slots in the in-game arrangement. In the bank they draw on the right side."),
    BANK_SIDES("Two sides", "A Bank Layout is two inventories: the left side and the right side of the bank."),
    VARIANTS("Variants", "Every version of this setup, such as Budget or Best-in-slot, with a preview of each. The lit one is shown below and in the bank; click another to show it, right-click for more. In the bank the variants sit as tabs above the layout; the arrows or the mouse wheel scroll them when there are many."),
    VARIANT_TOOLS("Variant tools", "Start a new variant as a copy, empty, or from what you wear; fold the list away when you are done."),
    LEDGER("Ledger", "Total value, carried weight and worn bonuses, recomputed on every edit."),
    LAYOUT_MAP("Layout map", "The custom grid in its real shape. Click a cell to jump to it below."),
    ROW_TABS("Row tabs", "One tab per row of the grid; only the chosen row's two cells are shown below."),
    CELL("Cell", "One cell of the grid: an equipment set or an inventory, with its own name and menu."),
    CELL_MENU("Cell menu", "Fill the cell from another setup or from the game, rename it, clear it, or empty it."),
    EMPTY_CELL("Empty cell", "Choose what this cell holds: an equipment set or an inventory."),
    DIVIDER("Dividers", "Labelled breaks above a row of a grid, each across the columns it covers. In the bank the row grows a small header strip for them, on both sides, so the sides stay lined up. Click one to edit it, click a free stretch to add one, right-click for quick changes."),
    FINDER("Find items", "Search for an item and drag a result onto a slot, or click it to fill the selected slot."),
    SELECTION("Selection", "Copy, paste and clear the selected slots. Ctrl+C, Ctrl+V and Delete work too."),

    // divider editor
    DIVIDER_PAGE("Divider editor", "The text of a divider, which columns it covers, and where the text sits."),
    DIVIDER_TEXT("Text", "What the divider says, up to 24 characters."),
    DIVIDER_COLUMNS("Columns", "Which columns of the row the divider covers. Click a column to extend or shrink it; other dividers on the row make room."),
    DIVIDER_ALIGN("Text sits", "Whether the text sits at the left, the centre, or the right of the columns it covers."),
    DIVIDER_LINE("Line", "Whether a line is drawn under the text across the columns, or the text stands alone."),
    DIVIDER_SAVE("Save", "Keeps the divider. Remove takes it off the row."),

    // slot editor
    SLOT_PAGE("Slot editor", "Which item this slot asks for and how much of it."),
    SLOT_ITEM("Item", "Type a name or id and pick a result."),
    SLOT_MATCH("Match", "Any variant accepts every dose or charge and shows the fullest. This first shows this one when you have it. Use up shows the emptiest. Exact wants that precise item."),
    SLOT_NOTED("Withdraw as", "Ask for the item itself, or its bank note for bulk trips."),
    SLOT_ALTERNATIVES("Alternatives", "Other items that will do when the first is missing, in order of preference."),
    SLOT_AMOUNT("Amount", "How many to withdraw. Bank means whatever the bank holds; MAX and K, M, B shorthand work."),
    SLOT_SAVE("Save", "Keeps the slot. Empty slot clears it."),

    // bank behaviour
    BANK_VIEW("In the bank", "With the setup active the bank shows only these slots, in this arrangement, in place of the tab you had open."),
    BANK_AMOUNTS("Amounts", "Each slot with an amount reads held over required, counting every dose or charge. Amber means only other doses make up the need; hover for the breakdown and right-click to show one of them. Red means you are short."),
    BANK_PLACEHOLDERS("Placeholders", "Items you do not have are drawn faded so you can see what is missing."),
    BANK_CELL_BANDS("Cell bands", "Each grid row draws as a band with the cell names written on the blank row above it."),
    BANK_LEAVING("Back to the bank", "Clicking a tab, searching, or closing the bank turns the setup off again."),

    // other pages
    COMPARE_PAGE("Compare", "Slot-by-slot differences against another setup or against what you wear, with the value and stat deltas."),
    COMPARE_TARGETS("Against", "Pick which setup to compare with. Worn now uses what you are wearing and carrying."),
    SHARE_PAGE("Share", "Save the setup as a picture, or copy it as an image or text."),
    SHARE_LEDGER("Include", "Whether value, weight and stats go along with the picture or text."),
    SHARE_ACTIONS("Share actions", "Save image writes a PNG to the screenshots folder. Copy puts it on the clipboard."),
    HISTORY_PAGE("History", "Earlier versions of this setup, newest first. Restore brings one back."),
    HOTKEY_PAGE("Hotkey", "Press the key combination that should show this setup in the bank."),
    IMPORT_PAGE("Import", "Setups read from a file, added beside what you already have."),
    BACKUPS_PAGE("Backups", "Rolling copies of your whole book, taken as you make changes. Restore replaces the book."),
    SHARE_CODE_PAGE("Share code", "A setup pasted from a share code. Pick the section it goes in."),
    CELL_SOURCE_PAGE("Fill from setup", "Every equipment set or inventory in your book that fits this cell. The items are copied."),

    // tutorials
    TUTORIAL_GEAR("Gear Layout", "A Gear Layout is one loadout: what you wear and what you carry. It is the type for a boss trip or a skilling run."),
    TUTORIAL_BANK("Bank Layout", "A Bank Layout is two inventories side by side. It is the type for supplies, herb runs and bulk withdrawals."),
    TUTORIAL_CUSTOM("Custom Layout", "A Custom Layout is a grid of cells, each an equipment set or an inventory. It is the type for a trip with several loadouts."),
    TUTORIAL_SAMPLE("The sample", "This tutorial made a sample setup to walk through. It is removed at the end unless you keep it."),
    TUTORIAL_DONE("That is the tour", "Open a bank, click the tile, and the bank arranges itself. Every page has a ? button for a closer look.");

    private final String title;
    private final String body;

    HelpTopic(String title, String body) {
        this.title = Objects.requireNonNull(title, "title");
        this.body = Objects.requireNonNull(body, "body");
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    /** The two-line tooltip: bold title over the sentence. */
    public String tooltip() {
        return "<html><b>" + escape(title) + "</b><br>" + escape(body) + "</html>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
