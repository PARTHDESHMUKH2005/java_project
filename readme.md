# OrderBookViz

A Java Swing application that simulates a limit order book and visualizes
it live: a price chart of the evolving mid price and a depth view of the top
bid/ask levels.

## Problem and solution

Understanding how buy and sell orders interact in a market can be difficult
when the order book is only described theoretically. OrderBookViz solves this
problem by providing a small, visual simulation of a limit order book. It
matches incoming orders, maintains bid/ask depth, records the mid-price, and
updates the visualization in real time.

## Real-world impact

**Yes, at an educational and prototyping level.** The project helps students,
developers, and finance learners understand market microstructure, order
matching, liquidity, spreads, and price movement without connecting to a real
exchange or risking real money. It is not intended for live trading or
financial decision-making.

## Intended users

- Students learning data structures, trading systems, or financial markets.
- Developers prototyping order-book and market-simulation ideas.
- Instructors demonstrating matching engines and market depth visually.

## Technology stack

- **Language:** Java
- **GUI library:** Java Swing (`JFrame`, `JPanel`, `Timer`, and standard AWT
  drawing APIs)
- **Core concepts:** `TreeMap`-based price-level order book, limit-order
  matching, simulated traders, and real-time chart/depth rendering
- **Runtime:** JDK 8+; OpenJDK is recommended

This project does use a GUI library: Java Swing. No external dependencies are
required.

## Team members

| Name | ID |
|------|----|
| Parth Deshmukh | 1024031045 |
| Sanket Kamboj | 1024031042 |

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
