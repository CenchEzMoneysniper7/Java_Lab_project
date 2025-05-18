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
    private List<Card> draggedStack = new ArrayList<>();
    private JLabel stockCountLabel;

    public SolitaireGUI() {
        setTitle("Solitaire - 單人接龍");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        deck = new Deck();
        tableau = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            List<Card> pile = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                Card card = deck.draw();
                if (j == i) card.flip();
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

        setupUI();
    }
    private void updateStockCount() {
        int total = stockPile.size() + wastePile.size();
        int open = wastePile.size();
        stockCountLabel.setText("" + open + " / " + total);
    }

    private void setupUI() {
        JPanel container = new JPanel(null);
        container.setPreferredSize(new Dimension(900, 600));

        stockCountLabel = new JLabel("", SwingConstants.CENTER);
        stockCountLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        stockCountLabel.setBounds(20, 0, 80, 20); // 位於 stockLabel 上方
        container.add(stockCountLabel);

        stockLabel = new JLabel(CardImageLoader.getCardImage(null, null, false));
        stockLabel.setBounds(20, 20, 80, 100);
        stockLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        stockLabel.setOpaque(true);
        stockLabel.setBackground(Color.LIGHT_GRAY);
        stockLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!stockPile.isEmpty()) {
                        Card card = stockPile.pop();
                        card.flip();
                        wastePile.push(card);
                        wasteLabel.setIcon(CardImageLoader.getCardImage(card));
                    } else {
                        while (!wastePile.isEmpty()) {
                            Card card = wastePile.pop();
                            card.flip();
                            stockPile.push(card);
                        }
                        wasteLabel.setIcon(null);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (!wastePile.isEmpty()) {
                        Card card = wastePile.pop();
                        card.flip();
                        stockPile.push(card);
                        wasteLabel.setIcon(wastePile.isEmpty() ? null : CardImageLoader.getCardImage(wastePile.peek()));
                    }
                }
                updateStockCount();
                stockLabel.setIcon(stockPile.isEmpty() ? null : CardImageLoader.getCardImage(null, null, false));
            }
        });
        container.add(stockLabel);

        wasteLabel = new JLabel();
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
                
                //Foundation
                for (int i = 0; i < foundationLabels.length; i++) {
                    Rectangle bounds = foundationLabels[i].getBounds();
                    Point location = foundationLabels[i].getLocationOnScreen();
                    Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

                    if (screenRect.contains(dropPoint)) {
                        if (canPlaceOnFoundation(foundationPiles[i], draggedCard)) {
                            wastePile.pop();
                            foundationPiles[i].push(draggedCard);
                            foundationLabels[i].setIcon(CardImageLoader.getCardImage(draggedCard));
                            wasteLabel.setIcon(wastePile.isEmpty() ? null : CardImageLoader.getCardImage(wastePile.peek()));
                            updateStockCount();
                        }
                        draggedCard = null;
                        draggedLabel = null;
                        return;
                    }
                }
                // tableau 的堆疊
                for (int i = 0; i < tableau.size(); i++) {
                    List<Card> pile = tableau.get(i);
                    if (!pile.isEmpty()) {
                        Component comp = getComponentAtTableau(i);
                        if (comp != null) {
                            Rectangle bounds = comp.getBounds();
                            Point location = comp.getLocationOnScreen();
                            Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

                            if (screenRect.contains(dropPoint)) {
                                Card top = pile.get(pile.size() - 1);
                                if (canPlaceOnTableau(top, draggedCard)) {
                                    wastePile.pop();
                                    pile.add(draggedCard);
                                    wasteLabel.setIcon(wastePile.isEmpty() ? null : CardImageLoader.getCardImage(wastePile.peek()));
                                    updateStockCount();
                                    refreshUI();
                                    draggedCard = null;
                                    draggedLabel = null;
                                    return;
                                }
                            }
                        }
                    }
                }
                //tableau 堆（只允許 K）
                for (int i = 0; i < tableau.size(); i++) {
                    List<Card> pile = tableau.get(i);
                    if (pile.isEmpty()) {
                        Component comp = getComponentAtTableau(i);
                        if (comp != null) {
                            Rectangle bounds = comp.getBounds();
                            Point location = comp.getLocationOnScreen();
                            Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

                            if (screenRect.contains(dropPoint)) {
                                if (draggedCard.getRank() == Card.Rank.KING) {
                                    wastePile.pop();
                                    pile.add(draggedCard);
                                    wasteLabel.setIcon(wastePile.isEmpty() ? null : CardImageLoader.getCardImage(wastePile.peek()));
                                    refreshUI();
                                    draggedCard = null;
                                    draggedLabel = null;
                                    return;
                                }
                            }
                        }
                    }
                }
                draggedCard = null;
                draggedLabel = null;
            }
        });
        container.add(wasteLabel);

        for (int i = 0; i < 4; i++) {
            JLabel foundationLabel = new JLabel();
            foundationLabel.setBounds(220 + i * 100, 20, 80, 100);
            foundationLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            foundationLabel.setOpaque(true);
            foundationLabel.setBackground(Color.WHITE);

            final int index = i;
            foundationLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (draggedCard == null || draggedLabel == null) return;

                    Stack<Card> foundation = foundationPiles[index];

                    if (canPlaceOnFoundation(foundation, draggedCard)) {
                        wastePile.pop();
                        foundation.push(draggedCard);
                        foundationLabels[index].setIcon(CardImageLoader.getCardImage(draggedCard));
                        wasteLabel.setIcon(wastePile.isEmpty() ? null : CardImageLoader.getCardImage(wastePile.peek()));
                    }

                    draggedCard = null;
                    draggedLabel = null;
                }
            });

            foundationLabels[i] = foundationLabel;
            container.add(foundationLabel);
        }

        tableauPanel = new JPanel(null);
        tableauPanel.setBounds(0, 150, 900, 450);
        container.add(tableauPanel);

        int pileSpacing = 120;
        int cardOffsetY = 30;

        for (int i = 0; i < tableau.size(); i++) {
            List<Card> pile = tableau.get(i);
            JLayeredPane pilePane = new JLayeredPane();
            pilePane.setBounds(i * pileSpacing + 20, 0, 100, 400);

            for (int j = 0; j < pile.size(); j++) {
                Card card = pile.get(j);
                JLabel cardLabel = new JLabel(CardImageLoader.getCardImage(card));
                cardLabel.setOpaque(true);
                cardLabel.setBackground(Color.WHITE);
                cardLabel.setBounds(0, j * cardOffsetY, 80, 100);
                cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                if (card.isFaceUp()) {
                    cardLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            draggedCard = card;
                            draggedLabel = cardLabel;
                            draggedStack.clear();

                            // 找到此牌所在堆與位置
                            for (List<Card> pile : tableau) {
                                int index = pile.indexOf(card);
                                if (index != -1) {
                                    // 將 index 之後的所有牌加入 draggedStack
                                    draggedStack.addAll(pile.subList(index, pile.size()));
                                    break;
                                }
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            handleTableauStackDrop(cardLabel);
                            draggedCard = null;
                            draggedLabel = null;
                            draggedStack.clear();
                        }
                    });
                }


                pilePane.add(cardLabel, Integer.valueOf(j));
            }

            tableauPanel.add(pilePane);
        }

        add(container);
        JButton restartButton = new JButton("重新開始");
        restartButton.setBounds(750, 20, 120, 30); // 放右上角
        container.add(restartButton);

        restartButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "確定要重新開始嗎？", "重新遊戲",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                restartGame();
            }
        });

    }
private void restartGame() {
    getContentPane().removeAll();

    // Reset UI & internal state
    draggedCard = null;
    draggedLabel = null;
    draggedStack.clear();

    // Reset foundation piles
    for (int i = 0; i < 4; i++) {
        foundationPiles[i].clear();
    }

    // Clear all piles
    wastePile.clear();
    stockPile.clear();
    tableau.clear();

    // 重建牌組並洗牌
    deck.reset();

    // 建立新的 tableau
    for (int i = 0; i < 7; i++) {
        List<Card> pile = new ArrayList<>();
        for (int j = 0; j <= i; j++) {
            Card card = deck.draw();
            if (j == i) card.flip(); // 最上面的翻面
            pile.add(card);
        }
        tableau.add(pile);
    }

    // 剩下的牌放入 stock
    while (!deck.isEmpty()) {
        stockPile.push(deck.draw());
    }

    // 重建 UI
    setupUI();
    revalidate();
    repaint();
}


private void handleTableauStackDrop(JLabel label) {
    if (draggedStack.isEmpty()) return;
    Point dropPoint = MouseInfo.getPointerInfo().getLocation();

    // ✅ Step 1: 嘗試拖到 Foundation
    if (draggedStack.size() == 1) { // 只能單張放上 foundation
        Card card = draggedStack.get(0);

        for (int i = 0; i < 4; i++) {
            Rectangle bounds = foundationLabels[i].getBounds();
            Point location = foundationLabels[i].getLocationOnScreen();
            Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

            if (screenRect.contains(dropPoint)) {
                if (canPlaceOnFoundation(foundationPiles[i], card)) {
                    removeCardsFromTableau(draggedStack);
                    foundationPiles[i].push(card);
                    foundationLabels[i].setIcon(CardImageLoader.getCardImage(card));
                    foundationLabels[i].setOpaque(true);
                    foundationLabels[i].setBackground(Color.WHITE);
                    refreshUI();
                    return;
                }
            }
        }
    }

    // ✅ Step 2: 拖到 Tableau（原本的邏輯）
    for (int i = 0; i < tableau.size(); i++) {
        List<Card> pile = tableau.get(i);
        if (pile.isEmpty()) continue;

        Component comp = getComponentAtTableau(i);
        if (comp != null) {
            Rectangle bounds = comp.getBounds();
            Point location = comp.getLocationOnScreen();
            Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

            if (screenRect.contains(dropPoint)) {
                Card target = pile.get(pile.size() - 1);
                Card moving = draggedStack.get(0);

                if (canPlaceOnTableau(target, moving)) {
                    removeCardsFromTableau(draggedStack);
                    pile.addAll(draggedStack);
                    refreshUI();
                    return;
                }
            }
        }
    }

    // ✅ Step 3: 拖到空堆（補充可選功能）
    for (int i = 0; i < tableau.size(); i++) {
        List<Card> pile = tableau.get(i);
        if (pile.isEmpty()) {
            Component comp = getComponentAtTableau(i);
            if (comp != null) {
                Rectangle bounds = comp.getBounds();
                Point location = comp.getLocationOnScreen();
                Rectangle screenRect = new Rectangle(location.x, location.y, bounds.width, bounds.height);

                if (screenRect.contains(dropPoint)) {
                    // 只允許 K 開頭的堆疊放入空堆
                    Card top = draggedStack.get(0);
                    if (top.getRank() == Card.Rank.KING) {
                        removeCardsFromTableau(draggedStack);
                        tableau.get(i).addAll(draggedStack);
                        refreshUI();
                        return;
                    }
                }
            }
        }
    }
}

private void removeCardsFromTableau(List<Card> cards) {
    Card first = cards.get(0);
    for (List<Card> pile : tableau) {
        int index = pile.indexOf(first);
        if (index != -1) {
            pile.subList(index, pile.size()).clear();
            if (!pile.isEmpty() && !pile.get(pile.size() - 1).isFaceUp()) {
                pile.get(pile.size() - 1).flip();
            }
            return;
        }
    }
}

private void moveWasteToFoundation(int index) {
    if (wastePile.isEmpty()) return;

    Card card = wastePile.peek(); // 先看不是先 pop
    Stack<Card> foundation = foundationPiles[index];

    if (canPlaceOnFoundation(foundation, card)) {
        wastePile.pop();
        foundation.push(card);
        foundationLabels[index].setIcon(CardImageLoader.getCardImage(card));
        foundationLabels[index].setOpaque(true);
        foundationLabels[index].setBackground(Color.WHITE);
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
                removeCardFromTableau(card);
                foundationPiles[i].push(card);
                foundationLabels[i].setIcon(CardImageLoader.getCardImage(card)); 
                foundationLabels[i].setOpaque(true); 
                foundationLabels[i].setBackground(Color.WHITE);
                label.setVisible(false);
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
            JLabel cardLabel = new JLabel(CardImageLoader.getCardImage(card));
            cardLabel.setOpaque(true);
            cardLabel.setBackground(Color.WHITE);
            cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cardLabel.setBounds(0, j * cardOffsetY, 80, 100);
            pilePane.add(cardLabel, Integer.valueOf(j));

            if (card.isFaceUp()) {
                cardLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        draggedCard = card;
                        draggedLabel = cardLabel;
                        draggedStack.clear();

                        for (List<Card> p : tableau) {
                            int idx = p.indexOf(card);
                            if (idx != -1) {
                                draggedStack.addAll(p.subList(idx, p.size()));
                                break;
                            }
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        handleTableauStackDrop(cardLabel);
                        draggedCard = null;
                        draggedLabel = null;
                        draggedStack.clear();
                    }
                });
            }

        }

        tableauPanel.add(pilePane);
    }

    tableauPanel.revalidate();
    tableauPanel.repaint();
    checkGameWin();
}

private void checkGameWin() {
    boolean allComplete = true;
    for (Stack<Card> pile : foundationPiles) {
        if (pile.size() < 13) {
            allComplete = false;
            break;
        }
    }

    if (allComplete) {
        int choice = JOptionPane.showOptionDialog(this,
                "恭喜！你完成了遊戲！\n是否要重新開始？",
                "遊戲結束",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"重新開始", "退出"},
                "重新開始");

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }
}

private Component getComponentAtTableau(int index) {
    if (tableauPanel.getComponentCount() <= index) return null;
    return tableauPanel.getComponent(index);
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
