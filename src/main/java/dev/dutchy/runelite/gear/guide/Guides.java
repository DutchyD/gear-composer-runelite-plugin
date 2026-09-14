package dev.dutchy.runelite.gear.guide;

import dev.dutchy.runelite.gear.content.SetupType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.dutchy.runelite.gear.guide.GuidePage.*;
import static dev.dutchy.runelite.gear.guide.GuideStep.at;
import static dev.dutchy.runelite.gear.guide.GuideStep.tell;
import static dev.dutchy.runelite.gear.guide.HelpTopic.*;
import static dev.dutchy.runelite.gear.guide.HelpTopic.DIVIDER;

/** The catalogue: page guides written once, tutorials assembled from them with narrative steps in between. */
public final class Guides {

    private Guides() {
    }

    public static Guide of(GuideId id) {
        Objects.requireNonNull(id, "id");
        switch (id) {
            case HOME:
                return Guide.of(id, List.of(tell(HOME_PAGE, GuidePage.CURRENT), at(ACCOUNT_CARD)));
            case LIST:
                return Guide.of(id, List.of(tell(LIST_PAGE, GuidePage.CURRENT), at(NEW_SETUP), at(VIEWED_ACCOUNT), at(SEARCH), at(SECTION_HEADER),
                        at(SETUP_TILE), at(NEW_SECTION), at(TILE_STYLE), at(BULK_DELETE), at(OVERFLOW_MENU), at(STATUS_LINE)));
            case EDITOR:
                return Guide.of(id, editorSteps(GuidePage.CURRENT, false));
            case CONTENTS_GEAR:
                return Guide.of(id, gearContentsSteps(GuidePage.CURRENT));
            case CONTENTS_BANK:
                return Guide.of(id, bankContentsSteps(GuidePage.CURRENT));
            case CONTENTS_CUSTOM:
                return Guide.of(id, customContentsSteps(GuidePage.CURRENT));
            case SLOT:
                return Guide.of(id, slotSteps(GuidePage.CURRENT));
            case DIVIDER:
                return Guide.of(id, List.of(tell(DIVIDER_PAGE, GuidePage.CURRENT), at(DIVIDER_TEXT), at(DIVIDER_COLUMNS), at(DIVIDER_ALIGN), at(DIVIDER_LINE), at(DIVIDER_SAVE)));
            case COMPARE:
                return Guide.of(id, List.of(tell(COMPARE_PAGE, GuidePage.CURRENT), at(COMPARE_TARGETS)));
            case SHARE:
                return Guide.of(id, List.of(tell(SHARE_PAGE, GuidePage.CURRENT), at(SHARE_LEDGER), at(SHARE_ACTIONS)));
            case HISTORY:
                return Guide.of(id, List.of(tell(HISTORY_PAGE, GuidePage.CURRENT)));
            case HOTKEY:
                return Guide.of(id, List.of(tell(HOTKEY_PAGE, GuidePage.CURRENT)));
            case IMPORT:
                return Guide.of(id, List.of(tell(IMPORT_PAGE, GuidePage.CURRENT)));
            case BACKUPS:
                return Guide.of(id, List.of(tell(BACKUPS_PAGE, GuidePage.CURRENT)));
            case SHARE_CODE:
                return Guide.of(id, List.of(tell(SHARE_CODE_PAGE, GuidePage.CURRENT)));
            case CELL_SOURCE:
                return Guide.of(id, List.of(tell(CELL_SOURCE_PAGE, GuidePage.CURRENT)));
            case TUTORIAL_GEAR:
                return Guide.tutorial(id, SetupType.GEAR, tutorial(TUTORIAL_GEAR, gearContentsSteps(CONTENTS),
                        List.of(tell(BANK_VIEW, BANK_PICTURE), at(BANK_AMOUNTS, BANK_PICTURE), at(BANK_PLACEHOLDERS, BANK_PICTURE),
                                tell(BANK_LEAVING, BANK_PICTURE))));
            case TUTORIAL_BANK:
                return Guide.tutorial(id, SetupType.BANK, tutorial(TUTORIAL_BANK, bankContentsSteps(CONTENTS),
                        List.of(tell(BANK_VIEW, BANK_PICTURE), at(BANK_AMOUNTS, BANK_PICTURE), at(BANK_PLACEHOLDERS, BANK_PICTURE),
                                tell(BANK_LEAVING, BANK_PICTURE))));
            default:
                return Guide.tutorial(id, SetupType.CUSTOM, tutorial(TUTORIAL_CUSTOM, customContentsSteps(CONTENTS),
                        List.of(tell(BANK_VIEW, BANK_PICTURE), at(BANK_CELL_BANDS, BANK_PICTURE), at(BANK_AMOUNTS, BANK_PICTURE),
                                tell(BANK_LEAVING, BANK_PICTURE))));
        }
    }

    /** The same guide made to run anywhere: it opens its page on a sample instead of whatever is on screen. */
    public static Guide standalone(GuideId id) {
        Guide base = of(id);
        if (base.needsSample()) {
            return base;
        }
        GuidePage page = pageOf(id);
        List<GuideStep> moved = base.steps().stream()
                .map(step -> step.page() == GuidePage.CURRENT ? new GuideStep(step.topic(), page, step.anchored()) : step)
                .collect(Collectors.toList());
        return Guide.tutorial(id, sampleTypeOf(id), moved);
    }

    private static GuidePage pageOf(GuideId id) {
        switch (id) {
            case HOME:
                return GuidePage.HOME;
            case EDITOR:
                return GuidePage.EDITOR;
            case CONTENTS_GEAR:
            case CONTENTS_BANK:
            case CONTENTS_CUSTOM:
                return GuidePage.CONTENTS;
            case SLOT:
                return GuidePage.SLOT;
            case DIVIDER:
                return GuidePage.DIVIDER;
            case COMPARE:
                return GuidePage.COMPARE;
            case SHARE:
                return GuidePage.SHARE;
            case HISTORY:
                return GuidePage.HISTORY;
            case HOTKEY:
                return GuidePage.HOTKEY;
            case IMPORT:
                return GuidePage.IMPORT;
            case BACKUPS:
                return GuidePage.BACKUPS;
            case SHARE_CODE:
                return GuidePage.SHARE_CODE;
            case CELL_SOURCE:
                return GuidePage.CELL_SOURCE;
            case LIST:
            default:
                return GuidePage.LIST;
        }
    }

    private static SetupType sampleTypeOf(GuideId id) {
        switch (id) {
            case CONTENTS_BANK:
                return SetupType.BANK;
            case CONTENTS_CUSTOM:
            case CELL_SOURCE:
                return SetupType.CUSTOM;
            default:
                return SetupType.GEAR;
        }
    }

    public static List<Guide> all() {
        return Arrays.stream(GuideId.values()).map(Guides::of).collect(Collectors.toList());
    }

    public static Guide tutorialFor(SetupType type) {
        switch (Objects.requireNonNull(type, "type")) {
            case GEAR:
                return of(GuideId.TUTORIAL_GEAR);
            case BANK:
                return of(GuideId.TUTORIAL_BANK);
            default:
                return of(GuideId.TUTORIAL_CUSTOM);
        }
    }

    public static GuideId contentsGuideFor(SetupType type) {
        switch (Objects.requireNonNull(type, "type")) {
            case GEAR:
                return GuideId.CONTENTS_GEAR;
            case BANK:
                return GuideId.CONTENTS_BANK;
            default:
                return GuideId.CONTENTS_CUSTOM;
        }
    }

    private static List<GuideStep> tutorial(HelpTopic intro, List<GuideStep> contents, List<GuideStep> bank) {
        List<GuideStep> steps = new ArrayList<>();
        steps.add(tell(intro, LIST));
        steps.add(tell(TUTORIAL_SAMPLE, LIST));
        steps.addAll(editorSteps(EDITOR, intro == TUTORIAL_CUSTOM));
        steps.addAll(contents);
        steps.addAll(slotSteps(SLOT));
        steps.addAll(bank);
        steps.add(tell(TUTORIAL_DONE, LIST));
        return steps;
    }

    private static List<GuideStep> editorSteps(GuidePage page, boolean withRows) {
        List<GuideStep> steps = new ArrayList<>(List.of(tell(EDITOR_PAGE, page), at(EDITOR_NAME, page), at(EDITOR_TYPE, page)));
        if (withRows) {
            steps.add(at(EDITOR_ROWS, page));
        }
        steps.addAll(List.of(at(EDITOR_ICON, page), at(EDITOR_COLOUR, page), at(EDITOR_TAGS, page), at(EDITOR_NOTES, page),
                at(EDITOR_SPELLBOOK, page), at(EDITOR_QUICK_PRAYERS, page), at(EDITOR_SAVE, page)));
        return steps;
    }

    private static List<GuideStep> gearContentsSteps(GuidePage page) {
        return List.of(tell(CONTENTS_PAGE, page), at(CONTENTS_TOOLS, page), at(VARIANTS, page), at(SYNC, page), at(EQUIPMENT, page), at(INVENTORY, page),
                at(DIVIDER, page), at(LEDGER, page));
    }

    private static List<GuideStep> bankContentsSteps(GuidePage page) {
        return List.of(tell(CONTENTS_PAGE, page), at(CONTENTS_TOOLS, page), at(VARIANTS, page), at(BANK_SIDES, page), at(DIVIDER, page), at(SYNC, page),
                at(LEDGER, page));
    }

    private static List<GuideStep> customContentsSteps(GuidePage page) {
        return List.of(tell(CONTENTS_PAGE, page), at(CONTENTS_TOOLS, page), at(VARIANTS, page), at(LAYOUT_MAP, page), at(ROW_TABS, page), at(CELL, page),
                at(CELL_MENU, page), at(EMPTY_CELL, page), at(LEDGER, page));
    }

    private static List<GuideStep> slotSteps(GuidePage page) {
        return List.of(tell(SLOT_PAGE, page), at(SLOT_ITEM, page), at(SLOT_MATCH, page), at(SLOT_NOTED, page), at(SLOT_ALTERNATIVES, page),
                at(SLOT_AMOUNT, page), at(SLOT_SAVE, page));
    }
}
