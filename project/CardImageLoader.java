import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CardImageLoader {
    private static final String CARD_FOLDER = "cards/"; // 圖片存放資料夾
    private static final Map<String, ImageIcon> cardImageCache = new HashMap<>();

    // 載入單張卡牌圖案（如 ace_of_spades、10_of_hearts 等）
    public static ImageIcon getCardImage(String rank, String suit, boolean faceUp) {
        if (!faceUp) return loadImage("back");
        return loadImage(rank.toLowerCase() + "_of_" + suit.toLowerCase());
    }

    // 載入指定名稱的圖片（會自動快取）
    private static ImageIcon loadImage(String name) {
        if (cardImageCache.containsKey(name)) {
            return cardImageCache.get(name);
        }

        String path = CARD_FOLDER + name + ".png";
        ImageIcon icon = new ImageIcon(path);

        // 可選：縮放圖片
        Image scaled = icon.getImage().getScaledInstance(80, 100, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);
        cardImageCache.put(name, scaledIcon);
        return scaledIcon;
    }

    // 從 Card 類別取得對應圖片（建議你呼叫這個）
    public static ImageIcon getCardImage(Card card) {
        String rank = switch (card.getRank()) {
            case ACE -> "ace";
            case JACK -> "jack";
            case QUEEN -> "queen";
            case KING -> "king";
            default -> String.valueOf(card.getRank().ordinal() + 1);
        };

        String suit = switch (card.getSuit()) {
            case SPADES -> "spades";
            case HEARTS -> "hearts";
            case DIAMONDS -> "diamonds";
            case CLUBS -> "clubs";
        };

        return getCardImage(rank, suit, card.isFaceUp());
    }
} 
