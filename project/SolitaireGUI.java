import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SolitaireGUI extends JFrame {
    private final Deck deck;
    private final List<List<Card>> tableau;
    private final Stack<Card> stockPile;
    private final Stack<Card> wastePile;
    private JLabel stockLabel;
    private JLabel wasteLabel;
    private final Stack<Card>[] foundationPiles = new Stack[4];
    private JLabel[] foundationLabels = new JLabel[4];
    private Card draggedCard = null;
    private JLabel draggedLabel = null;
    private JPanel tableauPanel;

    public SolitaireGUI() {
    setTitle("Solitaire - 單人接龍");
    setSize(900, 600);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    deck = new Deck();
    tableau = new ArrayList<>();

    // 初始化 7 個 tableau 堆疊
    for (int i = 0; i < 7; i++) {
        List<Card> pile = new ArrayList<>();
        for (int j = 0; j <= i; j++) {
            Card card = deck.draw();
            if (j == i) card.flip(); // 最上面那張翻面
            pile.add(card);
        }
        tableau.add(pile);
    }

    stockPile = new Stack<>();
    wastePile = new Stack<>();
    for (int i = 0; i < 4; i++) {
        foundationPiles[i] = new Stack<>();
    }


    while (!deck.isEmpty()) {
        stockPile.push(deck.draw());
    }

    setupUI(); // 建立圖形介面
}

    private void setupUI() {
        
    JPanel container = new JPanel(null);
    container.setPreferredSize(new Dimension(900, 600));

    // === Stock ===
    stockLabel = new JLabel("🂠", SwingConstants.CENTER);
    stockLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
    stockLabel.setBounds(20, 20, 80, 100);
    stockLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    stockLabel.setOpaque(true);
    stockLabel.setBackground(Color.LIGHT_GRAY);
    stockLabel.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // 👉 左鍵：抽下一張（原本邏輯）
            if (!stockPile.isEmpty()) {
                Card card = stockPile.pop();
                card.flip();
                wastePile.push(card);
                wasteLabel.setText(card.toDisplayString());
            } else {
                // 若 stock 空了，把 waste 全部翻回來（Reset）
                while (!wastePile.isEmpty()) {
                    Card card = wastePile.pop();
                    card.flip();
                    stockPile.push(card);
                }
                wasteLabel.setText("");
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            // 👈 右鍵：回上一張
            if (!wastePile.isEmpty()) {
                Card card = wastePile.pop();
                card.flip();
                stockPile.push(card);
                wasteLabel.setText(wastePile.isEmpty() ? "" : wastePile.peek().toDisplayString());
            }
        }

        // 更新 stock 顯示符號
        stockLabel.setText(stockPile.isEmpty() ? "" : "🂠");
    }
});

    container.add(stockLabel);

    // === Waste ===
    wasteLabel = new JLabel("", SwingConstants.CENTER);
    wasteLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
    wasteLabel.setBounds(110, 20, 80, 100);
    wasteLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    wasteLabel.setOpaque(true);
    wasteLabel.setBackground(Color.WHITE);
    wasteLabel.addMouseListener(new MouseAdapter() {
    @Override
    public void mousePressed(MouseEvent e) {
        if (!wastePile.isEmpty()) {
            draggedCard = wastePile.peek();
            draggedLabel = wasteLabel;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Point dropPoint = e.getPoint();
        SwingUtilities.convertPointToScreen(dropPoint, wasteLabel);

        for (int i = 0; i < foundationLabels.length; i++) {
            Rectangle bounds = foundationLabels[i].getBounds();
            Point location = foundationLabels[i].getLocationOnScreen();
            Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

            if (screenRect.contains(dropPoint)) {
                if (canPlaceOnFoundation(foundationPiles[i], draggedCard)) {
                    wastePile.pop();
                    foundationPiles[i].push(draggedCard);
                    foundationLabels[i].setText(draggedCard.toDisplayString());
                    wasteLabel.setText(wastePile.isEmpty() ? "" : wastePile.peek().toDisplayString());
                }
                break;
            }
        }

        draggedCard = null;
        draggedLabel = null;
    }
});

    container.add(wasteLabel);
    // === Foundation 4 格 ===
    for (int i = 0; i < 4; i++) {
        JLabel foundationLabel = new JLabel("", SwingConstants.CENTER);
        foundationLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        foundationLabel.setBounds(220 + i * 100, 20, 80, 100);
        foundationLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        foundationLabel.setOpaque(true);
        foundationLabel.setBackground(Color.WHITE);

        final int index = i; // for use inside lambda/mouse listener
        foundationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedCard == null || draggedLabel == null) return;

                Stack<Card> foundation = foundationPiles[index];

                if (canPlaceOnFoundation(foundation, draggedCard)) {
                    wastePile.pop();
                    foundation.push(draggedCard);
                    foundationLabels[index].setText(draggedCard.toDisplayString());
                    wasteLabel.setText(wastePile.isEmpty() ? "" : wastePile.peek().toDisplayString());
                }
                

                draggedCard = null;
                draggedLabel = null;
            }
        });

        foundationLabels[i] = foundationLabel;
        container.add(foundationLabel);
    }

    // === Tableau 堆疊 ===
    int pileSpacing = 120;
    int cardOffsetY = 30;

    // 放在 tableau 迴圈「之前」執行一次
tableauPanel = new JPanel(null);
tableauPanel.setBounds(0, 150, 900, 450); // 下半部
container.add(tableauPanel);

for (int i = 0; i < tableau.size(); i++) {
    List<Card> pile = tableau.get(i);
    JLayeredPane pilePane = new JLayeredPane();
    pilePane.setBounds(i * pileSpacing + 20, 0, 100, 400);

    for (int j = 0; j < pile.size(); j++) {
        Card card = pile.get(j);
        JLabel cardLabel = new JLabel(card.toDisplayString(), SwingConstants.CENTER);
        cardLabel.setOpaque(true);
        cardLabel.setBackground(Color.WHITE);
        cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cardLabel.setBounds(0, j * cardOffsetY, 80, 100);
        cardLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        pilePane.add(cardLabel, Integer.valueOf(j));

        if (card.isFaceUp() && j == pile.size() - 1) {
            cardLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    draggedCard = card;
                    draggedLabel = cardLabel;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    handleTableauCardDrop(card, cardLabel);
                    draggedCard = null;
                    draggedLabel = null;
                }
            });
        }
    }

    tableauPanel.add(pilePane); // ✅ 放到 tableauPanel 中
}

    
    add(container);
}
private void moveWasteToFoundation(int index) {
    if (wastePile.isEmpty()) return;

    Card card = wastePile.peek(); // 先看不是先 pop
    Stack<Card> foundation = foundationPiles[index];

    if (canPlaceOnFoundation(foundation, card)) {
        wastePile.pop();
        foundation.push(card);
        foundationLabels[index].setText(card.toDisplayString());
        wasteLabel.setText(wastePile.isEmpty() ? "" : wastePile.peek().toDisplayString());
    }
}
private void handleTableauCardDrop(Card card, JLabel label) {
    if (draggedCard == null || draggedLabel == null) return;

    Point dropPoint = MouseInfo.getPointerInfo().getLocation();

    // 嘗試拖到 Foundation
    for (int i = 0; i < 4; i++) {
        Rectangle bounds = foundationLabels[i].getBounds();
        Point location = foundationLabels[i].getLocationOnScreen();
        Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

        if (screenRect.contains(dropPoint)) {
            if (canPlaceOnFoundation(foundationPiles[i], card)) {
                // 將牌從原 tableau 拿掉並加入 foundation
                removeCardFromTableau(card);
                foundationPiles[i].push(card);
                foundationLabels[i].setText(card.toDisplayString());
                label.setVisible(false); // 隱藏原本 JLabel
                return;
            }
        }
    }

    // 嘗試拖到 Tableau 堆
    for (int i = 0; i < tableau.size(); i++) {
        List<Card> pile = tableau.get(i);
        if (pile.isEmpty()) continue;

        Component comp = getComponentAtTableau(i);
        if (comp != null) {
            Rectangle bounds = comp.getBounds();
            Point location = comp.getLocationOnScreen();
            Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

            if (screenRect.contains(dropPoint)) {
                Card top = pile.get(pile.size() - 1);
                if (canPlaceOnTableau(top, card)) {
                    removeCardFromTableau(card);
                    tableau.get(i).add(card);
                    
                    return;
                }
            }
        }
    }
}
private void removeCardFromTableau(Card card) {
    for (List<Card> pile : tableau) {
        if (!pile.isEmpty() && pile.get(pile.size() - 1) == card) {
            pile.remove(card);
            if (!pile.isEmpty()) {
                Card newTop = pile.get(pile.size() - 1);
                if (!newTop.isFaceUp()) {
                    newTop.flip(); // ✅ 翻開下一張牌
                }
            }
            break;
        }
    }

    refreshUI(); // ✅ 更新畫面顯示（見下一步）
}
private void refreshUI() {
    tableauPanel.removeAll();  // 只清空 tableau 的內容
    tableauPanel.revalidate();
    tableauPanel.repaint();

    // 重新建構 tableau 的內容（你可以抽成 refreshTableau()）
    int pileSpacing = 120;
    int cardOffsetY = 30;

    for (int i = 0; i < tableau.size(); i++) {
        List<Card> pile = tableau.get(i);
        JLayeredPane pilePane = new JLayeredPane();
        pilePane.setBounds(i * pileSpacing + 20, 0, 100, 400);

        for (int j = 0; j < pile.size(); j++) {
            Card card = pile.get(j);
            JLabel cardLabel = new JLabel(card.toDisplayString(), SwingConstants.CENTER);
            cardLabel.setOpaque(true);
            cardLabel.setBackground(Color.WHITE);
            cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cardLabel.setBounds(0, j * cardOffsetY, 80, 100);
            cardLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
            pilePane.add(cardLabel, Integer.valueOf(j));

            if (card.isFaceUp() && j == pile.size() - 1) {
                cardLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        draggedCard = card;
                        draggedLabel = cardLabel;
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        handleTableauCardDrop(card, cardLabel);
                        draggedCard = null;
                        draggedLabel = null;
                    }
                });
            }
        }

        tableauPanel.add(pilePane);
    }

    tableauPanel.revalidate();
    tableauPanel.repaint();
}


private Component getComponentAtTableau(int index) {
    // 可以記住每個 pilePane 或用 container.getComponent(index)
    // 或直接用 foundationLabels[i] 的方式來找 tableau 顯示的位置
    return null; // 你可以自行補實作方式
}

private boolean canPlaceOnTableau(Card target, Card dragged) {
    boolean differentColor = (isRed(target) != isRed(dragged));
    boolean rankDown = dragged.getRank().ordinal() + 1 == target.getRank().ordinal();
    return differentColor && rankDown;
}

private boolean isRed(Card card) {
    return card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS;
}

private boolean canPlaceOnFoundation(Stack<Card> pile, Card card) {
    if (pile.isEmpty()) {
        return card.getRank() == Card.Rank.ACE;
    } else {
        Card top = pile.peek();
        return top.getSuit() == card.getSuit() &&
               card.getRank().ordinal() == top.getRank().ordinal() + 1;
    }
}

private void drawFromStock() {
    if (!stockPile.isEmpty()) {
        Card card = stockPile.pop();
        card.flip(); // 翻牌
        wastePile.push(card);
        wasteLabel.setText(card.toDisplayString());
    } else {
        // 如果沒牌了就重置
        while (!wastePile.isEmpty()) {
            Card card = wastePile.pop();
            card.flip(); // 反面朝下
            stockPile.push(card);
        }
        wasteLabel.setText("");
    }

    // 更新 stock 顯示
    stockLabel.setText(stockPile.isEmpty() ? "" : "🂠");
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SolitaireGUI game = new SolitaireGUI();
            game.setVisible(true);
        });
    }
}
