package dev.atlasclient.themes;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Abstract base class for UI themes Handles rendering of wallpapers, buttons, HUD elements, panels, etc.
 */
public abstract class AbstractThemeRenderer {

  /**
   * Renders the background/wallpaper
   */
  public abstract void renderBackground(DrawContext context, int width, int height);

  /**
   * Renders a button with various states
   */
  public abstract void renderButton(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    boolean hovered,
    boolean pressed,
    String text,
    TextRenderer font);

  /**
   * Renders a HUD panel (semi-transparent overlay)
   */
  public abstract void renderHudPanel(DrawContext context, int x, int y, int width, int height);

  /**
   * Renders a title bar
   */
  public abstract void renderTitleBar(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    String title,
    TextRenderer font,
    boolean active);

  /**
   * Renders a taskbar at the bottom of the screen
   */
  public abstract void renderTaskbar(DrawContext context, int screenWidth, int screenHeight, int height);

  /**
   * Renders a panel/window background
   */
  public abstract void renderPanel(DrawContext context, int x, int y, int width, int height);

  /**
   * Renders a text input field
   */
  public abstract void renderTextField(DrawContext context, int x, int y, int width, int height, boolean focused);

  /**
   * Renders a selection highlight
   */
  public abstract void renderSelection(DrawContext context, int x, int y, int width, int height);

  /**
   * Renders a progress bar
   */
  public abstract void renderProgressBar(DrawContext context, int x, int y, int width, int height, float progress);

  /**
   * Renders a slider
   */
  public abstract void renderSlider(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    float value,
    boolean hovered);

  /**
   * Renders a checkbox
   */
  public abstract void renderCheckbox(DrawContext context, int x, int y, int size, boolean checked, boolean hovered);

  /**
   * Renders a separator line
   */
  public abstract void renderSeparator(DrawContext context, int x, int y, int width);

  /**
   * Gets the theme name
   */
  public abstract String getThemeName();

  /**
   * Called when theme is initialized (for setup/animations)
   */
  public void initialize() {
  }

  /**
   * Called every frame for animations
   */
  public void update() {
  }

  /**
   * Utility: Blend two ARGB colors
   */
  protected int blendColors(int color1, int color2, float ratio) {
    int a1 = (color1 >> 24) & 0xFF;
    int r1 = (color1 >> 16) & 0xFF;
    int g1 = (color1 >> 8) & 0xFF;
    int b1 = color1 & 0xFF;

    int a2 = (color2 >> 24) & 0xFF;
    int r2 = (color2 >> 16) & 0xFF;
    int g2 = (color2 >> 8) & 0xFF;
    int b2 = color2 & 0xFF;

    int a = (int) (a1 + (a2 - a1) * ratio);
    int r = (int) (r1 + (r2 - r1) * ratio);
    int g = (int) (g1 + (g2 - g1) * ratio);
    int b = (int) (b1 + (b2 - b1) * ratio);

    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  /**
   * Utility: Draw vertical gradient
   */
  protected void drawVerticalGradient(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    int topColor,
    int bottomColor) {
    for (int i = 0; i < height; i++) {
      float ratio = (float) i / height;
      int color = blendColors(topColor, bottomColor, ratio);
      context.fill(x, y + i, x + width, y + i + 1, color);
    }
  }

  /**
   * Utility: Draw horizontal gradient
   */
  protected void drawHorizontalGradient(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    int leftColor,
    int rightColor) {
    for (int i = 0; i < width; i++) {
      float ratio = (float) i / width;
      int color = blendColors(leftColor, rightColor, ratio);
      context.fill(x + i, y, x + i + 1, y + height, color);
    }
  }
}