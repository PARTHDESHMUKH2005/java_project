import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Application window. Wires the simulator to a price chart, an order-book
 * depth view and user controls, advancing the simulation on a Swing timer.
 */
public class MainFrame extends JFrame {

    static final Color BG = new Color(250, 251, 253);
    static final Color GRID = new Color(225, 229, 236);
    static final Color TEXT = new Color(30, 36, 50);
    static final Color MUTED = new Color(110, 120, 140);
    static final Color UP = new Color(18, 150, 100);
    static final Color DOWN = new Color(215, 55, 70);
    static final Color LINE = new Color(30, 110, 220);

    private final MarketSimulator simulator = new MarketSimulator(100.0);
    private final PriceChartPanel chart = new PriceChartPanel(simulator);
    private final OrderBookPanel bookPanel = new OrderBookPanel(simulator);
    private final Timer timer = new Timer(200, e -> {
        simulator.step();
        refresh();
    });

    private final JLabel bidLabel = new JLabel("-");
    private final JLabel askLabel = new JLabel("-");
    private final JLabel spreadLabel = new JLabel("-");
    private final JLabel midLabel = new JLabel("-");
    private final JLabel lastLabel = new JLabel("-");
    private final JTextArea tradeArea = new JTextArea(8, 20);

    private final JComboBox<Order.Side> sideBox = new JComboBox<>(Order.Side.values());
    private final JTextField priceField = new JTextField("100.00", 6);
    private final JTextField qtyField = new JTextField("10", 5);
    private final JButton pauseButton = new JButton("Pause");
    private final JSlider speedSlider = new JSlider(1, 20, 5);
    private final JLabel speedLabel = new JLabel("5 steps/s");
    private final JLabel messageLabel = new JLabel("Place your own order, or estimate how a market order would move the price.");

    public MainFrame() {
        super("OrderBookViz");
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(buildStatsRow(), BorderLayout.NORTH);
        add(chart, BorderLayout.CENTER);
        add(buildSidePanel(), BorderLayout.EAST);
        add(buildControls(), BorderLayout.SOUTH);

        timer.start();
        refresh();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setBackground(BG);
        row.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        addStat(row, "BEST BID", bidLabel, UP);
        addStat(row, "BEST ASK", askLabel, DOWN);
        addStat(row, "SPREAD", spreadLabel, TEXT);
        addStat(row, "MID PRICE", midLabel, TEXT);
        addStat(row, "LAST TRADE", lastLabel, LINE);
        return row;
    }

    private void addStat(JPanel row, String title, JLabel value, Color color) {
        JLabel name = new JLabel(title);
        name.setForeground(MUTED);
        value.setForeground(color);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 20f));
        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setBackground(BG);
        box.add(name);
        box.add(value);
        row.add(box);
    }

    private JPanel buildSidePanel() {
        JLabel title = new JLabel("RECENT TRADES");
        title.setForeground(MUTED);
        tradeArea.setEditable(false);
        tradeArea.setFocusable(false);
        tradeArea.setBackground(BG);
        tradeArea.setForeground(TEXT);
        tradeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel trades = new JPanel(new BorderLayout(0, 4));
        trades.setBackground(BG);
        trades.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        trades.add(title, BorderLayout.NORTH);
        trades.add(tradeArea, BorderLayout.CENTER);

        JPanel side = new JPanel(new BorderLayout());
        side.add(bookPanel, BorderLayout.CENTER);
        side.add(trades, BorderLayout.SOUTH);
        return side;
    }

    private JPanel buildControls() {
        for (JTextField field : new JTextField[] {priceField, qtyField}) {
            field.setBackground(GRID);
            field.setForeground(TEXT);
            field.setCaretColor(TEXT);
            field.addActionListener(e -> placeOrder());
        }
        JButton placeButton = new JButton("Place Order");
        placeButton.addActionListener(e -> placeOrder());
        JButton estimateButton = new JButton("Estimate Impact");
        estimateButton.addActionListener(e -> estimateImpact());
        pauseButton.addActionListener(e -> togglePause());
        speedSlider.addChangeListener(e -> changeSpeed());
        speedSlider.setBackground(BG);
        speedLabel.setForeground(TEXT);
        messageLabel.setForeground(MUTED);

        JPanel controls = new JPanel(new GridLayout(3, 1));
        controls.setBackground(BG);
        controls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, GRID),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        controls.add(row(label("Side"), sideBox, label("Price"), priceField,
                label("Qty"), qtyField, placeButton, estimateButton));
        controls.add(row(pauseButton, label("Speed"), speedSlider, speedLabel));
        controls.add(row(messageLabel));
        return controls;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    private JPanel row(Component... items) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setBackground(BG);
        for (Component item : items) {
            row.add(item);
        }
        return row;
    }

    private void placeOrder() {
        try {
            Order.Side side = (Order.Side) sideBox.getSelectedItem();
            double price = readPrice();
            int quantity = readQuantity();
            Order order = new Order(side, price, quantity, "YOU");
            simulator.getBook().submit(order);
            int resting = order.getQuantity();
            showMessage(String.format("%s %d @ %.2f: %d filled, %d resting",
                    side, quantity, price, quantity - resting, resting), false);
            refresh();
        } catch (NumberFormatException e) {
            showMessage("Price and quantity must be numbers", true);
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage(), true);
        }
    }

    private void estimateImpact() {
        try {
            Order.Side side = (Order.Side) sideBox.getSelectedItem();
            int quantity = readQuantity();
            ImpactResult result = simulator.getBook().estimateImpact(side, quantity);
            showMessage(side + " " + quantity + " at market: " + result, false);
        } catch (NumberFormatException e) {
            showMessage("Quantity must be a number", true);
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage(), true);
        }
    }

    private double readPrice() {
        double price = Double.parseDouble(priceField.getText().trim());
        double mid = simulator.getBook().midPrice();
        if (Math.abs(price - mid) > mid * 0.2) {
            throw new IllegalArgumentException(
                    String.format("Price must be within 20%% of the mid price (%.2f)", mid));
        }
        return price;
    }

    private int readQuantity() {
        int quantity = Integer.parseInt(qtyField.getText().trim());
        if (quantity <= 0 || quantity > 1000) {
            throw new IllegalArgumentException("Quantity must be between 1 and 1000");
        }
        return quantity;
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setForeground(isError ? DOWN : UP);
        messageLabel.setText(text);
    }

    private void togglePause() {
        if (timer.isRunning()) {
            timer.stop();
            pauseButton.setText("Resume");
        } else {
            timer.start();
            pauseButton.setText("Pause");
        }
    }

    private void changeSpeed() {
        int stepsPerSecond = speedSlider.getValue();
        timer.setDelay(1000 / stepsPerSecond);
        speedLabel.setText(stepsPerSecond + " steps/s");
    }

    private void refresh() {
        OrderBook book = simulator.getBook();
        Double bid = book.bestBid();
        Double ask = book.bestAsk();
        bidLabel.setText(bid == null ? "-" : String.format("%.2f", bid));
        askLabel.setText(ask == null ? "-" : String.format("%.2f", ask));
        spreadLabel.setText(bid == null || ask == null ? "-" : String.format("%.2f", ask - bid));
        midLabel.setText(String.format("%.2f", book.midPrice()));
        lastLabel.setText(String.format("%.2f", book.getLastPrice()));
        tradeArea.setText(String.join("\n", book.getTrades()));
        chart.repaint();
        bookPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

/**
 * Renders the recent mid-price history as a simple line chart.
 */
class PriceChartPanel extends JPanel {

    private static final int POINTS = 200;

    private final MarketSimulator simulator;

    PriceChartPanel(MarketSimulator simulator) {
        this.simulator = simulator;
        setPreferredSize(new Dimension(600, 400));
        setBackground(MainFrame.BG);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Double> all = simulator.getPriceHistory();
        if (all.size() < 2) return;
        List<Double> history = all.subList(Math.max(0, all.size() - POINTS), all.size());

        double min = history.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = history.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double pad = Math.max((max - min) * 0.1, 0.05);
        min -= pad;
        max += pad;
        double range = max - min;

        int left = 56, top = 32;
        int w = getWidth() - left - 16;
        int h = getHeight() - top - 16;

        g2.setColor(MainFrame.MUTED);
        g2.drawString("MID PRICE (LAST " + POINTS + " STEPS)", left, 18);
        for (int i = 0; i <= 4; i++) {
            int y = top + h * i / 4;
            g2.setColor(MainFrame.GRID);
            g2.drawLine(left, y, left + w, y);
            g2.setColor(MainFrame.MUTED);
            g2.drawString(String.format("%.2f", max - range * i / 4), 8, y + 4);
        }

        g2.setColor(MainFrame.LINE);
        g2.setStroke(new BasicStroke(2f));
        int n = history.size();
        int prevX = left;
        int prevY = top + (int) ((max - history.get(0)) / range * h);
        for (int i = 1; i < n; i++) {
            int x = left + (int) ((double) i / (n - 1) * w);
            int y = top + (int) ((max - history.get(i)) / range * h);
            g2.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }
        g2.fillOval(prevX - 4, prevY - 4, 8, 8);
    }
}

/**
 * Draws horizontal depth bars for the top bid and ask levels.
 */
class OrderBookPanel extends JPanel {

    private static final int LEVELS = 10;
    private static final int ROW_HEIGHT = 16;

    private final MarketSimulator simulator;

    OrderBookPanel(MarketSimulator simulator) {
        this.simulator = simulator;
        setPreferredSize(new Dimension(240, 370));
        setBackground(MainFrame.BG);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, MainFrame.GRID));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        OrderBook book = simulator.getBook();

        List<Map.Entry<Double, Integer>> asks = topLevels(book.getAsks());
        List<Map.Entry<Double, Integer>> bids = topLevels(book.getBids());
        Collections.reverse(asks);

        int maxQty = 1;
        for (Map.Entry<Double, Integer> level : asks) maxQty = Math.max(maxQty, level.getValue());
        for (Map.Entry<Double, Integer> level : bids) maxQty = Math.max(maxQty, level.getValue());

        int y = 24 + (LEVELS - asks.size()) * ROW_HEIGHT;
        y = drawLevels(g2, asks, y, maxQty, MainFrame.DOWN);
        y += 10;
        g2.setColor(MainFrame.GRID);
        g2.drawLine(8, y - 18, getWidth() - 8, y - 18);
        drawLevels(g2, bids, y, maxQty, MainFrame.UP);
    }

    private List<Map.Entry<Double, Integer>> topLevels(Map<Double, Integer> side) {
        List<Map.Entry<Double, Integer>> levels = new ArrayList<>();
        for (Map.Entry<Double, Integer> level : side.entrySet()) {
            if (levels.size() == LEVELS) break;
            levels.add(level);
        }
        return levels;
    }

    private int drawLevels(Graphics2D g2, List<Map.Entry<Double, Integer>> levels, int y, int maxQty, Color color) {
        int barMax = getWidth() - 110;
        for (Map.Entry<Double, Integer> level : levels) {
            int barW = (int) ((double) level.getValue() / maxQty * barMax);
            g2.setColor(color);
            g2.fillRect(64, y - 12, barW, ROW_HEIGHT - 2);
            g2.drawString(String.format("%.2f", level.getKey()), 8, y);
            g2.setColor(MainFrame.TEXT);
            g2.drawString(String.valueOf(level.getValue()), 64 + barW + 4, y);
            y += ROW_HEIGHT;
        }
        return y;
    }
}
