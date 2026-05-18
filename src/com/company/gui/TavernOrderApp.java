package com.company.gui;

import com.company.decorator.*;
import com.company.model.Dish;
import com.company.model.NordicStew;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class TavernOrderApp extends JFrame {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_ADDONS = 3;

    private final Map<JCheckBox, Function<Dish, Dish>> addonMap = new LinkedHashMap<>();

    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);

    public TavernOrderApp() {
        super("Гарцующая Кобыла — Вайтран");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));

        JLabel dishLabel = new JLabel("Нордское рагу — 50 септимов", SwingConstants.CENTER);
        dishLabel.setFont(dishLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(dishLabel, BorderLayout.NORTH);

        JPanel checkPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        checkPanel.setBorder(new TitledBorder("Добавки (не более 3)"));

        addAddon(checkPanel, "Огненный соус (+40 септимов)", WithFireSauce::new);
        addAddon(checkPanel, "Двойная порция оленины (+20 септимов)", WithDoubleDeer::new);
        addAddon(checkPanel, "Снежные ягоды (+6 септимов)", WithSnowBerries::new);
        addAddon(checkPanel, "Нордская лепёшка (+7 септимов)", WithNordicBread::new);

        topPanel.add(checkPanel, BorderLayout.CENTER);

        JButton orderBtn = new JButton("Оформить заказ");
        orderBtn.setFont(orderBtn.getFont().deriveFont(Font.BOLD, 13f));
        orderBtn.addActionListener(e -> placeOrder());
        topPanel.add(orderBtn, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        historyList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setBorder(new TitledBorder("Свиток заказов"));
        scroll.setPreferredSize(new Dimension(0, 200));

        root.add(scroll, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void addAddon(JPanel panel, String label, Function<Dish, Dish> factory) {
        JCheckBox cb = new JCheckBox(label);
        cb.addItemListener(e -> updateCheckboxStates());
        addonMap.put(cb, factory);
        panel.add(cb);
    }

    private void updateCheckboxStates() {
        long selected = addonMap.keySet().stream().filter(JCheckBox::isSelected).count();
        addonMap.keySet().forEach(cb -> {
            if (!cb.isSelected()) {
                cb.setEnabled(selected < MAX_ADDONS);
            }
        });
    }

    private void placeOrder() {
        Dish dish = new NordicStew();

        for (Map.Entry<JCheckBox, Function<Dish, Dish>> entry : addonMap.entrySet()) {
            if (entry.getKey().isSelected()) {
                dish = entry.getValue().apply(dish);
            }
        }

        String time = LocalTime.now().format(FMT);
        String record = String.format("%s  %s — %d септимов", time, dish.getName(), dish.getPrice());
        historyModel.addElement(record);
        historyList.ensureIndexIsVisible(historyModel.size() - 1);

        addonMap.keySet().forEach(cb -> {
            cb.setSelected(false);
            cb.setEnabled(true);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new TavernOrderApp().setVisible(true));
    }
}
