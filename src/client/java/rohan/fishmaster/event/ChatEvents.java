package rohan.fishmaster.event;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import rohan.fishmaster.feature.FishingTracker;

public class ChatEvents {
    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();
            // Pass chat messages to FishingTracker for sea creature detection
            FishingTracker.onChatMessage(text);
        });
    }
}
