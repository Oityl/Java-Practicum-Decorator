package com.company.api;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.regex.*;

public class RestServer {

    public static void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        OrderController ctrl = new OrderController();

        HttpHandler handler = new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod().toUpperCase();

                System.out.println("[" + method + "] " + path);

                if (method.equals("OPTIONS")) {
                    setCorsHeaders(exchange);
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                try {
                    if (path.equals("/") && method.equals("GET")) {
                        sendRaw(exchange, 200,
                                "{\"name\":\"Рагу в Гарцующей Кобыле\",\"version\":\"1.0\","
                                        + "\"endpoints\":[\"GET /orders\",\"POST /orders\","
                                        + "\"POST /orders/{id}/dishes\","
                                        + "\"POST /orders/{id}/dishes/{itemId}/modifications\","
                                        + "\"PATCH /orders/{id}/dishes/{itemId}\","
                                        + "\"POST /orders/{id}/confirm\"]}");
                        return;
                    }

                    if (path.equals("/orders") && method.equals("GET")) {
                        ctrl.getOrders(exchange);
                        return;
                    }

                    if (path.equals("/orders") && method.equals("POST")) {
                        ctrl.createOrder(exchange);
                        return;
                    }

                    Matcher cm = Pattern.compile("^/orders/(\\d+)/confirm$").matcher(path);
                    if (cm.matches() && method.equals("POST")) {
                        ctrl.confirmOrder(exchange, Integer.parseInt(cm.group(1)));
                        return;
                    }

                    Matcher mm = Pattern.compile("^/orders/(\\d+)/dishes/(\\d+)/modifications$").matcher(path);
                    if (mm.matches() && method.equals("POST")) {
                        ctrl.addModification(exchange,
                                Integer.parseInt(mm.group(1)),
                                Integer.parseInt(mm.group(2)));
                        return;
                    }

                    Matcher pm = Pattern.compile("^/orders/(\\d+)/dishes/(\\d+)$").matcher(path);
                    if (pm.matches() && method.equals("PATCH")) {
                        ctrl.updateItem(exchange,
                                Integer.parseInt(pm.group(1)),
                                Integer.parseInt(pm.group(2)));
                        return;
                    }

                    Matcher dm = Pattern.compile("^/orders/(\\d+)/dishes$").matcher(path);
                    if (dm.matches() && method.equals("POST")) {
                        ctrl.addDish(exchange, Integer.parseInt(dm.group(1)));
                        return;
                    }

                    sendRaw(exchange, 404, "{\"code\":\"NOT_FOUND\",\"message\":\"Маршрут не найден: " + path + "\"}");

                } catch (Exception e) {
                    System.err.println("Ошибка: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        sendRaw(exchange, 400, "{\"code\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}");
                    } catch (Exception ignored) {
                    }
                }
            }

            private void setCorsHeaders(HttpExchange ex) {
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
                ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            }

            private void sendRaw(HttpExchange ex, int status, String json) throws IOException {
                byte[] b = json.getBytes("UTF-8");
                ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                ex.sendResponseHeaders(status, b.length);
                ex.getResponseBody().write(b);
                ex.getResponseBody().close();
            }
        };

        server.createContext("/orders", handler);
        server.createContext("/", handler);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("REST API запущен: http://localhost:" + port);
        System.out.println("Проверка: http://localhost:" + port + "/orders");
    }
}