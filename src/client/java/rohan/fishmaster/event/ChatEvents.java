package rohan.fishmaster.event;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

public class ChatEvents {
    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();
            // Pass chat messages to FishingTracker for sea creature detection
            // Removed: FishingTracker.onChatMessage(text);
        });
    }
}
