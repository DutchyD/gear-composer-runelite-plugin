# Gear Composer

A RuneLite plugin that rearranges your bank into the shape of a gear setup, so the things you need
are sitting where you expect them instead of scattered across tabs.

## What it does

A setup holds an equipment cross and an inventory, or two bank sides if that suits you better. You
can group setups into sections, and reorder, pin, colour, tag and search them.

Turn a setup on and the bank redraws itself in that layout. Items you own keep their normal withdraw
options. If the same item turns up in more than one slot it gets drawn in each of them. Anything you
don't own is greyed out, and the amounts never go above what the bank is actually holding.

"Sync from game" fills a setup in from whatever you happen to be wearing and carrying at the time.
Variant matching treats charges, doses and degrade states as the same item, so you can list a few
alternatives for a slot in the order you'd rather have them.

Editing is drag and drop, either between slots or straight out of the search results. There's
multi-select copy and paste, and a quick fill. Anything you do can be undone, and each setup keeps
its own history.

Hotkeys let you flip between setups without opening the sidebar. If a setup needs a different
spellbook you get a banner about it, plus a note in the bank title.

Setups live in your RuneLite profile and get backed up automatically. You can export and import them
as JSON, and a setup can either belong to one character or be shared across all of them.

Worth saying plainly: this never clicks or types for you. All it does is rearrange the bank interface
and draw a few overlays on top.

## How this differs from other bank plugins

There are already several plugins in this area, so it's fair to ask where this one sits.

RuneLite ships **Bank Tags**, which groups items under tags and gives each tag its own bank tab, and
**Inventory Setups**, which records a loadout and can highlight or filter the bank for it. The Plugin
Hub has **Bank Tag Layouts**, which lets you pin a bank tag's items into a fixed arrangement.

Gear Composer overlaps with all three. The difference is that here the setup and the layout are the
same thing. A setup is shaped like the trip you're packing for, an equipment cross sat next to an
inventory, and the bank gets redrawn into that shape rather than into a grid of tagged items. What
it adds on top of that:

- Variants, so one setup can cover a solo trip, a team trip and a learner trip. You switch between
  them from tabs above the layout, or with a hotkey.
- Alternatives per slot, where charges, doses and degrade states all count as the same item, so a
  slot shows whichever version you happen to have on you.
- Twins, so an item you want in several places gets drawn in all of them while still being a single
  stack in the bank.
- Revisions, ten per variant, kept on disk, so you can walk a setup back long after the session you
  changed it in.

If all you want is tag-based tabs, or just a recorded loadout, or one fixed arrangement, the plugins
above do less and are probably the better fit.

## What it does to the bank interface

This part is mainly for anyone reviewing the plugin. Rearranging a game interface is the sort of
thing worth being precise about, so here is exactly what happens.

Everything the plugin writes goes to the bank, group `BANKMAIN`, and within that only the item layer,
the title, the search box and the note button. It doesn't touch the inventory, equipment, spellbook,
prayer or combat interfaces at all, and it never sends the server anything you didn't click yourself.

**Item slots stay the size the game made them.** The numbers in `BankGeometry` are the ones the
game's own bank scripts use, 36x32 at the game's padding and origin. Nothing here ever makes a slot
bigger. `restoreSlotSizes` exists only to put a slot back to 36x32 if one of the plugin's earlier
frames changed it, and it runs before anything else gets drawn.

**The plugin's own widgets are tacked on the end.** The variant tabs, the scroll arrows and their
labels are all created after the last child the game built, and those are the only widgets the plugin
gives a position or a size to. It does rewrite which item each of the game's own item children shows,
and what actions that item carries, since that redrawing is the entire point of the plugin. Their
geometry is left alone.

**Withdraw clicks get retargeted, not invented.** The game ties a bank item widget to the slot it was
drawn for, and the withdraw scripts work off that tie. A twin is drawn on a widget tied to some other
slot, so `onMenuOptionClicked` rewrites the clicked entry's `param0` to point at the slot the item
really sits in, which it looks up with `ItemContainer.find`. That is retargeting a click you had
already made. It doesn't add an entry that talks to the server, and it can't withdraw something the
bank isn't holding.

**Menu entries the plugin adds never leave the client.** "Show variant", "Scroll left" and "Scroll
right" on the tab strip, along with the alternatives you get on a slot's right-click, are all
`MenuAction.RUNELITE` entries. Their handlers change what the bank draws, and that's it.

**Reordering the menu only reorders it.** If you switch on "Left-click withdraws the setup amount",
the withdraw option matching your setup's amount gets moved into the left-click position. It's picked
from the options the game had already put there for that slot. Nothing is added and nothing is
hidden.

## Building

```
./gradlew build
```

To start a development client with the plugin already loaded:

```
./gradlew run
```

The plugin targets Java 11, which is what the Plugin Hub builds with. Any JDK 11 or newer on your
`PATH` will do, since the compiler is pointed at release 11 rather than at whatever it defaults to.

## Repository layout

`src/main` is the plugin, and it's the only thing the Plugin Hub builds. `src/test` holds the tests,
which run headless and don't need a game client.

## Licence

BSD 2-Clause. See [LICENSE](LICENSE).
