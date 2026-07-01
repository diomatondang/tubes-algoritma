package sundara.ui;

import sundara.model.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MenuItemCard extends JPanel {

    private final MenuItem item;
    private boolean hovered = false;

    public MenuItemCard(MenuItem item, Runnable onAdd) {
        this.item = item;
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setPreferredSize(new Dimension(140, 115));

        boolean hasStock = item.getStock() > 0;
        if (hasStock) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }

        // Emoji label
        JLabel emojiLabel = new JLabel(item.getEmoji(), SwingConstants.CENTER);
        emojiLabel.setFont(Theme.FONT_EMOJI);
        emojiLabel.setForeground(Theme.TEXT_PRIMARY);
        emojiLabel.setOpaque(false);

        // Name label
        JLabel nameLabel = new JLabel("<html><center>" + item.getName() + "</center></html>", SwingConstants.CENTER);
        nameLabel.setFont(Theme.FONT_SMALL);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setOpaque(false);

        // Price / Status label
        JLabel priceLabel = new JLabel(hasStock ? item.getFormattedPrice() : "HABIS", SwingConstants.CENTER);
        priceLabel.setFont(Theme.FONT_PRICE);
        priceLabel.setForeground(hasStock ? Theme.TEXT_PRICE : Theme.BTN_DANGER);
        priceLabel.setOpaque(false);

        // Stock label
        JLabel stockLabel = new JLabel("Stok: " + item.getStock(), SwingConstants.CENTER);
        stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        stockLabel.setForeground(Theme.TEXT_SECONDARY);
        stockLabel.setOpaque(false);

        JPanel pricePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        pricePanel.setOpaque(false);
        pricePanel.add(priceLabel);
        pricePanel.add(stockLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(nameLabel, BorderLayout.CENTER);
        bottomPanel.add(pricePanel, BorderLayout.SOUTH);

        add(emojiLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Hover & click effects
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (item.getStock() > 0) {
                    hovered = true;
                    repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (item.getStock() > 0) {
                    onAdd.run();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background card rounded box
        Color bg;
        if (item.getStock() <= 0) {
            bg = new Color(18, 42, 80, 70); // Very dim / transparent
        } else {
            bg = hovered ? Theme.BG_ACCENT : Theme.BG_CARD;
        }
        g2.setColor(bg);
        g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
        
        // Card border (glowing on hover)
        Color borderCol;
        if (item.getStock() <= 0) {
            borderCol = new Color(220, 38, 38, 50); // Muted red border
        } else {
            borderCol = hovered ? Theme.BORDER_FOCUS : Theme.BORDER;
        }
        g2.setColor(borderCol);
        g2.setStroke(new BasicStroke(hovered ? 1.5f : 1.0f));
        g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 16, 16));
        
        g2.dispose();
    }
}
