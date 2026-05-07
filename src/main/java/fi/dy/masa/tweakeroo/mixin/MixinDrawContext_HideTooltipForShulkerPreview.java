package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

/**
 * When the Shulker Box preview tooltip is enabled, hide the vanilla item tooltip text
 * and only render the preview overlay. This removes the extra tooltip "info" lines
 * the user doesn't want to see while previewing.
 */
@Mixin(DrawContext.class)
public abstract class MixinDrawContext_HideTooltipForShulkerPreview
{
    @Inject(method = "drawItemTooltip", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_hideTooltipForShulkerPreview(TextRenderer textRenderer, ItemStack stack, int x, int y, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_SHULKERBOX_DISPLAY.getBooleanValue())
        {
            boolean render = Configs.Generic.SHULKER_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown();

            if (render)
            {
                // Cancel vanilla tooltip text and render the preview instead
                fi.dy.masa.malilib.render.RenderUtils.renderShulkerBoxPreview(
                        stack, x, y,
                        Configs.Generic.SHULKER_DISPLAY_BACKGROUND_COLOR.getBooleanValue(),
                        (DrawContext) (Object) this);
                ci.cancel();
            }
        }
    }
}

