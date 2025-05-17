public class Card {
    public enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
    public enum Rank {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT,
        NINE, TEN, JACK, QUEEN, KING
    }

    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.faceUp = false;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }
    public boolean isFaceUp() { return faceUp; }

    public void flip() { faceUp = !faceUp; }

    public String toDisplayString() {
        return faceUp ? getSymbol() : "🂠";
    }

    private String getSymbol() {
        String rankStr;
        switch (rank) {
            case ACE -> rankStr = "A";
            case JACK -> rankStr = "J";
            case QUEEN -> rankStr = "Q";
            case KING -> rankStr = "K";
            default -> rankStr = String.valueOf(rank.ordinal() + 1);
        }

        String suitStr = switch (suit) {
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
            case SPADES -> "♠";
        };

        return suitStr + rankStr;
    }
}
