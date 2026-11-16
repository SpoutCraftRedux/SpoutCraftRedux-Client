package org.spoutcraftredux.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

/**
 * Notification popup that slides down at the top right of the screen.
 */
public final class NotificationToast implements Toast {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");
    private static final Object TOKEN = new Object();
    private static final int WIDTH = 160;
    private static final int HEIGHT = 32;
    private static final int TITLE_COLOR = 0xFFFFFF00;
    private static final int MESSAGE_COLOR = 0xFFFFFFFF;

    private Component title;
    private Component message;
    private ItemStack icon;
    private long durationMillis;
    private long startMillis;
    private float slide;

    private NotificationToast(Component title, Component message, ItemStack icon, long durationMillis) {
        this.set(title, message, icon, durationMillis);
    }

    /** Shows a notification, replacing the one on screen. */
    public static void show(ToastManager manager, Component title, Component message, ItemStack icon, long durationMillis) {
        NotificationToast current = manager.getToast(NotificationToast.class, TOKEN);
        if (current != null) {
            current.set(title, message, icon, durationMillis);
        } else {
            manager.addToast(new NotificationToast(title, message, icon, durationMillis));
        }
    }

    private void set(Component title, Component message, ItemStack icon, long durationMillis) {
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.durationMillis = durationMillis;
        this.startMillis = Util.getMillis();
        this.slide = 1.0F;
    }

    private double progress() {
        return (double) (Util.getMillis() - this.startMillis) / this.durationMillis;
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.progress() > 1.0 ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        // In for the first eighth, out for the last eighth, eased with a fourth power
        double t = Math.min(Math.max(this.progress(), 0.0), 1.0) * 2.0;
        if (t > 1.0) {
            t = 2.0 - t;
        }
        t = Math.max(1.0 - t * 4.0, 0.0);
        t *= t;
        t *= t;
        this.slide = (float) t;
    }

    @Override
    public float xPos(int screenWidth, float visibilityPortion) {
        return screenWidth - WIDTH;
    }

    @Override
    public float yPos(int slot) {
        return -(int) (this.slide * 36.0F);
    }

    @Override
    public Object getToken() {
        return TOKEN;
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        return HEIGHT;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, WIDTH, HEIGHT);
        graphics.text(font, this.title, 30, 7, TITLE_COLOR, false);
        graphics.text(font, this.message, 30, 18, MESSAGE_COLOR, false);
        if (!this.icon.isEmpty()) {
            graphics.item(this.icon, 8, 8);
        }
    }
}
