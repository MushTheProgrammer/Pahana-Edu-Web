package org.pahanaedu.service.impl;

import org.pahanaedu.dao.ItemDAO;
import org.pahanaedu.dao.impl.ItemDAOImpl;
import org.pahanaedu.model.Item;
import org.pahanaedu.service.ItemService;

import java.math.BigDecimal;
import java.util.List;

public class ItemServiceImpl implements ItemService {
    private final ItemDAO dao = new ItemDAOImpl();

    @Override
    public boolean addItem(Item item) { return dao.addItem(item); }

    @Override
    public boolean updateItem(Item item) { return dao.updateItem(item); }

    @Override
    public boolean deleteItem(String itemCode) { return dao.deleteItem(itemCode); }

    @Override
    public Item getItemByCode(String itemCode) { return dao.getItemByCode(itemCode); }

    @Override
    public List<Item> getAllItems() { return dao.getAllItems(); }

    @Override
    public List<Item> searchItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty, int page, int size, String sort, String dir) {
        int limit = Math.max(1, size);
        int offset = Math.max(0, (page-1) * limit);
        String orderCol = sort == null ? "item_code" : sort;
        String orderDir = dir == null ? "asc" : dir;
        return dao.searchItems(code, name, minPrice, maxPrice, minQty, maxQty, offset, limit, orderCol, orderDir);
    }

    @Override
    public int countItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty) {
        return dao.countItems(code, name, minPrice, maxPrice, minQty, maxQty);
    }

    @Override
    public String getNextItemCode() { return dao.getNextItemCode(); }
}
