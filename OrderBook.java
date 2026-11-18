import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A price/quantity limit order book. Incoming orders are matched against the
 * opposite side; any remainder rests. Bids are kept high-to-low, asks low-to-high.
 */
public class OrderBook {

    private final TreeMap<Double, Integer> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Double, Integer> asks = new TreeMap<>();
    private final List<String> trades = new ArrayList<>();
    private double lastPrice;

    public OrderBook(double initialPrice) {
        this.lastPrice = initialPrice;
    }

    /** Submit an order, matching what it can and resting the remainder. */
    public void submit(Order order) {
        if (order.getSide() == Order.Side.BUY) {
            matchBuy(order);
            if (order.getQuantity() > 0) {
                rest(bids, order.getPrice(), order.getQuantity());
            }
        } else {
            matchSell(order);
            if (order.getQuantity() > 0) {
                rest(asks, order.getPrice(), order.getQuantity());
            }
        }
    }

    private void matchBuy(Order order) {
        while (order.getQuantity() > 0 && !asks.isEmpty()) {
            Map.Entry<Double, Integer> best = asks.firstEntry();
            if (order.getPrice() < best.getKey()) break;
            int traded = Math.min(order.getQuantity(), best.getValue());
            order.setQuantity(order.getQuantity() - traded);
            reduce(asks, best.getKey(), traded);
            lastPrice = best.getKey();
            recordTrade(order, traded, best.getKey());
        }
    }

    private void matchSell(Order order) {
        while (order.getQuantity() > 0 && !bids.isEmpty()) {
            Map.Entry<Double, Integer> best = bids.firstEntry();
            if (order.getPrice() > best.getKey()) break;
            int traded = Math.min(order.getQuantity(), best.getValue());
            order.setQuantity(order.getQuantity() - traded);
            reduce(bids, best.getKey(), traded);
            lastPrice = best.getKey();
            recordTrade(order, traded, best.getKey());
        }
    }

    private void rest(TreeMap<Double, Integer> side, double price, int qty) {
        side.merge(price, qty, Integer::sum);
    }

    private void reduce(TreeMap<Double, Integer> side, double price, int qty) {
        int remaining = side.getOrDefault(price, 0) - qty;
        if (remaining <= 0) {
            side.remove(price);
        } else {
            side.put(price, remaining);
        }
    }

    private void recordTrade(Order order, int quantity, double price) {
        trades.add(0, String.format("%s %s %d @ %.2f", order.getTraderId(), order.getSide(), quantity, price));
        if (trades.size() > 8) {
            trades.remove(8);
        }
    }

    public Double bestBid() { return bids.isEmpty() ? null : bids.firstKey(); }
    public Double bestAsk() { return asks.isEmpty() ? null : asks.firstKey(); }
    public double getLastPrice() { return lastPrice; }

    public double midPrice() {
        Double bid = bestBid();
        Double ask = bestAsk();
        if (bid != null && ask != null) return (bid + ask) / 2.0;
        if (bid != null) return bid;
        if (ask != null) return ask;
        return lastPrice;
    }

    /** Estimate the impact of a marketable order without mutating the book. */
    public ImpactResult estimateImpact(Order.Side side, int quantity) {
        TreeMap<Double, Integer> book = side == Order.Side.BUY ? asks : bids;
        double startMid = midPrice();
        int remaining = quantity;
        double notional = 0.0;
        double lastLevel = startMid;
        for (Map.Entry<Double, Integer> level : book.entrySet()) {
            if (remaining <= 0) break;
            int traded = Math.min(remaining, level.getValue());
            notional += traded * level.getKey();
            remaining -= traded;
            lastLevel = level.getKey();
        }
        int filled = quantity - remaining;
        double avg = filled > 0 ? notional / filled : startMid;
        double impact = lastLevel - startMid;
        return new ImpactResult(filled, avg, impact);
    }

    public TreeMap<Double, Integer> getBids() { return bids; }
    public TreeMap<Double, Integer> getAsks() { return asks; }
    public List<String> getTrades() { return trades; }
}
