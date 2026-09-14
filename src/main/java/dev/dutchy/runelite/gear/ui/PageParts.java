package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.config.ViewSettings;
import dev.dutchy.runelite.gear.content.DropRule;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.selector.ItemSelectorFactory;
import lombok.Value;
import lombok.experimental.Accessors;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Objects;

/** What the sidebar pages are drawn from: pictures, pickers, prices, preferences and the clock. */
@Value
@Accessors(fluent = true)
public class PageParts {

    ItemIconFactory icons;
    ItemSelectorFactory selectors;
    SetupTypeArtwork typeArtwork;
    PrayerArtwork prayerArtwork;
    EquipmentSlotArtwork slotArtwork;
    ItemFactsSource facts;
    ViewSettings view;
    DropRule dropRule;
    Clock clock;

    @Inject
    public PageParts(ItemIconFactory icons, ItemSelectorFactory selectors, SetupTypeArtwork typeArtwork,
                     PrayerArtwork prayerArtwork, EquipmentSlotArtwork slotArtwork, ItemFactsSource facts,
                     ViewSettings view, DropRule dropRule, Clock clock) {
        this.icons = Objects.requireNonNull(icons, "icons");
        this.selectors = Objects.requireNonNull(selectors, "selectors");
        this.typeArtwork = Objects.requireNonNull(typeArtwork, "typeArtwork");
        this.prayerArtwork = Objects.requireNonNull(prayerArtwork, "prayerArtwork");
        this.slotArtwork = Objects.requireNonNull(slotArtwork, "slotArtwork");
        this.facts = Objects.requireNonNull(facts, "facts");
        this.view = Objects.requireNonNull(view, "view");
        this.dropRule = Objects.requireNonNull(dropRule, "dropRule");
        this.clock = Objects.requireNonNull(clock, "clock");
    }
}
