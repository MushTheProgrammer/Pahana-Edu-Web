package org.pahanaedu.service;

import org.pahanaedu.model.Item;

import java.math.BigDecimal;
import java.util.List;

public interface ItemService {
    boolean addItem(Item item);
    boolean updateItem(Item item);
    boolean deleteItem(String itemCode);
    Item getItemByCode(String itemCode);
    List<Item> getAllItems();

    List<Item> searchItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty, int page, int size, String sort, String dir);
    int countItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty);

    String getNextItemCode();
}
