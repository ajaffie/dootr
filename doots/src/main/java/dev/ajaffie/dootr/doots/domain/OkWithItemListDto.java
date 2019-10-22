package dev.ajaffie.dootr.doots.domain;

import io.vertx.axle.sqlclient.Row;

import java.util.LinkedList;
import java.util.List;

public class OkWithItemListDto extends OkDto {
    public List<ItemDto> items;

    public OkWithItemListDto(List<ItemDto> items) {
        this.items = items;
    }

    public static OkWithItemListDto fromDoots(Iterable<Doot> doots) {
        List<ItemDto> items = new LinkedList<>();
        doots.forEach(doot -> items.add(ItemDto.from(doot)));
        return new OkWithItemListDto(items);
    }

    public static OkWithItemListDto fromRows(Iterable<Row> rows) {
        List<ItemDto> items = new LinkedList<>();
        rows.forEach(row -> items.add(ItemDto.from(row)));
        items.sort((a, b) -> (int)(a.timestamp - b.timestamp));
        return new OkWithItemListDto(items);
    }
}
