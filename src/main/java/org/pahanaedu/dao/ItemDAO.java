package org.pahanaedu.dao;

import org.pahanaedu.model.Item;
import java.math.BigDecimal;
import java.util.List;

public interface ItemDAO {
    boolean addItem(Item item);
    boolean updateItem(Item item);
    boolean deleteItem(String itemCode);
    Item getItemByCode(String itemCode);
    List<Item> getAllItems();
    // Paged and sorted search with ranges
    List<Item> searchItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty, int offset, int limit, String orderByCol, String orderDir);
    int countItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty);
    String getNextItemCode();
}
