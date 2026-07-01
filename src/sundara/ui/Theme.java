package sundara.ui;

import java.awt.*;

public class Theme {
    // Background colors (translucent for glassmorphism)
    public static final Color BG_PRIMARY     = new Color(10, 22, 40, 160);
    public static final Color BG_SECONDARY   = new Color(13, 31, 60, 200);
    public static final Color BG_CARD        = new Color(18, 42, 80, 150);
    public static final Color BG_ACCENT      = new Color(30, 58, 95, 180);

    // Button colors (vibrant blue & electric accents)
    public static final Color BTN_PRIMARY    = new Color(0x00, 0x88, 0xFF);
    public static final Color BTN_PRIMARY_H  = new Color(0x00, 0x66, 0xEE);
    public static final Color BTN_DANGER     = new Color(0xDC, 0x26, 0x26);
    public static final Color BTN_DANGER_H   = new Color(0xB9, 0x1C, 0x1C);
    public static final Color BTN_SUCCESS    = new Color(0x16, 0xA3, 0x4A);
    public static final Color BTN_SUCCESS_H  = new Color(0x15, 0x80, 0x3D);
    public static final Color BTN_ORANGE     = new Color(0x00, 0xA2, 0xE8); // changed to cyan-blue
    public static final Color BTN_ORANGE_H   = new Color(0x00, 0x88, 0xC2);

    // Tab colors
    public static final Color TAB_ACTIVE     = new Color(0x00, 0x88, 0xFF);
    public static final Color TAB_INACTIVE   = new Color(30, 58, 95, 120);

    // Text colors
    public static final Color TEXT_PRIMARY   = new Color(0xFF, 0xFF, 0xFF);
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);
    public static final Color TEXT_ORANGE    = new Color(0x00, 0xD2, 0xFF); // changed to electric cyan
    public static final Color TEXT_PRICE     = new Color(0x38, 0xBD, 0xF8); // changed to light sky blue

    // Border
    public static final Color BORDER         = new Color(30, 58, 95, 100);
    public static final Color BORDER_FOCUS   = new Color(0x00, 0xBD, 0xFF);

    // Fonts
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_PRICE      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_HEADER     = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_EMOJI      = new Font("Segoe UI Emoji", Font.PLAIN, 28);
    public static final Font FONT_EMOJI_SM   = new Font("Segoe UI Emoji", Font.PLAIN, 18);
    public static final Font FONT_BTN        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TOTAL      = new Font("Segoe UI", Font.BOLD, 18);
}
