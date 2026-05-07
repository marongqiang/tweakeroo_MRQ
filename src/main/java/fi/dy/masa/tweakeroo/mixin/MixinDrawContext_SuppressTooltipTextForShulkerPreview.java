package fi.dy.masa.tweakeroo.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

/**
 * Suppress vanilla tooltip text rendering when the Shulker preview is active.
 * This is a global hook that works across screens (including Creative) because
 * they ultimately call DrawContext.drawTooltip(...) to render the text box.
 */
@Mixin(DrawContext.class)
public abstract class MixinDrawContext_SuppressTooltipTextForShulkerPreview
{
    @Inject(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tweakeroo_suppressTooltipTextForShulkerPreview(TextRenderer textRenderer,
                                                                List<?> lines,
                                                                Optional<?> data,
                                                                int x,
                                                                int y,
                                                                CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_SHULKERBOX_DISPLAY.getBooleanValue() == false)
        {
            return;
        }

        boolean render = Configs.Generic.SHULKER_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown();
        if (render == false)
        {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.currentScreen instanceof HandledScreen<?> handled)
        {
            Slot slot = ((IMixinHandledScreenFocusedSlot) handled).tweakeroo_getFocusedSlot();

            if (slot != null && slot.hasStack())
            {
                ItemStack stack = slot.getStack();

                // Only suppress when the preview would draw something
                if (stack.hasNbt())
                {
                    ci.cancel();
                }
            }
        }
    }
}

