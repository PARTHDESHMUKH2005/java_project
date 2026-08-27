import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Application window. Wires the simulator to a price chart and an order-book
 * depth view, advancing the simulation on a Swing timer.
 */
public class MainFrame extends JFrame {

    private final MarketSimulator simulator = new MarketSimulator(100.0);

    public MainFrame() {
        super("OrderBookViz");
        PriceChartPanel chart = new PriceChartPanel(simulator);
        OrderBookPanel bookPanel = new OrderBookPanel(simulator);

        setLayout(new BorderLayout());
        add(chart, BorderLayout.CENTER);
        add(bookPanel, BorderLayout.EAST);

        Timer timer = new Timer(200, e -> {
            simulator.step();
            chart.repaint();
            bookPanel.repaint();
        });
        timer.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

/**
 * Renders the recorded mid-price history as a simple line chart.
 */
class PriceChartPanel extends JPanel {

    private final MarketSimulator simulator;

    PriceChartPanel(MarketSimulator simulator) {
        this.simulator = simulator;
        setPreferredSize(new Dimension(600, 400));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Double> history = simulator.getPriceHistory();
        if (history.size() < 2) return;

        double min = history.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = history.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double range = Math.max(max - min, 1e-6);

        int w = getWidth();
        int h = getHeight();
        g2.setColor(new Color(30, 120, 200));

        int n = history.size();
        int prevX = 0, prevY = toY(history.get(0), min, range, h);
        for (int i = 1; i < n; i++) {
            int x = (int) ((double) i / (n - 1) * (w - 1));
            int y = toY(history.get(i), min, range, h);
            g2.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }

        g2.setColor(Color.DARK_GRAY);
        g2.drawString(String.format("mid=%.2f", history.get(n - 1)), 8, 16);
    }

    private int toY(double price, double min, double range, int h) {
        return (int) (h - (price - min) / range * (h - 20) - 10);
    }
}

/**
 * Draws horizontal depth bars for the top bid and ask levels.
 */
class OrderBookPanel extends JPanel {

    private final MarketSimulator simulator;

    OrderBookPanel(MarketSimulator simulator) {
        this.simulator = simulator;
        setPreferredSize(new Dimension(220, 400));
        setBackground(new Color(245, 245, 245));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        OrderBook book = simulator.getBook();

        int maxQty = 1;
        for (int q : book.getAsks().values()) maxQty = Math.max(maxQty, q);
        for (int q : book.getBids().values()) maxQty = Math.max(maxQty, q);

        int rowH = 16;
        int y = 20;
        int barMax = getWidth() - 90;

        g2.setColor(Color.RED);
        int shown = 0;
        for (Map.Entry<Double, Integer> level : book.getAsks().entrySet()) {
            if (shown++ >= 10) break;
            int barW = (int) ((double) level.getValue() / maxQty * barMax);
            g2.fillRect(80, y - 12, barW, rowH - 2);
            g2.drawString(String.format("%.2f", level.getKey()), 4, y);
            y += rowH;
        }

        y += 10;
        g2.setColor(Color.GREEN.darker());
        shown = 0;
        for (Map.Entry<Double, Integer> level : book.getBids().entrySet()) {
            if (shown++ >= 10) break;
            int barW = (int) ((double) level.getValue() / maxQty * barMax);
            g2.fillRect(80, y - 12, barW, rowH - 2);
            g2.drawString(String.format("%.2f", level.getKey()), 4, y);
            y += rowH;
        }
    }
}
