package dev.dutchy.runelite.gear.guide;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.SetupType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuidesTest {

    @Test
    void everyGuideIdHasAGuideAndTutorialsCarryASample() {
        assertEquals(GuideId.values().length, Guides.all().size());
        for (Guide guide : Guides.all()) {
            assertTrue(guide.length() > 0, guide.title());
            assertEquals(guide.id().isTutorial(), guide.needsSample(), guide.title());
        }
        assertEquals(SetupType.GEAR, Guides.tutorialFor(SetupType.GEAR).sampleType().orElseThrow());
        assertEquals(GuideId.CONTENTS_CUSTOM, Guides.contentsGuideFor(SetupType.CUSTOM));
    }

    @Test
    void aTutorialReusesThePageStepsAndMovesThroughThePagesInOrder() {
        Guide tutorial = Guides.of(GuideId.TUTORIAL_GEAR);
        List<GuideStep> steps = tutorial.steps();
        assertEquals(HelpTopic.TUTORIAL_GEAR, steps.get(0).topic());
        assertFalse(steps.get(0).anchored(), "narrative steps point at nothing");
        assertEquals(GuidePage.LIST, steps.get(0).page());

        List<GuidePage> pages = steps.stream().map(GuideStep::page).distinct().collect(Collectors.toList());
        assertEquals(List.of(GuidePage.LIST, GuidePage.EDITOR, GuidePage.CONTENTS, GuidePage.SLOT, GuidePage.BANK_PICTURE), pages);
        assertEquals(HelpTopic.TUTORIAL_DONE, steps.get(steps.size() - 1).topic());

        List<HelpTopic> pageGuide = Guides.of(GuideId.CONTENTS_GEAR).steps().stream().map(GuideStep::topic).collect(Collectors.toList());
        List<HelpTopic> inTutorial = steps.stream().filter(step -> step.page() == GuidePage.CONTENTS).map(GuideStep::topic).collect(Collectors.toList());
        assertEquals(pageGuide, inTutorial, "the contents page is explained with the same steps in both");
        assertTrue(Guides.of(GuideId.TUTORIAL_CUSTOM).steps().stream().anyMatch(step -> step.topic() == HelpTopic.EDITOR_ROWS));
        assertFalse(steps.stream().anyMatch(step -> step.topic() == HelpTopic.EDITOR_ROWS), "rows are a custom-only step");
    }

    @Test
    void topicsRenderAsTwoLineTooltipsAndGuidesRefuseToBeEmpty() {
        assertEquals("<html><b>Search</b><br>" + HelpTopic.SEARCH.body() + "</html>", HelpTopic.SEARCH.tooltip());
        assertThrows(IllegalArgumentException.class, () -> Guide.of(GuideId.LIST, List.of()));
    }

    @Test
    void progressRemembersCompletionsAndSamplesAreTaggedAndKeepable() {
        GuideProgress progress = GuideProgress.inMemory();
        assertFalse(progress.isCompleted(GuideId.LIST));
        progress.markCompleted(GuideId.LIST);
        progress.markCompleted(GuideId.LIST);
        assertEquals(Set.of(GuideId.LIST), progress.completed());

        GearSetup gear = SampleSetups.of(SetupType.GEAR);
        assertTrue(SampleSetups.isSample(gear));
        assertTrue(gear.hasVariants(), "the gear sample has a variant to explain");
        assertEquals(2, ((CustomContent) SampleSetups.of(SetupType.CUSTOM).content()).rows());
        assertFalse(SampleSetups.isSample(SampleSetups.kept(gear)));
        assertEquals(gear.content(), SampleSetups.kept(gear).content());
    }
}
