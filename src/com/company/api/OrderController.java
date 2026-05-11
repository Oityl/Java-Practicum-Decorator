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

    public void getOrders(HttpExchange ex) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order o : OrderRepository.getInstance().findAll()) {
            list.add(orderToMap(o));
        }
        sendJson(ex, 200, list);
    }

    public void createOrder(HttpExchange ex) throws IOException {
        requireMethod(ex, "POST");
        Map<String, Object> body = parseBody(ex);
        int table = toInt(body.get("table_number"));
        String guest = (String) body.getOrDefault("guest_name", null);

        Order order = service.createOrder(table, guest);
        sendJson(ex, 201, orderToMap(order));
    }

    public void addDish(HttpExchange ex, int orderId) throws IOException {
        requireMethod(ex, "POST");
        Map<String, Object> body = parseBody(ex);
        Drink drink = resolveDrink((String) body.get("dish_name"));
        int qty = body.containsKey("quantity") ? toInt(body.get("quantity")) : 1;

        OrderItem item = service.addDish(orderId, drink, qty);
        sendJson(ex, 201, itemToMap(item));
    }

    public void addModification(HttpExchange ex, int orderId, int itemId) throws IOException {
        requireMethod(ex, "POST");
        Map<String, Object> body = parseBody(ex);
        String modType = (String) body.get("modification_type");

        OrderItem item = service.addModification(orderId, itemId, modType);
        sendJson(ex, 201, itemToMap(item));
    }

    public void updateItem(HttpExchange ex, int orderId, int itemId) throws IOException {
        requireMethod(ex, "PATCH");
        Map<String, Object> body = parseBody(ex);

        Drink newDrink = body.containsKey("dish_name")
                ? resolveDrink((String) body.get("dish_name")) : null;
        Integer newQty = body.containsKey("quantity")
                ? toInt(body.get("quantity")) : null;
        @SuppressWarnings("unchecked")
        List<String> newMods = body.containsKey("modifications")
                ? (List<String>) body.get("modifications") : null;

        OrderItem item = service.updateItem(orderId, itemId, newDrink, newQty, newMods);
        sendJson(ex, 200, itemToMap(item));
    }

    public void confirmOrder(HttpExchange ex, int orderId) throws IOException {
        requireMethod(ex, "POST");
        Order order = service.confirmOrder(orderId);
        sendJson(ex, 200, orderToMap(order));
    }

    // Маппинг моделей
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
    private Drink resolveDrink(String name) {
        if (name == null) throw new IllegalArgumentException("dish_name обязателен");
        switch (name.toLowerCase()) {
            case "кофе":
            case "coffee":
                return new com.company.model.Coffee();
            case "чай":
            case "tea":
                return new com.company.model.Tea();
            case "медовуха":
            case "mead":
                return new com.company.model.Mead();
            default:
                throw new IllegalArgumentException("Блюдо не найдено: " + name);
        }
    }

    private void requireMethod(HttpExchange ex, String method) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase(method)) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("code", "METHOD_NOT_ALLOWED");
            err.put("message", "Ожидается " + method);
            sendJson(ex, 405, err);
            throw new IOException("Wrong method");
        }
    }

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
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (sb.length() > 1) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":").append(toJson(entry.getValue()));
            }
            return sb.append("}").toString();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (Object v : list) {
                if (sb.length() > 1) sb.append(",");
                sb.append(toJson(v));
            }
            return sb.append("]").toString();
        }
        if (obj instanceof String) {
            String s = (String) obj;
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return obj.toString();
    }

    private Map<String, Object> parseBody(HttpExchange ex) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        String raw = sb.toString();

        Map<String, Object> result = new LinkedHashMap<>();
        Matcher m = Pattern.compile(
                "\"(\\w+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?\\d+))").matcher(raw);
        while (m.find()) {
            result.put(m.group(1),
                    m.group(2) != null ? m.group(2) : Integer.parseInt(m.group(3)));
        }
        return result;
    }

    private int toInt(Object v) {
        if (v instanceof Integer) return (Integer) v;
        return Integer.parseInt(v.toString());
    }
}