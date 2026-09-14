package dev.dutchy.runelite.gear.persistence;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;

/** Maps setup contents to and from plain transfer objects shared by every JSON document. */
public final class ContentCodec {

    private ContentCodec() {
    }

    public static class ContentDto {
        String type;
        Map<String, ItemDto> equipment;
        Map<String, ItemDto> inventory;
        Map<String, ItemDto> left;
        Map<String, ItemDto> right;
        @JsonAdapter(DividerListAdapter.class)
        List<DividerDto> inventoryDividers;
        @JsonAdapter(DividerListAdapter.class)
        List<DividerDto> leftDividers;
        @JsonAdapter(DividerListAdapter.class)
        List<DividerDto> rightDividers;
        Integer rows;
        List<CellDto> cells;
    }

    public static final class CellDto {
        String kind;
        String name;
        Map<String, ItemDto> equipment;
        Map<String, ItemDto> items;
        @JsonAdapter(DividerListAdapter.class)
        List<DividerDto> dividers;
    }

    public static final class DividerDto {
        int row;
        Integer from;
        Integer to;
        String label;
        String align;
        Boolean line;
    }

    /** Reads dividers as the list they are now, or as the row-to-label map older books wrote, and writes the list. */
    public static final class DividerListAdapter extends TypeAdapter<List<DividerDto>> {

        @Override
        public void write(JsonWriter out, List<DividerDto> dividers) throws IOException {
            if (dividers == null) {
                out.nullValue();
                return;
            }
            out.beginArray();
            for (DividerDto divider : dividers) {
                out.beginObject();
                out.name("row").value(divider.row);
                if (divider.from != null) {
                    out.name("from").value(divider.from);
                }
                if (divider.to != null) {
                    out.name("to").value(divider.to);
                }
                out.name("label").value(divider.label);
                if (divider.align != null) {
                    out.name("align").value(divider.align);
                }
                if (divider.line != null) {
                    out.name("line").value(divider.line);
                }
                out.endObject();
            }
            out.endArray();
        }

        @Override
        public List<DividerDto> read(JsonReader in) throws IOException {
            List<DividerDto> dividers = new ArrayList<>();
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            if (in.peek() == JsonToken.BEGIN_OBJECT) {
                in.beginObject();
                while (in.hasNext()) {
                    DividerDto divider = new DividerDto();
                    divider.row = Integer.parseInt(in.nextName());
                    divider.label = in.nextString();
                    dividers.add(divider);
                }
                in.endObject();
                return dividers;
            }
            in.beginArray();
            while (in.hasNext()) {
                DividerDto divider = new DividerDto();
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    switch (name) {
                        case "row":
                            divider.row = in.nextInt();
                            break;
                        case "from":
                            divider.from = in.nextInt();
                            break;
                        case "to":
                            divider.to = in.nextInt();
                            break;
                        case "label":
                            divider.label = in.nextString();
                            break;
                        case "align":
                            divider.align = in.nextString();
                            break;
                        case "line":
                            divider.line = in.nextBoolean();
                            break;
                        default:
                            in.skipValue();
                    }
                }
                in.endObject();
                dividers.add(divider);
            }
            in.endArray();
            return dividers;
        }
    }

    public static final class ItemDto {
        int id;
        Integer qty;
        String match;
        List<Integer> alts;
        Boolean noted;
    }

    public static ContentDto toDto(SetupContent content) {
        return write(content, new ContentDto());
    }

    /** Fills the document's content fields, leaving any others alone. */
    public static <D extends ContentDto> D write(SetupContent content, D dto) {
        dto.type = content.type().name();
        if (content instanceof GearContent) {
            GearContent gear = (GearContent) content;
            dto.equipment = new LinkedHashMap<>();
            gear.equipment().forEach((slot, item) -> dto.equipment.put(slot.name(), toDto(item)));
            dto.inventory = toDto(gear.inventory());
            dto.inventoryDividers = dividersOf(gear.inventory());
        } else if (content instanceof BankContent) {
            BankContent bank = (BankContent) content;
            dto.left = toDto(bank.left());
            dto.right = toDto(bank.right());
            dto.leftDividers = dividersOf(bank.left());
            dto.rightDividers = dividersOf(bank.right());
        } else if (content instanceof CustomContent) {
            CustomContent custom = (CustomContent) content;
            dto.rows = custom.rows();
            dto.cells = custom.cells().stream().map(ContentCodec::toDto).collect(Collectors.toList());
        }
        return dto;
    }

    public static SetupContent fromDto(ContentDto dto) {
        Objects.requireNonNull(dto, "content");
        SetupType type = SetupType.valueOf(require(dto.type, "setup type"));
        switch (type) {
            case GEAR:
                return new GearContent(equipmentFrom(dto.equipment), gridFrom(dto.inventory, dto.inventoryDividers));
            case BANK:
                return new BankContent(gridFrom(dto.left, dto.leftDividers), gridFrom(dto.right, dto.rightDividers));
            default:
                return customFrom(dto);
        }
    }

    private static Map<EquipmentSlot, SetupItem> equipmentFrom(Map<String, ItemDto> dto) {
        Map<EquipmentSlot, SetupItem> equipment = new EnumMap<>(EquipmentSlot.class);
        if (dto != null) {
            dto.forEach((slot, item) -> equipment.put(EquipmentSlot.valueOf(slot), fromDto(item)));
        }
        return equipment;
    }

    private static CellDto toDto(LayoutCell cell) {
        CellDto dto = new CellDto();
        dto.kind = cell.kind().name();
        dto.name = cell.name().orElse(null);
        if (cell.kind() == CellKind.EQUIPMENT) {
            dto.equipment = new LinkedHashMap<>();
            cell.equipment().forEach((slot, item) -> dto.equipment.put(slot.name(), toDto(item)));
        } else if (cell.kind() == CellKind.INVENTORY) {
            dto.items = toDto(cell.inventory());
            dto.dividers = dividersOf(cell.inventory());
        }
        return dto;
    }

    private static CustomContent customFrom(ContentDto dto) {
        if (dto.rows == null) {
            throw new IllegalArgumentException("Missing row count");
        }
        List<LayoutCell> cells = new ArrayList<>();
        if (dto.cells != null) {
            for (CellDto cell : dto.cells) {
                CellKind kind = CellKind.valueOf(require(cell.kind, "cell kind"));
                cells.add(new LayoutCell(kind, cell.name, equipmentFrom(cell.equipment), gridFrom(cell.items, cell.dividers)));
            }
        }
        return new CustomContent(dto.rows, cells);
    }

    private static Map<String, ItemDto> toDto(ItemGrid grid) {
        Map<String, ItemDto> slots = new LinkedHashMap<>();
        for (int index = 0; index < ItemGrid.SIZE; index++) {
            String key = String.valueOf(index);
            grid.slot(index).ifPresent(item -> slots.put(key, toDto(item)));
        }
        return slots;
    }

    private static List<DividerDto> dividersOf(ItemGrid grid) {
        if (grid.dividers().isEmpty()) {
            return null;
        }
        List<DividerDto> dividers = new ArrayList<>();
        for (Divider divider : grid.dividers()) {
            DividerDto dto = new DividerDto();
            dto.row = divider.row();
            dto.from = divider.spansWholeRow() ? null : divider.fromColumn();
            dto.to = divider.spansWholeRow() ? null : divider.toColumn();
            dto.label = divider.label();
            dto.align = divider.align() == TextAlign.DEFAULT ? null : divider.align().name();
            dto.line = divider.underlined() ? null : Boolean.FALSE;
            dividers.add(dto);
        }
        return dividers;
    }

    private static ItemGrid gridFrom(Map<String, ItemDto> dto, List<DividerDto> dividerDto) {
        Map<Integer, SetupItem> slots = new LinkedHashMap<>();
        if (dto != null) {
            dto.forEach((index, item) -> slots.put(Integer.parseInt(index), fromDto(item)));
        }
        List<Divider> dividers = new ArrayList<>();
        if (dividerDto != null) {
            for (DividerDto divider : dividerDto) {
                dividers.add(new Divider(divider.row, divider.from == null ? 0 : divider.from, divider.to == null ? ItemGrid.COLUMNS - 1 : divider.to,
                        require(divider.label, "divider label"), divider.align == null ? TextAlign.DEFAULT : TextAlign.valueOf(divider.align),
                        divider.line == null || divider.line));
            }
        }
        return ItemGrid.of(slots, dividers);
    }

    static ItemDto toDto(SetupItem item) {
        ItemDto dto = new ItemDto();
        dto.id = item.id().value();
        dto.qty = item.quantity().isPresent() ? item.quantity().getAsInt() : null;
        dto.match = item.match() == ItemMatch.DEFAULT ? null : item.match().name();
        dto.alts = item.alternatives().isEmpty() ? null : item.alternatives().stream().map(ItemId::value).collect(Collectors.toList());
        dto.noted = item.noted() ? Boolean.TRUE : null;
        return dto;
    }

    static SetupItem fromDto(ItemDto dto) {
        Objects.requireNonNull(dto, "item");
        ItemMatch match = dto.match == null ? ItemMatch.DEFAULT : ItemMatch.valueOf(dto.match);
        List<ItemId> alternatives = dto.alts == null ? List.of() : dto.alts.stream().map(ItemId::of).collect(Collectors.toList());
        SetupItem item = new SetupItem(ItemId.of(dto.id), SetupItem.BANK_AMOUNT, match, alternatives, Boolean.TRUE.equals(dto.noted));
        return dto.qty == null ? item : item.withQuantity(dto.qty);
    }

    static String require(String value, String what) {
        if (value == null) {
            throw new IllegalArgumentException("Missing " + what);
        }
        return value;
    }
}
