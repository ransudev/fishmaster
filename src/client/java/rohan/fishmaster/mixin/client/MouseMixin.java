package rohan.fishmaster.mixin.client;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mouse.class)
public interface MouseMixin {
    @Invoker("onMouseButton")
    void invokeOnMouseButton(long window, int button, int action, int mods);
}