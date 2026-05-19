package com.company.api;

import com.company.model.*;
import com.company.repository.OrderRepository;
import com.company.service.OrderService;
import com.sun.net.httpserver.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class OrderController {

    private final OrderService service = new OrderService();

    public void getMenu(HttpExchange ex) throws IOException {
        Map<String, Object> dish = new LinkedHashMap<>();
        dish.put("id", 1);
        dish.put("name", "Нордское рагу");
        dish.put("price", 50);

        List<Map<String, Object>> mods = new ArrayList<>();
        int modId = 901;
        for (Map.Entry<String, Integer> e : OrderService.getAvailableModifications().entrySet()) {
            Map<String, Object> mod = new LinkedHashMap<>();
            mod.put("id", modId++);
            mod.put("name", e.getKey());
            mod.put("price", e.getValue());
            mods.add(mod);
        }
        dish.put("available_modifications", mods);

        sendJson(ex, 200, Collections.singletonList(dish));
    }

    public void getOrders(HttpExchange ex) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order o : OrderRepository.getInstance().findAll())
            list.add(orderToMap(o));
        sendJson(ex, 200, list);
    }

    public void createOrder(HttpExchange ex) throws IOException {
        Map<String, Object> body = parseBody(ex);
        int table = toInt(body.get("table_number"));
        String guest = (String) body.getOrDefault("guest_name", null);
        Order order = service.createOrder(table, guest);
        sendJson(ex, 201, orderToMap(order));
    }

    public void addDish(HttpExchange ex, int orderId) throws IOException {
        Map<String, Object> body = parseBody(ex);
        int qty = body.containsKey("quantity") ? toInt(body.get("quantity")) : 1;
        OrderItem item = service.addDish(orderId, qty);
        sendJson(ex, 201, itemToMap(item));
    }

    public void addModification(HttpExchange ex, int orderId, int itemId) throws IOException {
        Map<String, Object> body = parseBody(ex);
        String modType = (String) body.get("modification_type");
        OrderItem item = service.addModification(orderId, itemId, modType);
        sendJson(ex, 201, itemToMap(item));
    }

    public void updateItem(HttpExchange ex, int orderId, int itemId) throws IOException {
        Map<String, Object> body = parseBody(ex);
        Integer newQty = body.containsKey("quantity") ? toInt(body.get("quantity")) : null;
        @SuppressWarnings("unchecked")
        List<String> newMods = body.containsKey("modifications")
                ? (List<String>) body.get("modifications") : null;
        OrderItem item = service.updateItem(orderId, itemId, newQty, newMods);
        sendJson(ex, 200, itemToMap(item));
    }

    public void confirmOrder(HttpExchange ex, int orderId) throws IOException {
        Order order = service.confirmOrder(orderId);
        sendJson(ex, 200, orderToMap(order));
    }

    private Map<String, Object> orderToMap(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("table_number", o.getTableNumber());
        m.put("guest_name", o.getGuestName());
        m.put("status", o.getStatus().name().toLowerCase());
        m.put("total_price", o.getTotalPrice());
        m.put("created_at", o.getCreatedAt().toString());
        m.put("confirmed_at", o.getConfirmedAt() != null ? o.getConfirmedAt().toString() : null);
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem i : o.getItems()) items.add(itemToMap(i));
        m.put("items", items);
        return m;
    }

    private Map<String, Object> itemToMap(OrderItem i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("dish_name", i.getDishName());
        m.put("unit_price", i.getUnitPrice());
        m.put("quantity", i.getQuantity());
        m.put("modifications", i.getAppliedModNames());
        m.put("total_price", i.getTotalPrice());
        return m;
    }

    // Utils
    private void sendJson(HttpExchange ex, int status, Object data) throws IOException {
        String json = toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    @SuppressWarnings("unchecked")
    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            for (Map.Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
                if (sb.length() > 1) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":").append(toJson(e.getValue()));
            }
            return sb.append("}").toString();
        }
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            for (Object v : (List<?>) obj) {
                if (sb.length() > 1) sb.append(",");
                sb.append(toJson(v));
            }
            return sb.append("]").toString();
        }
        if (obj instanceof String) {
            return "\"" + ((String) obj).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return obj.toString();
    }

    private Map<String, Object> parseBody(HttpExchange ex) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);

        Map<String, Object> result = new LinkedHashMap<>();
        Matcher m = Pattern.compile(
                "\"(\\w+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?\\d+))").matcher(sb.toString());
        while (m.find())
            result.put(m.group(1), m.group(2) != null ? m.group(2) : Integer.parseInt(m.group(3)));
        return result;
    }

    private int toInt(Object v) {
        if (v instanceof Integer) return (Integer) v;
        return Integer.parseInt(v.toString());
    }
}
