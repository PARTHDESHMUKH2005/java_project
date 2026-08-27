# OrderBookViz

A small Java Swing application that simulates a limit order book and visualizes
it live: a price chart of the evolving mid price and a depth view of the top
bid/ask levels.

## What it does

- Maintains a price/time-priority **limit order book** (bids high→low, asks low→high).
- Runs a **market simulation** where trading agents act each tick:
  - a **market maker** posting symmetric two-sided quotes around the mid, and
  - **random traders** submitting randomly sized/priced orders.
- Renders two panels on a Swing timer (advances every 200 ms):
  - **PriceChartPanel** — a line chart of mid-price history, and
  - **OrderBookPanel** — horizontal depth bars for the top bid/ask levels.

## Project layout

Classes are grouped by relationship, one logical unit per file:

| File | Classes |
|------|---------|
| `Order.java` | `Order`, `ImpactResult` |
| `Trader.java` | `Trader` (abstract), `MarketMaker`, `RandomTrader` |
| `OrderBook.java` | `OrderBook` |
| `MarketSimulator.java` | `MarketSimulator` |
| `MainFrame.java` | `MainFrame`, `PriceChartPanel`, `OrderBookPanel` |

## Build

```bash
javac Order.java Trader.java OrderBook.java MarketSimulator.java MainFrame.java
```

Requires a JDK (tested with OpenJDK). Build artifacts (`*.class`) are gitignored.

## Run

```bash
java MainFrame
```

A window titled **OrderBookViz** opens with the price chart on the left and the
order-book depth view on the right, updating in real time.
