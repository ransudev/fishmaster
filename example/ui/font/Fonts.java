// 
// Decompiled by Procyon v0.6.0
// 

package dev.quiteboring.cobalt.ui.font;

import org.jetbrains.annotations.NotNull;
import kotlin.Metadata;

@Metadata(mv = { 2, 2, 0 }, k = 1, xi = 48, d1 = { """
                                                   \u0000\u0014
                                                   \u0002\u0018\u0002
                                                   \u0002\u0010\u0000
                                                   \u0002\b\u0002
                                                   \u0002\u0018\u0002
                                                   \u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B	\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u0017\u0010\u0005\u001a\u00020\u00048\u0006¢\u0006\f
                                                   \u0004\b\u0005\u0010\u0006\u001a\u0004\b\u0007\u0010\b¨\u0006	""" }, d2 = { "Ldev/quiteboring/cobalt/ui/font/Fonts;", "", "<init>", "()V", "Ldev/quiteboring/cobalt/ui/font/NvgFontProvider;", "font", "Ldev/quiteboring/cobalt/ui/font/NvgFontProvider;", "getFont", "()Ldev/quiteboring/cobalt/ui/font/NvgFontProvider;", "CobaltFabric" })
public final class Fonts
{
    @NotNull
    public static final Fonts INSTANCE;
    @NotNull
    private static final NvgFontProvider font;
    
    private Fonts() {
    }
    
    @NotNull
    public final NvgFontProvider getFont() {
        return Fonts.font;
    }
    
    static {
        INSTANCE = new Fonts();
        font = new NvgFontProvider("/assets/cobalt/fonts/Exo2-SemiBold.ttf");
    }
}
