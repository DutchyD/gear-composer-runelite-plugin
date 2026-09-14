package dev.dutchy.runelite;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.UntargettedBinding;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.requirements.CurrentQuickPrayers;
import dev.dutchy.runelite.gear.ui.PageParts;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Walks the session's dependency graph the way Guice would, without a client: every collaborator must be bound or buildable. */
class SessionWiringTest {

    private static final List<String> PROVIDED_BY_THE_CLIENT = List.of("net.runelite.", "com.google.", "okhttp3.", "java.", "javax.");

    @Test
    void everyCollaboratorOfASessionIsBoundOrBuildable() {
        Map<Key<?>, Binding<?>> bindings = new HashMap<>();
        List<Element> elements = new ArrayList<>(Elements.getElements(new GearComposerPlugin()));
        elements.addAll(Elements.getElements(new SessionModule(new GearSetupBook(), Path.of("build", "tmp", "wiring"))));
        for (Element element : elements) {
            if (element instanceof Binding) {
                bindings.put(((Binding<?>) element).getKey(), (Binding<?>) element);
            }
        }

        List<String> problems = new ArrayList<>();
        Set<Key<?>> seen = new HashSet<>();
        Deque<Key<?>> todo = new ArrayDeque<>();
        todo.push(Key.get(GearComposerSession.class));
        while (!todo.isEmpty()) {
            Key<?> key = todo.pop();
            if (!seen.add(key)) {
                continue;
            }
            Binding<?> binding = bindings.get(key);
            if (binding instanceof LinkedKeyBinding) {
                todo.push(((LinkedKeyBinding<?>) binding).getLinkedKey());
                continue;
            }
            if (binding instanceof ProviderInstanceBinding) {
                for (Dependency<?> dependency : ((ProviderInstanceBinding<?>) binding).getDependencies()) {
                    todo.push(dependency.getKey());
                }
                continue;
            }
            if (binding instanceof InstanceBinding) {
                continue;
            }
            Class<?> type = key.getTypeLiteral().getRawType();
            if (binding != null && !(binding instanceof UntargettedBinding)) {
                continue;
            }
            if (PROVIDED_BY_THE_CLIENT.stream().anyMatch(type.getName()::startsWith)) {
                continue;
            }
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                problems.add("No binding for " + type.getName());
                continue;
            }
            Constructor<?> constructor = injectableConstructor(type);
            if (constructor == null) {
                problems.add("No injectable constructor on " + type.getName());
                continue;
            }
            for (Type parameter : constructor.getGenericParameterTypes()) {
                todo.push(Key.get(parameter));
            }
        }
        assertEquals(List.of(), problems);
        assertTrue(seen.contains(Key.get(PageParts.class)), "the walk reaches the sidebar");
        assertTrue(seen.contains(Key.get(CurrentQuickPrayers.class)), "and the bank watches");
        assertTrue(seen.size() > 60, "seen " + seen.size());
    }

    private static Constructor<?> injectableConstructor(Class<?> type) {
        Constructor<?> fallback = null;
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class) || constructor.isAnnotationPresent(com.google.inject.Inject.class)) {
                return constructor;
            }
            if (constructor.getParameterCount() == 0 && !Modifier.isPrivate(constructor.getModifiers())) {
                fallback = constructor;
            }
        }
        return fallback;
    }
}
