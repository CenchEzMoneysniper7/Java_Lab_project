import java.util.*;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
    public void reset() {
    cards.clear();
    for (Card.Suit suit : Card.Suit.values()) {
        for (Card.Rank rank : Card.Rank.values()) {
            cards.add(new Card(suit, rank));
        }
    }
    Collections.shuffle(cards);
}

}
