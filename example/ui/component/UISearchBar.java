// 
// Decompiled by Procyon v0.6.0
// 

package dev.quiteboring.cobalt.ui.component;

import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.PropertyReference1;
import gg.essential.elementa.UIConstraints;
import gg.essential.elementa.dsl.UtilitiesKt;
import gg.essential.elementa.constraints.ColorConstraint;
import gg.essential.elementa.dsl.ComponentsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import java.awt.Color;
import gg.essential.elementa.components.input.UITextInput;
import gg.essential.elementa.UIComponent;
import org.jetbrains.annotations.NotNull;
import kotlin.properties.ReadWriteProperty;
import kotlin.reflect.KProperty;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.Metadata;
import gg.essential.elementa.components.UIRoundedRectangle;

@Metadata(mv = { 2, 2, 0 }, k = 1, xi = 48, d1 = { """
                                                   \u0000\u0014
                                                   \u0002\u0018\u0002
                                                   \u0002\u0018\u0002
                                                   \u0002\b\u0002
                                                   \u0002\u0018\u0002
                                                   \u0002\b\u0006\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003R\u001b\u0010	\u001a\u00020\u00048FX\u0086\u0084\u0002¢\u0006\f
                                                   \u0004\b\u0005\u0010\u0006\u001a\u0004\b\u0007\u0010\b¨\u0006
                                                   """ }, d2 = { "Ldev/quiteboring/cobalt/ui/component/UISearchBar;", "Lgg/essential/elementa/components/UIRoundedRectangle;", "<init>", "()V", "Lgg/essential/elementa/components/input/UITextInput;", "textInput$delegate", "Lkotlin/properties/ReadWriteProperty;", "getTextInput", "()Lgg/essential/elementa/components/input/UITextInput;", "textInput", "CobaltFabric" })
@SourceDebugExtension({ """
                        SMAP
                        UISearchBar.kt
                        Kotlin
                        *S Kotlin
                        *F
                        + 1 UISearchBar.kt
                        dev/quiteboring/cobalt/ui/component/UISearchBar
                        + 2 components.kt
                        gg/essential/elementa/dsl/ComponentsKt
                        *L
                        1#1,20:1
                        9#2,3:21
                        9#2,3:24
                        *S KotlinDebug
                        *F
                        + 1 UISearchBar.kt
                        dev/quiteboring/cobalt/ui/component/UISearchBar
                        *L
                        10#1:21,3
                        15#1:24,3
                        *E
                        """ })
public final class UISearchBar extends UIRoundedRectangle
{
    static final /* synthetic */ KProperty<Object>[] $$delegatedProperties;
    @NotNull
    private final ReadWriteProperty textInput$delegate;
    
    public UISearchBar() {
        super(15.0f);
        UIComponent $this$constrain$iv = (UIComponent)new UITextInput("Search...", false, (Color)null, (Color)null, false, (Color)null, (Color)null, (Color)null, 254, (DefaultConstructorMarker)null);
        int $i$f$constrain = 0;
        UIComponent $this$constrain_u24lambda_u240$iv = $this$constrain$iv;
        int n = 0;
        $this$constrain_u24lambda_u240$iv.getConstraints();
        final int n2 = 0;
        this.textInput$delegate = ComponentsKt.provideDelegate(ComponentsKt.childOf($this$constrain_u24lambda_u240$iv, (UIComponent)this), (Object)this, (KProperty)UISearchBar.$$delegatedProperties[0]);
        $this$constrain$iv = (UIComponent)this;
        $i$f$constrain = 0;
        $this$constrain_u24lambda_u240$iv = $this$constrain$iv;
        n = 0;
        final UIConstraints $this$_init__u24lambda_u241 = $this$constrain_u24lambda_u240$iv.getConstraints();
        final int n3 = 0;
        $this$_init__u24lambda_u241.setColor((ColorConstraint)UtilitiesKt.getConstraint(new Color(40, 40, 40)));
    }
    
    @NotNull
    public final UITextInput getTextInput() {
        return (UITextInput)this.textInput$delegate.getValue((Object)this, (KProperty)UISearchBar.$$delegatedProperties[0]);
    }
    
    static {
        $$delegatedProperties = new KProperty[] { (KProperty)Reflection.property1((PropertyReference1)new PropertyReference1Impl((Class)UISearchBar.class, "textInput", "getTextInput()Lgg/essential/elementa/components/input/UITextInput;", 0)) };
    }
}
