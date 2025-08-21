package org.pahanaedu.dao.impl;

import org.pahanaedu.dao.ItemDAO;
import org.pahanaedu.model.Item;
import org.pahanaedu.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl implements ItemDAO {

    @Override
    public String getNextItemCode() {
        String last = null;
        String sql = "SELECT item_code FROM items ORDER BY item_code DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) last = rs.getString(1);
        } catch (Exception e) { e.printStackTrace(); }
        if (last == null || last.isEmpty()) return "ITM0001";
        try {
            String numStr = last.replaceAll("[^0-9]", "");
            int num = Integer.parseInt(numStr);
            return String.format("ITM%04d", num + 1);
        } catch (Exception e) {
            return "ITM0001";
        }
    }

    @Override
    public List<Item> searchItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty, int offset, int limit, String orderByCol, String orderDir) {
        List<Item> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM items WHERE 1=1");
        List<Object> params = new ArrayList<>();
        // Handle search logic - if code and name are the same, use OR logic for better search
        if (code != null && !code.isEmpty() && name != null && !name.isEmpty() && code.equals(name)) {
            // Same search term for both fields - use OR logic
            sql.append(" AND (LOWER(item_code) LIKE ? OR LOWER(name) LIKE ?)");
            params.add("%" + code.toLowerCase() + "%");
            params.add("%" + name.toLowerCase() + "%");
        } else {
            // Different search terms - use AND logic
            if (code != null && !code.isEmpty()) {
                sql.append(" AND LOWER(item_code) LIKE ?");
                params.add("%" + code.toLowerCase() + "%");
            }
            if (name != null && !name.isEmpty()) {
                sql.append(" AND LOWER(name) LIKE ?");
                params.add("%" + name.toLowerCase() + "%");
            }
        }
        if (minPrice != null) { sql.append(" AND unit_price >= ?"); params.add(minPrice); }
        if (maxPrice != null) { sql.append(" AND unit_price <= ?"); params.add(maxPrice); }
        if (minQty != null) { sql.append(" AND qty_on_hand >= ?"); params.add(minQty); }
        if (maxQty != null) { sql.append(" AND qty_on_hand <= ?"); params.add(maxQty); }
        // Sorting - whitelist columns
        String col = "item_code";
        if ("name".equalsIgnoreCase(orderByCol)) col = "name";
        else if ("unit_price".equalsIgnoreCase(orderByCol)) col = "unit_price";
        else if ("qty_on_hand".equalsIgnoreCase(orderByCol)) col = "qty_on_hand";
        String dir = "ASC";
        if ("desc".equalsIgnoreCase(orderDir)) dir = "DESC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir);
        // Pagination
        if (limit > 0) sql.append(" LIMIT ? OFFSET ?");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) ps.setObject(idx++, p);
            if (limit > 0) { ps.setInt(idx++, limit); ps.setInt(idx, offset); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Item i = new Item();
                i.setItemCode(rs.getString("item_code"));
                i.setName(rs.getString("name"));
                i.setUnitPrice(rs.getBigDecimal("unit_price"));
                i.setQtyOnHand(rs.getInt("qty_on_hand"));
                list.add(i);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public int countItems(String code, String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minQty, Integer maxQty) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM items WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (code != null && !code.isEmpty()) { sql.append(" AND LOWER(item_code) LIKE ?"); params.add("%"+code.toLowerCase()+"%"); }
        if (name != null && !name.isEmpty()) { sql.append(" AND LOWER(name) LIKE ?"); params.add("%"+name.toLowerCase()+"%"); }
        if (minPrice != null) { sql.append(" AND unit_price >= ?"); params.add(minPrice); }
        if (maxPrice != null) { sql.append(" AND unit_price <= ?"); params.add(maxPrice); }
        if (minQty != null) { sql.append(" AND qty_on_hand >= ?"); params.add(minQty); }
        if (maxQty != null) { sql.append(" AND qty_on_hand <= ?"); params.add(maxQty); }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i=0; i<params.size(); i++) ps.setObject(i+1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (item_code, name, unit_price, qty_on_hand) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            System.out.println("Adding item: " + item.getItemCode() + ", " + item.getName() + ", " + item.getUnitPrice() + ", " + item.getQtyOnHand());
            ps.setString(1, item.getItemCode());
            ps.setString(2, item.getName());
            ps.setBigDecimal(3, item.getUnitPrice());
            ps.setInt(4, item.getQtyOnHand());
            int result = ps.executeUpdate();
            System.out.println("Insert result: " + result);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error adding item: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET name=?, unit_price=?, qty_on_hand=? WHERE item_code=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setBigDecimal(2, item.getUnitPrice());
            ps.setInt(3, item.getQtyOnHand());
            ps.setString(4, item.getItemCode());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean deleteItem(String itemCode) {
        String sql = "DELETE FROM items WHERE item_code=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public Item getItemByCode(String itemCode) {
        String sql = "SELECT * FROM items WHERE item_code=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Item i = new Item();
                i.setItemCode(rs.getString("item_code"));
                i.setName(rs.getString("name"));
                i.setUnitPrice(rs.getBigDecimal("unit_price"));
                i.setQtyOnHand(rs.getInt("qty_on_hand"));
                return i;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Item i = new Item();
                i.setItemCode(rs.getString("item_code"));
                i.setName(rs.getString("name"));
                i.setUnitPrice(rs.getBigDecimal("unit_price"));
                i.setQtyOnHand(rs.getInt("qty_on_hand"));
                list.add(i);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
