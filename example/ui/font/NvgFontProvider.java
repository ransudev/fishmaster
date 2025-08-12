// 
// Decompiled by Procyon v0.6.0
// 

package dev.quiteboring.cobalt.ui.font;

import gg.essential.elementa.constraints.SuperConstraint;
import kotlin.ReplaceWith;
import kotlin.Deprecated;
import java.awt.Color;
import gg.essential.universal.UMatrixStack;
import org.lwjgl.nanovg.NVGColor;
import gg.essential.elementa.constraints.ConstraintType;
import gg.essential.elementa.constraints.resolution.ConstraintVisitor;
import java.nio.FloatBuffer;
import kotlin.jdk7.AutoCloseableKt;
import org.lwjgl.system.MemoryStack;
import java.net.URL;
import org.lwjgl.nanovg.NanoVG;
import java.nio.ByteBuffer;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;
import gg.essential.elementa.UIComponent;
import org.jetbrains.annotations.NotNull;
import kotlin.Metadata;
import gg.essential.elementa.font.FontProvider;

@Metadata(mv = { 2, 2, 0 }, k = 1, xi = 48, d1 = { """
                                                   \u0000R
                                                   \u0002\u0018\u0002
                                                   \u0002\u0018\u0002
                                                   \u0002\u0010\u000e
                                                   \u0002\b\u0003
                                                   \u0002\u0010	
                                                   \u0000
                                                   \u0002\u0010\u0002
                                                   \u0002\b\u0004
                                                   \u0002\u0010\u0007
                                                   \u0002\b	
                                                   \u0002\u0018\u0002
                                                   \u0000
                                                   \u0002\u0018\u0002
                                                   \u0002\b\u0006
                                                   \u0002\u0018\u0002
                                                   \u0000
                                                   \u0002\u0010\u000b
                                                   \u0002\b\u0005
                                                   \u0002\u0010\b
                                                   \u0002\b\u0010
                                                   \u0002\u0018\u0002
                                                   \u0002\b
                                                   \u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0003\u001a\u00020\u0002¢\u0006\u0004\b\u0004\u0010\u0005J\u0015\u0010	\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\u0006¢\u0006\u0004\b	\u0010
                                                   J\u000f\u0010\u000b\u001a\u00020\bH\u0002¢\u0006\u0004\b\u000b\u0010\fJ\u000f\u0010\u000e\u001a\u00020\rH\u0016¢\u0006\u0004\b\u000e\u0010\u000fJ\u000f\u0010\u0010\u001a\u00020\rH\u0016¢\u0006\u0004\b\u0010\u0010\u000fJ\u000f\u0010\u0011\u001a\u00020\rH\u0016¢\u0006\u0004\b\u0011\u0010\u000fJ\u001f\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u00022\u0006\u0010\u0013\u001a\u00020\rH\u0016¢\u0006\u0004\b\u0014\u0010\u0015J\u001f\u0010\u0016\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u00022\u0006\u0010\u0013\u001a\u00020\rH\u0016¢\u0006\u0004\b\u0016\u0010\u0015J\u001f\u0010\u001b\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u001a\u001a\u00020\u0019H\u0016¢\u0006\u0004\b\u001b\u0010\u001cJK\u0010%\u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\u00022\u0006\u0010\u001e\u001a\u00020\r2\u0006\u0010\u001f\u001a\u00020\r2\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010!\u001a\u00020 2\b\b\u0002\u0010#\u001a\u00020"2
                                                   \b\u0002\u0010$\u001a\u0004\u0018\u00010 ¢\u0006\u0004\b%\u0010&J\r\u0010'\u001a\u00020\b¢\u0006\u0004\b'\u0010\fJ\r\u0010)\u001a\u00020(¢\u0006\u0004\b)\u0010*J\r\u0010+\u001a\u00020"¢\u0006\u0004\b+\u0010,R\u0014\u0010\u0003\u001a\u00020\u00028\u0002X\u0082\u0004¢\u0006\u0006
                                                   \u0004\b\u0003\u0010-R"\u0010.\u001a\u00020\u00018\u0016@\u0016X\u0096\u000e¢\u0006\u0012
                                                   \u0004\b.\u0010/\u001a\u0004\b0\u00101"\u0004\b2\u00103R"\u00104\u001a\u00020"8\u0016@\u0016X\u0096\u000e¢\u0006\u0012
                                                   \u0004\b4\u00105\u001a\u0004\b6\u0010,"\u0004\b7\u00108R$\u0010:\u001a\u0004\u0018\u0001098\u0016@\u0016X\u0096\u000e¢\u0006\u0012
                                                   \u0004\b:\u0010;\u001a\u0004\b<\u0010="\u0004\b>\u0010?R\u0016\u0010@\u001a\u00020(8\u0002@\u0002X\u0082\u000e¢\u0006\u0006
                                                   \u0004\b@\u0010AR\u0016\u0010\u0007\u001a\u00020\u00068\u0002@\u0002X\u0082\u000e¢\u0006\u0006
                                                   \u0004\b\u0007\u0010B¨\u0006C""" }, d2 = { "Ldev/quiteboring/cobalt/ui/font/NvgFontProvider;", "Lgg/essential/elementa/font/FontProvider;", "", "fontPath", "<init>", "(Ljava/lang/String;)V", "", "nvgContext", "", "initialize", "(J)V", "loadFont", "()V", "", "getBaseLineHeight", "()F", "getBelowLineHeight", "getShadowHeight", "string", "pointSize", "getStringHeight", "(Ljava/lang/String;F)F", "getStringWidth", "Lgg/essential/elementa/constraints/resolution/ConstraintVisitor;", "visitor", "Lgg/essential/elementa/constraints/ConstraintType;", "type", "visitImpl", "(Lgg/essential/elementa/constraints/resolution/ConstraintVisitor;Lgg/essential/elementa/constraints/ConstraintType;)V", "text", "x", "y", "Lorg/lwjgl/nanovg/NVGColor;", "color", "", "shadow", "shadowColor", "drawString", "(Ljava/lang/String;FFFLorg/lwjgl/nanovg/NVGColor;ZLorg/lwjgl/nanovg/NVGColor;)V", "cleanup", "", "getFontHandle", "()I", "isInitialized", "()Z", "Ljava/lang/String;", "cachedValue", "Lgg/essential/elementa/font/FontProvider;", "getCachedValue", "()Lgg/essential/elementa/font/FontProvider;", "setCachedValue", "(Lgg/essential/elementa/font/FontProvider;)V", "recalculate", "Z", "getRecalculate", "setRecalculate", "(Z)V", "Lgg/essential/elementa/UIComponent;", "constrainTo", "Lgg/essential/elementa/UIComponent;", "getConstrainTo", "()Lgg/essential/elementa/UIComponent;", "setConstrainTo", "(Lgg/essential/elementa/UIComponent;)V", "fontHandle", "I", "J", "CobaltFabric" })
public final class NvgFontProvider implements FontProvider
{
    @NotNull
    private final String fontPath;
    @NotNull
    private FontProvider cachedValue;
    private boolean recalculate;
    @Nullable
    private UIComponent constrainTo;
    private int fontHandle;
    private long nvgContext;
    
    public NvgFontProvider(@NotNull final String fontPath) {
        Intrinsics.checkNotNullParameter((Object)fontPath, "fontPath");
        this.fontPath = fontPath;
        this.cachedValue = (FontProvider)this;
        this.fontHandle = -1;
    }
    
    @NotNull
    public FontProvider getCachedValue() {
        return this.cachedValue;
    }
    
    public void setCachedValue(@NotNull final FontProvider <set-?>) {
        Intrinsics.checkNotNullParameter((Object)<set-?>, "<set-?>");
        this.cachedValue = <set-?>;
    }
    
    public boolean getRecalculate() {
        return this.recalculate;
    }
    
    public void setRecalculate(final boolean <set-?>) {
        this.recalculate = <set-?>;
    }
    
    @Nullable
    public UIComponent getConstrainTo() {
        return this.constrainTo;
    }
    
    public void setConstrainTo(@Nullable final UIComponent <set-?>) {
        this.constrainTo = <set-?>;
    }
    
    public final void initialize(final long nvgContext) {
        this.nvgContext = nvgContext;
        this.loadFont();
    }
    
    private final void loadFont() {
        try {
            final URL resource = NvgFontProvider.class.getResource(this.fontPath);
            if (resource == null) {
                throw new RuntimeException("Font resource not found: " + this.fontPath);
            }
            final URL fontResource = resource;
            final byte[] fontData = TextStreamsKt.readBytes(fontResource);
            final ByteBuffer fontBuffer = ByteBuffer.allocateDirect(fontData.length);
            fontBuffer.put(fontData);
            fontBuffer.flip();
            this.fontHandle = NanoVG.nvgCreateFontMem(this.nvgContext, (CharSequence)"font", fontBuffer, false);
            if (this.fontHandle == -1) {
                throw new RuntimeException("Failed to load font: " + this.fontPath);
            }
        }
        catch (final Exception e) {
            throw new RuntimeException("Error loading font resource: " + this.fontPath, (Throwable)e);
        }
    }
    
    public float getBaseLineHeight() {
        if (this.fontHandle == -1 || this.nvgContext == 0L) {
            return 8.0f;
        }
        final AutoCloseable autoCloseable = (AutoCloseable)MemoryStack.stackPush();
        Throwable t = null;
        try {
            final MemoryStack stack = (MemoryStack)autoCloseable;
            final int n = 0;
            final FloatBuffer ascender = stack.mallocFloat(1);
            final FloatBuffer descender = stack.mallocFloat(1);
            final FloatBuffer lineHeight = stack.mallocFloat(1);
            NanoVG.nvgFontSize(this.nvgContext, 12.0f);
            NanoVG.nvgFontFaceId(this.nvgContext, this.fontHandle);
            NanoVG.nvgTextMetrics(this.nvgContext, ascender, descender, lineHeight);
            return ascender.get(0);
        }
        catch (final Throwable t2) {
            t = t2;
            throw t2;
        }
        finally {
            AutoCloseableKt.closeFinally(autoCloseable, t);
        }
    }
    
    public float getBelowLineHeight() {
        if (this.fontHandle == -1 || this.nvgContext == 0L) {
            return 2.0f;
        }
        final AutoCloseable autoCloseable = (AutoCloseable)MemoryStack.stackPush();
        Throwable t = null;
        try {
            final MemoryStack stack = (MemoryStack)autoCloseable;
            final int n = 0;
            final FloatBuffer ascender = stack.mallocFloat(1);
            final FloatBuffer descender = stack.mallocFloat(1);
            final FloatBuffer lineHeight = stack.mallocFloat(1);
            NanoVG.nvgFontSize(this.nvgContext, 12.0f);
            NanoVG.nvgFontFaceId(this.nvgContext, this.fontHandle);
            NanoVG.nvgTextMetrics(this.nvgContext, ascender, descender, lineHeight);
            return -descender.get(0);
        }
        catch (final Throwable t2) {
            t = t2;
            throw t2;
        }
        finally {
            AutoCloseableKt.closeFinally(autoCloseable, t);
        }
    }
    
    public float getShadowHeight() {
        return 1.0f;
    }
    
    public float getStringHeight(@NotNull final String string, final float pointSize) {
        Intrinsics.checkNotNullParameter((Object)string, "string");
        if (this.fontHandle == -1 || this.nvgContext == 0L) {
            return pointSize;
        }
        final AutoCloseable autoCloseable = (AutoCloseable)MemoryStack.stackPush();
        Throwable t = null;
        try {
            final MemoryStack stack = (MemoryStack)autoCloseable;
            final int n = 0;
            final FloatBuffer ascender = stack.mallocFloat(1);
            final FloatBuffer descender = stack.mallocFloat(1);
            final FloatBuffer lineHeight = stack.mallocFloat(1);
            NanoVG.nvgFontSize(this.nvgContext, pointSize);
            NanoVG.nvgFontFaceId(this.nvgContext, this.fontHandle);
            NanoVG.nvgTextMetrics(this.nvgContext, ascender, descender, lineHeight);
            return lineHeight.get(0);
        }
        catch (final Throwable t2) {
            t = t2;
            throw t2;
        }
        finally {
            AutoCloseableKt.closeFinally(autoCloseable, t);
        }
    }
    
    public float getStringWidth(@NotNull final String string, final float pointSize) {
        Intrinsics.checkNotNullParameter((Object)string, "string");
        if (this.fontHandle == -1 || this.nvgContext == 0L) {
            return 0.0f;
        }
        final AutoCloseable autoCloseable = (AutoCloseable)MemoryStack.stackPush();
        Throwable t = null;
        try {
            final MemoryStack stack = (MemoryStack)autoCloseable;
            final int n = 0;
            final FloatBuffer bounds = stack.mallocFloat(4);
            NanoVG.nvgFontSize(this.nvgContext, pointSize);
            NanoVG.nvgFontFaceId(this.nvgContext, this.fontHandle);
            return NanoVG.nvgTextBounds(this.nvgContext, 0.0f, 0.0f, (CharSequence)string, bounds);
        }
        catch (final Throwable t2) {
            t = t2;
            throw t2;
        }
        finally {
            AutoCloseableKt.closeFinally(autoCloseable, t);
        }
    }
    
    public void visitImpl(@NotNull final ConstraintVisitor visitor, @NotNull final ConstraintType type) {
        Intrinsics.checkNotNullParameter((Object)visitor, "visitor");
        Intrinsics.checkNotNullParameter((Object)type, "type");
    }
    
    public final void drawString(@NotNull final String text, final float x, final float y, final float pointSize, @NotNull final NVGColor color, final boolean shadow, @Nullable final NVGColor shadowColor) {
        Intrinsics.checkNotNullParameter((Object)text, "text");
        Intrinsics.checkNotNullParameter((Object)color, "color");
        if (this.fontHandle == -1 || this.nvgContext == 0L) {
            return;
        }
        NanoVG.nvgSave(this.nvgContext);
        NanoVG.nvgFontSize(this.nvgContext, pointSize);
        NanoVG.nvgFontFaceId(this.nvgContext, this.fontHandle);
        NanoVG.nvgTextAlign(this.nvgContext, 9);
        if (shadow && shadowColor != null) {
            NanoVG.nvgFillColor(this.nvgContext, shadowColor);
            NanoVG.nvgText(this.nvgContext, x + 1.0f, y + 1.0f, (CharSequence)text);
        }
        NanoVG.nvgFillColor(this.nvgContext, color);
        NanoVG.nvgText(this.nvgContext, x, y, (CharSequence)text);
        NanoVG.nvgRestore(this.nvgContext);
    }
    
    public final void cleanup() {
        if (this.fontHandle != -1 && this.nvgContext != 0L) {
            this.fontHandle = -1;
        }
    }
    
    public final int getFontHandle() {
        return this.fontHandle;
    }
    
    public final boolean isInitialized() {
        return this.fontHandle != -1 && this.nvgContext != 0L;
    }
    
    public void drawString(@NotNull final UMatrixStack matrixStack, @NotNull final String string, @NotNull final Color color, final float x, final float y, final float originalPointSize, final float scale, final boolean shadow, @Nullable final Color shadowColor) {
        super.drawString(matrixStack, string, color, x, y, originalPointSize, scale, shadow, shadowColor);
    }
    
    @Deprecated(message = """
                          For 1.17 this method requires you pass a UMatrixStack as the first argument.
                          
                          If you are currently extending this method, you should instead extend the method with the added argument.
                          Note however for this to be non-breaking, your parent class needs to transition before you do.
                          
                          If you are calling this method and you cannot guarantee that your target class has been fully updated (such as when
                          calling an open method on an open class), you should instead call the method with the "Compat" suffix, which will
                          call both methods, the new and the deprecated one.
                          If you are sure that your target class has been updated (such as when calling the super method), you should
                          (for super calls you must!) instead just call the method with the original name and added argument.""", replaceWith = @ReplaceWith(expression = "drawString(matrixStack, string, color, x, y, originalPointSize, scale, shadow, shadowColor)", imports = {}))
    @java.lang.Deprecated
    public void drawString(@NotNull final String string, @NotNull final Color color, final float x, final float y, final float originalPointSize, final float scale, final boolean shadow, @Nullable final Color shadowColor) {
        super.drawString(string, color, x, y, originalPointSize, scale, shadow, shadowColor);
    }
    
    @Deprecated(message = "See [ElementaVersion.V8].")
    @java.lang.Deprecated
    public void animationFrame() {
        super.animationFrame();
    }
    
    public void pauseIfSupported() {
        super.pauseIfSupported();
    }
    
    public void resumeIfSupported() {
        super.resumeIfSupported();
    }
    
    public void stopIfSupported() {
        super.stopIfSupported();
    }
    
    @NotNull
    public SuperConstraint<FontProvider> to(@NotNull final UIComponent component) {
        return (SuperConstraint<FontProvider>)super.to(component);
    }
    
    public void visit(@NotNull final ConstraintVisitor visitor, @NotNull final ConstraintType type, final boolean setNewConstraint) {
        super.visit(visitor, type, setNewConstraint);
    }
}
