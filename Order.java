/**
 * A single resting or incoming order, plus the result type used to describe
 * the impact of executing against the book.
 */
public class Order {

    public enum Side { BUY, SELL }

    private final Side side;
    private final double price;
    private int quantity;
    private final String traderId;

    public Order(Side side, double price, int quantity, String traderId) {
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.traderId = traderId;
    }

    public Side getSide() { return side; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getTraderId() { return traderId; }

    @Override
    public String toString() {
        return String.format("%s %d @ %.2f (%s)", side, quantity, price, traderId);
    }
}

/**
 * Outcome of walking the book with a marketable order: how much filled, at
 * what average price, and how far the touch moved.
 */
class ImpactResult {

    private final int filledQuantity;
    private final double avgFillPrice;
    private final double priceImpact;

    public ImpactResult(int filledQuantity, double avgFillPrice, double priceImpact) {
        this.filledQuantity = filledQuantity;
        this.avgFillPrice = avgFillPrice;
        this.priceImpact = priceImpact;
    }

    public int getFilledQuantity() { return filledQuantity; }
    public double getAvgFillPrice() { return avgFillPrice; }
    public double getPriceImpact() { return priceImpact; }

    @Override
    public String toString() {
        return String.format("filled=%d avg=%.2f impact=%.2f",
                filledQuantity, avgFillPrice, priceImpact);
    }
}
