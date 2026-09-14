package dev.dutchy.runelite.gear;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountFilterTest {

    private static List<String> names(List<GearSection> sections) {
        return sections.stream().flatMap(section -> section.setups().stream()).map(GearSetup::name).collect(Collectors.toList());
    }

    private static List<GearSection> sample() {
        return List.of(new GearSection(SectionId.random(), "S", List.of(
                GearSetup.named("Mine").withOwner(Owner.account("main")),
                GearSetup.named("Theirs").withOwner(Owner.account("iron")),
                GearSetup.named("Everyone").withOwner(Owner.shared()))));
    }

    @Test
    void anAccountSeesItsOwnAndTheSharedOnes() {
        assertEquals(List.of("Mine", "Everyone"), names(AccountFilter.apply(sample(), Owner.account("main"))));
    }

    @Test
    void theSharedListSeesSharedOnly() {
        assertEquals(List.of("Everyone"), names(AccountFilter.apply(sample(), Owner.shared())));
    }

    @Test
    void sectionsStayEvenWhenEmptied() {
        assertEquals(1, AccountFilter.apply(sample(), Owner.account("nobody")).size());
    }
}
