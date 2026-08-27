import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Abstract base for all trading agents. Subclasses must decide what orders to
 * submit each simulation step via {@link #act(OrderBook)}.
 */
public abstract class Trader {

    protected final String id;
    protected double cash;
    protected int inventory;

    protected Trader(String id, double cash) {
        this.id = id;
        this.cash = cash;
        this.inventory = 0;
    }

    public String getId() { return id; }
    public double getCash() { return cash; }
    public int getInventory() { return inventory; }

    /** Produce the orders this trader wants to submit against the given book. */
    public abstract List<Order> act(OrderBook book);
}

/**
 * Posts symmetric two-sided quotes around the current mid price.
 */
class MarketMaker extends Trader {

    private final double spread;
    private final int quoteSize;

    MarketMaker(String id, double cash, double spread, int quoteSize) {
        super(id, cash);
        this.spread = spread;
        this.quoteSize = quoteSize;
    }

    @Override
    public List<Order> act(OrderBook book) {
        List<Order> orders = new ArrayList<>();
        double mid = book.midPrice();
        orders.add(new Order(Order.Side.BUY, mid - spread / 2.0, quoteSize, id));
        orders.add(new Order(Order.Side.SELL, mid + spread / 2.0, quoteSize, id));
        return orders;
    }
}

/**
 * Submits a single randomly-sized, randomly-priced order each step.
 */
class RandomTrader extends Trader {

    private final Random rng;
    private final int maxSize;

    RandomTrader(String id, double cash, int maxSize, Random rng) {
        super(id, cash);
        this.maxSize = maxSize;
        this.rng = rng;
    }

    @Override
    public List<Order> act(OrderBook book) {
        List<Order> orders = new ArrayList<>();
        double mid = book.midPrice();
        Order.Side side = rng.nextBoolean() ? Order.Side.BUY : Order.Side.SELL;
        int qty = 1 + rng.nextInt(maxSize);
        double offset = rng.nextInt(3);
        double price = side == Order.Side.BUY ? mid + offset : mid - offset;
        orders.add(new Order(side, price, qty, id));
        return orders;
    }
}
