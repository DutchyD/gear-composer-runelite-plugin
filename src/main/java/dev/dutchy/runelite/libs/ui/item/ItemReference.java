package dev.dutchy.runelite.libs.ui.item;

import lombok.Value;
import java.util.Objects;

public interface ItemReference {

    static ItemReference byId(int id) {
        return new ById(ItemId.of(id));
    }

    static ItemReference byId(ItemId id) {
        return new ById(id);
    }

    static ItemReference byName(String name) {
        return new ByName(name);
    }

    String describe();

    @Value
    class ById implements ItemReference {
        ItemId id;


        public ById(ItemId id) {
            Objects.requireNonNull(id, "id");
            this.id = id;
        }

        @Override
        public String describe() {
            return "item " + id;
        }
    }

    @Value
    class ByName implements ItemReference {
        String name;


        public ByName(String name) {
            Objects.requireNonNull(name, "name");
            name = name.strip();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Item name must not be blank");
            }
            this.name = name;
        }

        @Override
        public String describe() {
            return '"' + name + '"';
        }
    }
}
