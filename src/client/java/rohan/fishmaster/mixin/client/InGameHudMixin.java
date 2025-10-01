package rohan.fishmaster.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rohan.fishmaster.feature.AutoFishingFeature;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    
    /**
     * Inject into the render method to handle pause menu prevention when auto fishing is enabled
     * and the mouse is ungrabbed
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // This injection point allows us to potentially modify behavior before rendering
        // We'll handle the pause menu prevention in the MinecraftClientMixin instead
        // which is more reliable for this use case
    }
}
