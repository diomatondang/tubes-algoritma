package sundara.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Displays the real Sundara logo image (logo.jpg from assets).
 * Falls back to text if image cannot be loaded.
 */
public class SundaraLogo extends JComponent {

    public enum Style { FULL, ICON_ONLY }

    private static BufferedImage logoImage = null;

    static {
        try {
            InputStream is = SundaraLogo.class
                    .getResourceAsStream("/sundara/assets/logo.jpg");
            if (is != null) {
                logoImage = ImageIO.read(is);
                is.close();
            }
        } catch (Exception e) {
            logoImage = null;
        }
    }

    private final Style style;
    private final int   targetSize;
    private final float alpha;

    public SundaraLogo(Style style, int targetSize, float alpha) {
        this.style      = style;
        this.targetSize = targetSize;
        this.alpha      = alpha;
        setOpaque(false);
        int h = (style == Style.FULL) ? (int)(targetSize * 1.5) : targetSize;
        setPreferredSize(new Dimension(targetSize, h));
    }

    public SundaraLogo(int size) {
        this(Style.FULL, size, 1.0f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (logoImage == null) {
            drawFallback(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int w = getWidth();
        int h = getHeight();

        if (style == Style.ICON_ONLY) {
            // Draw just the icon portion (top ~60% of the square logo)
            int imgW = logoImage.getWidth();
            int imgH = logoImage.getHeight();
            // Crop top portion (the S icon)
            int cropH = (int)(imgH * 0.58);
            BufferedImage cropped = logoImage.getSubimage(0, 0, imgW, cropH);
            // Scale to fit
            int drawSize = Math.min(w, h);
            int dx = (w - drawSize) / 2;
            int dy = (h - drawSize) / 2;
            g2.drawImage(cropped, dx, dy, drawSize, drawSize, null);
        } else {
            // Draw full logo centered
            // Keep aspect ratio
            int imgW = logoImage.getWidth();
            int imgH = logoImage.getHeight();
            double ratio = (double) imgW / imgH;
            int drawW = w;
            int drawH = (int)(drawW / ratio);
            if (drawH > h) { drawH = h; drawW = (int)(drawH * ratio); }
            int dx = (w - drawW) / 2;
            int dy = (h - drawH) / 2;
            g2.drawImage(logoImage, dx, dy, drawW, drawH, null);
        }

        g2.dispose();
    }

    /** Text fallback if image missing */
    private void drawFallback(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, targetSize / 4));
        FontMetrics fm = g2.getFontMetrics();
        String t = "sundara";
        g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
                getHeight() / 2 + fm.getAscent() / 2);
        g2.dispose();
    }

    // ── Static factories ─────────────────────────────────────────────

    /** Full logo for login screen */
    public static SundaraLogo loginLogo() {
        return new SundaraLogo(Style.FULL, 200, 1.0f) {
            { setPreferredSize(new Dimension(220, 220)); }
        };
    }

    /** Faint watermark for menu background */
    public static SundaraLogo watermark(int size) {
        return new SundaraLogo(Style.ICON_ONLY, size, 0.07f);
    }

    public static boolean isLoaded() {
        return logoImage != null;
    }

    public static BufferedImage getLogoImage() {
        return logoImage;
    }
}
