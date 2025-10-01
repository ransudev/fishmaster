package rohan.fishmaster.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rohan.fishmaster.feature.AutoFishingFeature;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    
    @Shadow
    public GameOptions options;
    
    /**
     * Intercept window focus changes to handle auto fishing state properly
     */
    @Inject(method = "onWindowFocusChanged", at = @At("TAIL"))
    private void onWindowFocusChanged(boolean focused, CallbackInfo ci) {
        // Notify auto fishing feature about the window focus change
        if (AutoFishingFeature.isEnabled()) {
            AutoFishingFeature.onWindowFocusChanged(focused);
        }
    }
}