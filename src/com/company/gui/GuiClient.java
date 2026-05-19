package com.company.gui;

import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.*;

public class GuiClient extends JFrame {

    private static final String BASE = "http://localhost:8080";

    private static final Color BG = new Color(0x2B1D0E);
    private static final Color PANEL_BG = new Color(0x3D2B1A);
    private static final Color ACCENT = new Color(0xC8973A);
    private static final Color ACCENT2 = new Color(0x8B4513);
    private static final Color TEXT = new Color(0xF5DEB3);
    private static final Color TEXT_DIM = new Color(0xA08060);
    private static final Color SUCCESS = new Color(0x4CAF50);
    private static final Color TABLE_ODD = new Color(0x4A3520);
    private static final Color TABLE_EVEN = new Color(0x3D2B1A);

    private final DefaultTableModel ordersModel;
    private final JTable ordersTable;
    private final DefaultTableModel itemsModel;
    private final JTable itemsTable;
    private final JTextArea logArea;

    private final JTextField tfTable = styledField("1");
    private final JTextField tfGuest = styledField("Довакин");
    private final JSpinner spQty = styledSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private static final String[][] MOD_ENTRIES = {
            {"Огненный соус (+40 сеп.)",          "fire_sauce"},
            {"Двойная порция оленины (+20 сеп.)", "double_deer"},
            {"Снежные ягоды (+6 сеп.)",           "snow_berries"},
            {"Нордская лепёшка (+7 сеп.)",        "nordic_bread"},
    };
    private final JComboBox<String> cbMod = styledCombo(
            Arrays.stream(MOD_ENTRIES).map(e -> e[0]).toArray(String[]::new));
    private final JTextField tfItemId = styledField("");

    private int selectedOrderId = -1;
    private JButton btnConfirm;
    private JButton btnAddDish;
    private JButton btnAddMod;
    private JLabel selectedLabel;

    public GuiClient() {
        super("⚔  Рагу в Гарцующей Кобыле  ⚔");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        ordersModel = new DefaultTableModel(
                new String[]{"ID", "Стол", "Гость", "Статус", "Сумма", "Создан"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        ordersTable = styledTable(ordersModel);
        ordersTable.getSelectionModel().addListSelectionListener(
                e -> {
                    if (!e.getValueIsAdjusting()) loadItemsForSelected();
                });

        itemsModel = new DefaultTableModel(
                new String[]{"ID", "Блюдо", "Цена/ед", "Кол-во", "Добавки", "Итого"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        itemsTable = styledTable(itemsModel);
        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = itemsTable.getSelectedRow();
                if (row >= 0) tfItemId.setText(itemsModel.getValueAt(row, 0).toString());
            }
        });

        logArea = new JTextArea(5, 0);
        logArea.setEditable(false);
        logArea.setBackground(new Color(0x1A0F05));
        logArea.setForeground(TEXT_DIM);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        setLayout(new BorderLayout(6, 6));
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildLogPanel(), BorderLayout.SOUTH);

        refreshOrders();
    }

    private JPanel buildLeftPanel() {
        JPanel p = darkPanel();
        p.setPreferredSize(new Dimension(250, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        p.add(sectionLabel("📋 Новый заказ"));
        p.add(labeledRow("Стол (1-20):", tfTable));
        p.add(labeledRow("Гость:", tfGuest));
        p.add(Box.createVerticalStrut(6));
        p.add(accentButton("Создать заказ", e -> createOrder()));

        p.add(Box.createVerticalStrut(16));
        p.add(sectionLabel("🍲 Нордское рагу (50 сеп.)"));
        p.add(labeledRow("Кол-во:", spQty));
        p.add(Box.createVerticalStrut(6));
        btnAddDish = accentButton("Добавить в заказ", e -> addDish());
        p.add(btnAddDish);

        p.add(Box.createVerticalStrut(16));
        p.add(sectionLabel("✨ Добавка к позиции"));
        JLabel hint = new JLabel("кликни на позицию в таблице");
        hint.setForeground(TEXT_DIM);
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 10f));
        hint.setAlignmentX(LEFT_ALIGNMENT);
        p.add(hint);
        p.add(labeledRow("ID позиции:", tfItemId));
        p.add(labeledRow("Добавка:", cbMod));
        p.add(Box.createVerticalStrut(6));
        btnAddMod = accentButton("Применить добавку", e -> addMod());
        p.add(btnAddMod);

        p.add(Box.createVerticalStrut(16));
        p.add(sectionLabel("✅ Подтверждение"));
        btnConfirm = accentButton("Подтвердить заказ", SUCCESS, e -> confirmOrder());
        p.add(btnConfirm);

        p.add(Box.createVerticalStrut(16));
        p.add(dimButton("🔄 Обновить список", e -> refreshOrders()));

        p.add(Box.createVerticalGlue());
        updateButtonStates(true);
        return p;
    }

    private JPanel buildCenterPanel() {
        JPanel p = darkPanel();
        p.setLayout(new GridLayout(2, 1, 0, 6));
        p.setBorder(new EmptyBorder(10, 6, 10, 10));

        JPanel top = darkPanel();
        top.setLayout(new BorderLayout());
        top.add(sectionLabel("📦 Заказы"), BorderLayout.NORTH);
        JScrollPane ts = new JScrollPane(ordersTable);
        ts.getViewport().setBackground(PANEL_BG);
        ts.setBorder(BorderFactory.createLineBorder(ACCENT2));
        top.add(ts, BorderLayout.CENTER);

        JPanel bot = darkPanel();
        bot.setLayout(new BorderLayout());
        selectedLabel = sectionLabel("🥤 Позиции (выберите заказ)");
        bot.add(selectedLabel, BorderLayout.NORTH);
        JScrollPane bs = new JScrollPane(itemsTable);
        bs.getViewport().setBackground(PANEL_BG);
        bs.setBorder(BorderFactory.createLineBorder(ACCENT2));
        bot.add(bs, BorderLayout.CENTER);

        p.add(top);
        p.add(bot);
        return p;
    }

    private JPanel buildLogPanel() {
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, ACCENT2),
                new EmptyBorder(4, 6, 4, 6)));
        p.setPreferredSize(new Dimension(0, 110));
        JLabel lbl = new JLabel("  Лог запросов", JLabel.LEFT);
        lbl.setForeground(ACCENT);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        p.add(lbl, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(0x1A0F05));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void updateButtonStates(boolean confirmed) {
        btnConfirm.setEnabled(!confirmed);
        btnAddDish.setEnabled(!confirmed);
        btnAddMod.setEnabled(!confirmed);
    }

    private void refreshOrders() {
        new Thread(() -> {
            String json = get("/orders");
            SwingUtilities.invokeLater(() -> populateOrders(json));
        }).start();
    }

    private void createOrder() {
        String table = tfTable.getText().trim();
        String guest = tfGuest.getText().trim();
        if (table.isEmpty()) {
            log("Введите номер стола");
            return;
        }
        String body = "{\"table_number\":" + table
                + (guest.isEmpty() ? "" : ",\"guest_name\":\"" + guest + "\"") + "}";
        new Thread(() -> {
            String res = post("/orders", body);
            SwingUtilities.invokeLater(() -> {
                log("POST /orders → " + res);
                refreshOrders();
            });
        }).start();
    }

    private void addDish() {
        if (selectedOrderId < 0) {
            log("Выберите заказ в таблице");
            return;
        }
        int qty = (int) spQty.getValue();
        int oid = selectedOrderId;
        String body = "{\"quantity\":" + qty + "}";
        new Thread(() -> {
            String res = post("/orders/" + oid + "/dishes", body);
            SwingUtilities.invokeLater(() -> {
                log("POST /orders/" + oid + "/dishes → " + res);
                refreshOrders();
            });
        }).start();
    }

    private void addMod() {
        if (selectedOrderId < 0) {
            log("Выберите заказ");
            return;
        }
        String itemIdStr = tfItemId.getText().trim();
        if (itemIdStr.isEmpty()) {
            log("Кликните на позицию в нижней таблице");
            return;
        }
        int idx = cbMod.getSelectedIndex();
        String mod = MOD_ENTRIES[idx][1];
        int oid = selectedOrderId;
        String body = "{\"modification_type\":\"" + mod + "\"}";
        new Thread(() -> {
            String res = post("/orders/" + oid + "/dishes/" + itemIdStr + "/modifications", body);
            SwingUtilities.invokeLater(() -> {
                log("POST /orders/" + oid + "/dishes/" + itemIdStr + "/modifications → " + res);
                refreshOrders();
            });
        }).start();
    }

    private void confirmOrder() {
        if (selectedOrderId < 0) {
            log("Выберите заказ");
            return;
        }
        int oid = selectedOrderId;
        updateButtonStates(true);
        new Thread(() -> {
            String res = post("/orders/" + oid + "/confirm", "{}");
            SwingUtilities.invokeLater(() -> {
                log("POST /orders/" + oid + "/confirm → " + res);
                refreshOrders();
            });
        }).start();
    }

    // Заполнение таблиц
    private void populateOrders(String json) {
        ordersModel.setRowCount(0);
        itemsModel.setRowCount(0);
        if (json == null || json.trim().isEmpty() || json.startsWith("{\"code\"")) {
            log("Ответ: " + json);
            return;
        }
        List<Map<String, String>> orders = parseJsonArray(json);
        for (Map<String, String> o : orders) {
            ordersModel.addRow(new Object[]{
                    o.getOrDefault("id", ""),
                    o.getOrDefault("table_number", ""),
                    o.getOrDefault("guest_name", ""),
                    o.getOrDefault("status", ""),
                    o.getOrDefault("total_price", "") + " сеп.",
                    o.getOrDefault("created_at", "").replace("T", " ")
            });
        }
        log("GET /orders → " + orders.size() + " заказ(ов)");

        if (selectedOrderId > 0) {
            for (int r = 0; r < ordersModel.getRowCount(); r++) {
                if (ordersModel.getValueAt(r, 0).toString().equals(String.valueOf(selectedOrderId))) {
                    ordersTable.setRowSelectionInterval(r, r);
                    updateButtonStates("confirmed".equals(ordersModel.getValueAt(r, 3).toString()));
                    break;
                }
            }
        }
    }

    private void loadItemsForSelected() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) return;
        selectedOrderId = Integer.parseInt(ordersModel.getValueAt(row, 0).toString());
        selectedLabel.setText("☕️ Позиции заказа #" + selectedOrderId);
        itemsModel.setRowCount(0);
        tfItemId.setText("");
        updateButtonStates("confirmed".equals(ordersModel.getValueAt(row, 3).toString()));

        int oid = selectedOrderId;
        new Thread(() -> {
            String json = get("/orders");
            SwingUtilities.invokeLater(() -> {
                String itemsJson = extractItems(json, oid);
                if (itemsJson == null) return;
                for (Map<String, String> i : parseJsonArray(itemsJson)) {
                    itemsModel.addRow(new Object[]{
                            i.getOrDefault("id", ""),
                            i.getOrDefault("dish_name", ""),
                            i.getOrDefault("unit_price", "") + " сеп.",
                            i.getOrDefault("quantity", ""),
                            i.getOrDefault("modifications", ""),
                            i.getOrDefault("total_price", "") + " сеп."
                    });
                }
            });
        }).start();
    }

    // HTTP
    private String get(String path) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(BASE + path).openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Accept", "application/json");
            return readResponse(c);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String post(String path, String body) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(BASE + path).openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            return readResponse(c);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String readResponse(HttpURLConnection c) throws IOException {
        InputStream is = c.getResponseCode() < 400 ? c.getInputStream() : c.getErrorStream();
        if (is == null) return "{}";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    // JSON-парсер
    private List<Map<String, String>> parseJsonArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return result;
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth++ == 0) start = i;
            } else if (c == '}') {
                if (--depth == 0 && start >= 0) {
                    result.add(parseJsonObject(json.substring(start, i + 1)));
                    start = -1;
                }
            }
        }
        return result;
    }

    private Map<String, String> parseJsonObject(String obj) {
        Map<String, String> m = new LinkedHashMap<>();
        Matcher scalar = Pattern.compile(
                "\"(\\w+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?\\d+(?:\\.\\d+)?)|null)").matcher(obj);
        while (scalar.find()) {
            m.put(scalar.group(1),
                    scalar.group(2) != null ? scalar.group(2)
                            : scalar.group(3) != null ? scalar.group(3) : "");
        }
        Matcher arrM = Pattern.compile("\"modifications\"\\s*:\\s*\\[([^\\]]*)]").matcher(obj);
        if (arrM.find()) {
            String inner = arrM.group(1).replaceAll("\"", "").trim();
            m.put("modifications", inner.isEmpty() ? "—" : inner);
        }
        return m;
    }

    private String extractItems(String json, int orderId) {
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*" + orderId + "\\s*,").matcher(json);
        if (!m.find()) return null;
        int start = json.lastIndexOf('{', m.start());
        int iStart = json.indexOf("\"items\"", start);
        if (iStart < 0) return "[]";
        int arrStart = json.indexOf('[', iStart);
        if (arrStart < 0) return "[]";
        int depth = 0, pos = arrStart;
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == '[') depth++;
            else if (c == ']' && --depth == 0) break;
            pos++;
        }
        return json.substring(arrStart, pos + 1);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // UI-утилиты
    private static JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        return p;
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(ACCENT);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setBorder(new EmptyBorder(6, 0, 4, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JTextField styledField(String def) {
        JTextField f = new JTextField(def);
        f.setBackground(new Color(0x1A0F05));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT2),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return f;
    }

    private static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(new Color(0x1A0F05));
        cb.setForeground(TEXT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return cb;
    }

    private static JSpinner styledSpinner(SpinnerModel model) {
        JSpinner sp = new JSpinner(model);
        sp.setBackground(new Color(0x1A0F05));
        sp.setForeground(TEXT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setBackground(new Color(0x1A0F05));
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setForeground(TEXT);
        return sp;
    }

    private static JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isCellSelected(row, col) ? ACCENT2 : (row % 2 == 0 ? TABLE_EVEN : TABLE_ODD));
                c.setForeground(TEXT);
                return c;
            }
        };
        t.setBackground(PANEL_BG);
        t.setForeground(TEXT);
        t.setSelectionBackground(ACCENT2);
        t.setSelectionForeground(TEXT);
        t.setGridColor(new Color(0x5A3F28));
        t.setRowHeight(22);
        t.getTableHeader().setBackground(new Color(0x5A3010));
        t.getTableHeader().setForeground(ACCENT);
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD));
        t.setFillsViewportHeight(true);
        return t;
    }

    private static JButton accentButton(String text, java.awt.event.ActionListener al) {
        return accentButton(text, ACCENT2, al);
    }

    private static JButton accentButton(String text, Color bg, java.awt.event.ActionListener al) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(TEXT);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    private static JButton dimButton(String text, java.awt.event.ActionListener al) {
        JButton b = accentButton(text, new Color(0x4A3520), al);
        b.setForeground(TEXT_DIM);
        return b;
    }

    private static JPanel labeledRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setBackground(PANEL_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel l = new JLabel(label);
        l.setForeground(TEXT_DIM);
        l.setPreferredSize(new Dimension(90, 24));
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setBorder(new EmptyBorder(2, 0, 2, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            UIManager.put("Button.background", new Color(0x8B4513));
            UIManager.put("Button.foreground", new Color(0xF5DEB3));
            UIManager.put("Panel.background", new Color(0x3D2B1A));
            UIManager.put("ScrollPane.background", new Color(0x3D2B1A));
            UIManager.put("ComboBox.background", new Color(0x1A0F05));
            UIManager.put("ComboBox.foreground", new Color(0xF5DEB3));
            UIManager.put("ComboBox.selectionBackground", new Color(0x8B4513));
            UIManager.put("ComboBox.selectionForeground", new Color(0xF5DEB3));
            UIManager.put("TextField.background", new Color(0x1A0F05));
            UIManager.put("TextField.foreground", new Color(0xF5DEB3));
            UIManager.put("TextArea.background", new Color(0x1A0F05));
            UIManager.put("TextArea.foreground", new Color(0xA08060));
            UIManager.put("Label.foreground", new Color(0xF5DEB3));
            new GuiClient().setVisible(true);
        });
    }
}
