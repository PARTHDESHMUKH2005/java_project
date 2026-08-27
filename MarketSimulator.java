import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Drives the simulation: on each step every trader acts against the book and
 * the resulting mid price is recorded for charting.
 */
public class MarketSimulator {

    private final OrderBook book;
    private final List<Trader> traders = new ArrayList<>();
    private final List<Double> priceHistory = new ArrayList<>();

    public MarketSimulator(double initialPrice) {
        this.book = new OrderBook(initialPrice);
        Random rng = new Random(42);
        traders.add(new MarketMaker("MM-1", 1_000_000, 2.0, 50));
        traders.add(new RandomTrader("RT-1", 500_000, 10, rng));
        traders.add(new RandomTrader("RT-2", 500_000, 10, rng));
        priceHistory.add(initialPrice);
    }

    public void step() {
        for (Trader trader : traders) {
            for (Order order : trader.act(book)) {
                book.submit(order);
            }
        }
        priceHistory.add(book.midPrice());
    }

    public OrderBook getBook() { return book; }
    public List<Double> getPriceHistory() { return priceHistory; }
    public List<Trader> getTraders() { return traders; }
}
