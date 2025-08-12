// 
// Decompiled by Procyon v0.6.0
// 

package dev.quiteboring.cobalt.ui;

import net.minecraft.class_437;
import net.minecraft.class_310;
import dev.quiteboring.cobalt.util.helper.TickScheduler;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.PropertyReference1;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.UIContainer;
import gg.essential.elementa.components.UIRoundedRectangle;
import org.jetbrains.annotations.Nullable;
import kotlin.properties.ReadWriteProperty;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.Metadata;
import gg.essential.elementa.WindowScreen;

@Metadata(mv = { 2, 2, 0 }, k = 1, xi = 48, d1 = { """
                                                   \u0000$
                                                   \u0002\u0018\u0002
                                                   \u0002\u0018\u0002
                                                   \u0002\b\u0002
                                                   \u0002\u0018\u0002
                                                   \u0002\b\u0005
                                                   \u0002\u0018\u0002
                                                   \u0002\b\u0004
                                                   \u0002\u0018\u0002
                                                   \u0002\b	\u0018\u0000 \u00172\u00020\u0001:\u0001\u0017B\u0007¢\u0006\u0004\b\u0002\u0010\u0003R\u001b\u0010	\u001a\u00020\u00048FX\u0086\u0084\u0002¢\u0006\f
                                                   \u0004\b\u0005\u0010\u0006\u001a\u0004\b\u0007\u0010\bR\u001b\u0010\u000e\u001a\u00020
                                                   8FX\u0086\u0084\u0002¢\u0006\f
                                                   \u0004\b\u000b\u0010\u0006\u001a\u0004\b\f\u0010\rR\u001b\u0010\u0013\u001a\u00020\u000f8FX\u0086\u0084\u0002¢\u0006\f
                                                   \u0004\b\u0010\u0010\u0006\u001a\u0004\b\u0011\u0010\u0012R\u001b\u0010\u0016\u001a\u00020
                                                   8FX\u0086\u0084\u0002¢\u0006\f
                                                   \u0004\b\u0014\u0010\u0006\u001a\u0004\b\u0015\u0010¨\u0006\u0018""" }, d2 = { "Ldev/quiteboring/cobalt/ui/ClickGUI;", "Lgg/essential/elementa/WindowScreen;", "<init>", "()V", "Lgg/essential/elementa/components/UIRoundedRectangle;", "container$delegate", "Lkotlin/properties/ReadWriteProperty;", "getContainer", "()Lgg/essential/elementa/components/UIRoundedRectangle;", "container", "Lgg/essential/elementa/components/UIContainer;", "topBar$delegate", "getTopBar", "()Lgg/essential/elementa/components/UIContainer;", "topBar", "Lgg/essential/elementa/components/UIText;", "logoText$delegate", "getLogoText", "()Lgg/essential/elementa/components/UIText;", "logoText", "mainContent$delegate", "getMainContent", "mainContent", "Companion", "CobaltFabric" })
@SourceDebugExtension({ """
                        SMAP
                        ClickGUI.kt
                        Kotlin
                        *S Kotlin
                        *F
                        + 1 ClickGUI.kt
                        dev/quiteboring/cobalt/ui/ClickGUI
                        + 2 components.kt
                        gg/essential/elementa/dsl/ComponentsKt
                        *L
                        1#1,61:1
                        9#2,3:62
                        9#2,3:65
                        9#2,3:68
                        9#2,3:71
                        *S KotlinDebug
                        *F
                        + 1 ClickGUI.kt
                        dev/quiteboring/cobalt/ui/ClickGUI
                        *L
                        22#1:62,3
                        30#1:65,3
                        36#1:68,3
                        41#1:71,3
                        *E
                        """ })
public final class ClickGUI extends WindowScreen
{
    @NotNull
    public static final Companion Companion;
    static final /* synthetic */ KProperty<Object>[] $$delegatedProperties;
    @NotNull
    private final ReadWriteProperty container$delegate;
    @NotNull
    private final ReadWriteProperty topBar$delegate;
    @NotNull
    private final ReadWriteProperty logoText$delegate;
    @NotNull
    private final ReadWriteProperty mainContent$delegate;
    @Nullable
    private static WindowScreen clickGUI;
    
    public ClickGUI() {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: getstatic       gg/essential/elementa/ElementaVersion.V2:Lgg/essential/elementa/ElementaVersion;
        //     4: iconst_0       
        //     5: iconst_0       
        //     6: iconst_1       
        //     7: getstatic       gg/essential/universal/GuiScale.Companion:Lgg/essential/universal/GuiScale$Companion;
        //    10: iconst_0       
        //    11: iconst_1       
        //    12: aconst_null    
        //    13: invokestatic    gg/essential/universal/GuiScale$Companion.scaleForScreenSize$default:(Lgg/essential/universal/GuiScale$Companion;IILjava/lang/Object;)Lgg/essential/universal/GuiScale;
        //    16: invokevirtual   gg/essential/universal/GuiScale.ordinal:()I
        //    19: invokespecial   gg/essential/elementa/WindowScreen.<init>:(Lgg/essential/elementa/ElementaVersion;ZZZI)V
        //    22: aload_0         /* this */
        //    23: new             Lgg/essential/elementa/components/UIRoundedRectangle;
        //    26: dup            
        //    27: ldc             10.0
        //    29: invokespecial   gg/essential/elementa/components/UIRoundedRectangle.<init>:(F)V
        //    32: checkcast       Lgg/essential/elementa/UIComponent;
        //    35: astore_1        /* $this$constrain$iv */
        //    36: iconst_0       
        //    37: istore_2        /* $i$f$constrain */
        //    38: aload_1         /* $this$constrain$iv */
        //    39: astore_3       
        //    40: aload_3        
        //    41: astore          $this$constrain_u24lambda_u240$iv
        //    43: iconst_0       
        //    44: istore          $i$a$-apply-ComponentsKt$constrain$1$iv
        //    46: aload           $this$constrain_u24lambda_u240$iv
        //    48: invokevirtual   gg/essential/elementa/UIComponent.getConstraints:()Lgg/essential/elementa/UIConstraints;
        //    51: astore          6
        //    53: astore          8
        //    55: iconst_0       
        //    56: istore          $i$a$-constrain-ClickGUI$container$2
        //    58: aload           $this$container_delegate_u24lambda_u240
        //    60: new             Lgg/essential/elementa/constraints/CenterConstraint;
        //    63: dup            
        //    64: invokespecial   gg/essential/elementa/constraints/CenterConstraint.<init>:()V
        //    67: checkcast       Lgg/essential/elementa/constraints/XConstraint;
        //    70: invokevirtual   gg/essential/elementa/UIConstraints.setX:(Lgg/essential/elementa/constraints/XConstraint;)V
        //    73: aload           $this$container_delegate_u24lambda_u240
        //    75: new             Lgg/essential/elementa/constraints/CenterConstraint;
        //    78: dup            
        //    79: invokespecial   gg/essential/elementa/constraints/CenterConstraint.<init>:()V
        //    82: checkcast       Lgg/essential/elementa/constraints/YConstraint;
        //    85: invokevirtual   gg/essential/elementa/UIConstraints.setY:(Lgg/essential/elementa/constraints/YConstraint;)V
        //    88: aload           $this$container_delegate_u24lambda_u240
        //    90: bipush          70
        //    92: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //    95: checkcast       Ljava/lang/Number;
        //    98: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPercent:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/RelativeConstraint;
        //   101: checkcast       Lgg/essential/elementa/constraints/WidthConstraint;
        //   104: invokevirtual   gg/essential/elementa/UIConstraints.setWidth:(Lgg/essential/elementa/constraints/WidthConstraint;)V
        //   107: aload           $this$container_delegate_u24lambda_u240
        //   109: bipush          70
        //   111: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   114: checkcast       Ljava/lang/Number;
        //   117: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPercent:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/RelativeConstraint;
        //   120: checkcast       Lgg/essential/elementa/constraints/HeightConstraint;
        //   123: invokevirtual   gg/essential/elementa/UIConstraints.setHeight:(Lgg/essential/elementa/constraints/HeightConstraint;)V
        //   126: aload           $this$container_delegate_u24lambda_u240
        //   128: new             Ljava/awt/Color;
        //   131: dup            
        //   132: bipush          24
        //   134: bipush          24
        //   136: bipush          24
        //   138: invokespecial   java/awt/Color.<init>:(III)V
        //   141: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getConstraint:(Ljava/awt/Color;)Lgg/essential/elementa/constraints/ConstantColorConstraint;
        //   144: checkcast       Lgg/essential/elementa/constraints/ColorConstraint;
        //   147: invokevirtual   gg/essential/elementa/UIConstraints.setColor:(Lgg/essential/elementa/constraints/ColorConstraint;)V
        //   150: nop            
        //   151: aload           8
        //   153: nop            
        //   154: nop            
        //   155: aload_3        
        //   156: nop            
        //   157: aload_0         /* this */
        //   158: invokevirtual   dev/quiteboring/cobalt/ui/ClickGUI.getWindow:()Lgg/essential/elementa/components/Window;
        //   161: checkcast       Lgg/essential/elementa/UIComponent;
        //   164: invokestatic    gg/essential/elementa/dsl/ComponentsKt.childOf:(Lgg/essential/elementa/UIComponent;Lgg/essential/elementa/UIComponent;)Lgg/essential/elementa/UIComponent;
        //   167: aload_0         /* this */
        //   168: getstatic       dev/quiteboring/cobalt/ui/ClickGUI.$$delegatedProperties:[Lkotlin/reflect/KProperty;
        //   171: iconst_0       
        //   172: aaload         
        //   173: invokestatic    gg/essential/elementa/dsl/ComponentsKt.provideDelegate:(Lgg/essential/elementa/UIComponent;Ljava/lang/Object;Lkotlin/reflect/KProperty;)Lkotlin/properties/ReadWriteProperty;
        //   176: putfield        dev/quiteboring/cobalt/ui/ClickGUI.container$delegate:Lkotlin/properties/ReadWriteProperty;
        //   179: aload_0         /* this */
        //   180: new             Lgg/essential/elementa/components/UIContainer;
        //   183: dup            
        //   184: invokespecial   gg/essential/elementa/components/UIContainer.<init>:()V
        //   187: checkcast       Lgg/essential/elementa/UIComponent;
        //   190: astore_1        /* $this$constrain$iv */
        //   191: iconst_0       
        //   192: istore_2        /* $i$f$constrain */
        //   193: aload_1         /* $this$constrain$iv */
        //   194: astore_3       
        //   195: aload_3        
        //   196: astore          $this$constrain_u24lambda_u240$iv
        //   198: iconst_0       
        //   199: istore          $i$a$-apply-ComponentsKt$constrain$1$iv
        //   201: aload           $this$constrain_u24lambda_u240$iv
        //   203: invokevirtual   gg/essential/elementa/UIComponent.getConstraints:()Lgg/essential/elementa/UIConstraints;
        //   206: astore          6
        //   208: astore          8
        //   210: iconst_0       
        //   211: istore          $i$a$-constrain-ClickGUI$topBar$2
        //   213: aload           $this$topBar_delegate_u24lambda_u241
        //   215: iconst_0       
        //   216: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   219: checkcast       Ljava/lang/Number;
        //   222: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPixels:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/PixelConstraint;
        //   225: checkcast       Lgg/essential/elementa/constraints/YConstraint;
        //   228: invokevirtual   gg/essential/elementa/UIConstraints.setY:(Lgg/essential/elementa/constraints/YConstraint;)V
        //   231: aload           $this$topBar_delegate_u24lambda_u241
        //   233: bipush          100
        //   235: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   238: checkcast       Ljava/lang/Number;
        //   241: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPercent:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/RelativeConstraint;
        //   244: checkcast       Lgg/essential/elementa/constraints/WidthConstraint;
        //   247: invokevirtual   gg/essential/elementa/UIConstraints.setWidth:(Lgg/essential/elementa/constraints/WidthConstraint;)V
        //   250: aload           $this$topBar_delegate_u24lambda_u241
        //   252: bipush          10
        //   254: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   257: checkcast       Ljava/lang/Number;
        //   260: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPercent:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/RelativeConstraint;
        //   263: checkcast       Lgg/essential/elementa/constraints/HeightConstraint;
        //   266: invokevirtual   gg/essential/elementa/UIConstraints.setHeight:(Lgg/essential/elementa/constraints/HeightConstraint;)V
        //   269: nop            
        //   270: aload           8
        //   272: nop            
        //   273: nop            
        //   274: aload_3        
        //   275: nop            
        //   276: aload_0         /* this */
        //   277: invokevirtual   dev/quiteboring/cobalt/ui/ClickGUI.getContainer:()Lgg/essential/elementa/components/UIRoundedRectangle;
        //   280: checkcast       Lgg/essential/elementa/UIComponent;
        //   283: invokestatic    gg/essential/elementa/dsl/ComponentsKt.childOf:(Lgg/essential/elementa/UIComponent;Lgg/essential/elementa/UIComponent;)Lgg/essential/elementa/UIComponent;
        //   286: aload_0         /* this */
        //   287: getstatic       dev/quiteboring/cobalt/ui/ClickGUI.$$delegatedProperties:[Lkotlin/reflect/KProperty;
        //   290: iconst_1       
        //   291: aaload         
        //   292: invokestatic    gg/essential/elementa/dsl/ComponentsKt.provideDelegate:(Lgg/essential/elementa/UIComponent;Ljava/lang/Object;Lkotlin/reflect/KProperty;)Lkotlin/properties/ReadWriteProperty;
        //   295: putfield        dev/quiteboring/cobalt/ui/ClickGUI.topBar$delegate:Lkotlin/properties/ReadWriteProperty;
        //   298: aload_0         /* this */
        //   299: new             Lgg/essential/elementa/components/UIText;
        //   302: dup            
        //   303: getstatic       net/minecraft/class_124.field_1067:Lnet/minecraft/class_124;
        //   306: invokedynamic   BootstrapMethod #0, makeConcatWithConstants:(Lnet/minecraft/class_124;)Ljava/lang/String;
        //   311: iconst_0       
        //   312: aconst_null    
        //   313: bipush          6
        //   315: aconst_null    
        //   316: invokespecial   gg/essential/elementa/components/UIText.<init>:(Ljava/lang/String;ZLjava/awt/Color;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
        //   319: checkcast       Lgg/essential/elementa/UIComponent;
        //   322: astore_1        /* $this$constrain$iv */
        //   323: iconst_0       
        //   324: istore_2        /* $i$f$constrain */
        //   325: aload_1         /* $this$constrain$iv */
        //   326: astore_3       
        //   327: aload_3        
        //   328: astore          $this$constrain_u24lambda_u240$iv
        //   330: iconst_0       
        //   331: istore          $i$a$-apply-ComponentsKt$constrain$1$iv
        //   333: aload           $this$constrain_u24lambda_u240$iv
        //   335: invokevirtual   gg/essential/elementa/UIComponent.getConstraints:()Lgg/essential/elementa/UIConstraints;
        //   338: astore          6
        //   340: astore          8
        //   342: iconst_0       
        //   343: istore          $i$a$-constrain-ClickGUI$logoText$2
        //   345: aload           $this$logoText_delegate_u24lambda_u242
        //   347: iconst_5       
        //   348: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   351: checkcast       Ljava/lang/Number;
        //   354: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPixels:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/PixelConstraint;
        //   357: checkcast       Lgg/essential/elementa/constraints/XConstraint;
        //   360: invokevirtual   gg/essential/elementa/UIConstraints.setX:(Lgg/essential/elementa/constraints/XConstraint;)V
        //   363: aload           $this$logoText_delegate_u24lambda_u242
        //   365: new             Lgg/essential/elementa/constraints/CenterConstraint;
        //   368: dup            
        //   369: invokespecial   gg/essential/elementa/constraints/CenterConstraint.<init>:()V
        //   372: checkcast       Lgg/essential/elementa/constraints/YConstraint;
        //   375: invokevirtual   gg/essential/elementa/UIConstraints.setY:(Lgg/essential/elementa/constraints/YConstraint;)V
        //   378: nop            
        //   379: aload           8
        //   381: nop            
        //   382: nop            
        //   383: aload_3        
        //   384: nop            
        //   385: aload_0         /* this */
        //   386: invokevirtual   dev/quiteboring/cobalt/ui/ClickGUI.getTopBar:()Lgg/essential/elementa/components/UIContainer;
        //   389: checkcast       Lgg/essential/elementa/UIComponent;
        //   392: invokestatic    gg/essential/elementa/dsl/ComponentsKt.childOf:(Lgg/essential/elementa/UIComponent;Lgg/essential/elementa/UIComponent;)Lgg/essential/elementa/UIComponent;
        //   395: aload_0         /* this */
        //   396: getstatic       dev/quiteboring/cobalt/ui/ClickGUI.$$delegatedProperties:[Lkotlin/reflect/KProperty;
        //   399: iconst_2       
        //   400: aaload         
        //   401: invokestatic    gg/essential/elementa/dsl/ComponentsKt.provideDelegate:(Lgg/essential/elementa/UIComponent;Ljava/lang/Object;Lkotlin/reflect/KProperty;)Lkotlin/properties/ReadWriteProperty;
        //   404: putfield        dev/quiteboring/cobalt/ui/ClickGUI.logoText$delegate:Lkotlin/properties/ReadWriteProperty;
        //   407: aload_0         /* this */
        //   408: new             Lgg/essential/elementa/components/UIContainer;
        //   411: dup            
        //   412: invokespecial   gg/essential/elementa/components/UIContainer.<init>:()V
        //   415: checkcast       Lgg/essential/elementa/UIComponent;
        //   418: astore_1        /* $this$constrain$iv */
        //   419: iconst_0       
        //   420: istore_2        /* $i$f$constrain */
        //   421: aload_1         /* $this$constrain$iv */
        //   422: astore_3       
        //   423: aload_3        
        //   424: astore          $this$constrain_u24lambda_u240$iv
        //   426: iconst_0       
        //   427: istore          $i$a$-apply-ComponentsKt$constrain$1$iv
        //   429: aload           $this$constrain_u24lambda_u240$iv
        //   431: invokevirtual   gg/essential/elementa/UIComponent.getConstraints:()Lgg/essential/elementa/UIConstraints;
        //   434: astore          6
        //   436: astore          8
        //   438: iconst_0       
        //   439: istore          $i$a$-constrain-ClickGUI$mainContent$2
        //   441: aload           $this$mainContent_delegate_u24lambda_u243
        //   443: bipush          11
        //   445: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   448: checkcast       Ljava/lang/Number;
        //   451: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPercent:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/RelativeConstraint;
        //   454: checkcast       Lgg/essential/elementa/constraints/YConstraint;
        //   457: invokevirtual   gg/essential/elementa/UIConstraints.setY:(Lgg/essential/elementa/constraints/YConstraint;)V
        //   460: aload           $this$mainContent_delegate_u24lambda_u243
        //   462: bipush          100
        //   464: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   467: checkcast       Ljava/lang/Number;
        //   470: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPercent:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/RelativeConstraint;
        //   473: checkcast       Lgg/essential/elementa/constraints/WidthConstraint;
        //   476: invokevirtual   gg/essential/elementa/UIConstraints.setWidth:(Lgg/essential/elementa/constraints/WidthConstraint;)V
        //   479: aload           $this$mainContent_delegate_u24lambda_u243
        //   481: bipush          85
        //   483: invokestatic    java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        //   486: checkcast       Ljava/lang/Number;
        //   489: invokestatic    gg/essential/elementa/dsl/UtilitiesKt.getPercent:(Ljava/lang/Number;)Lgg/essential/elementa/constraints/RelativeConstraint;
        //   492: checkcast       Lgg/essential/elementa/constraints/HeightConstraint;
        //   495: invokevirtual   gg/essential/elementa/UIConstraints.setHeight:(Lgg/essential/elementa/constraints/HeightConstraint;)V
        //   498: nop            
        //   499: aload           8
        //   501: nop            
        //   502: nop            
        //   503: aload_3        
        //   504: nop            
        //   505: aload_0         /* this */
        //   506: invokevirtual   dev/quiteboring/cobalt/ui/ClickGUI.getContainer:()Lgg/essential/elementa/components/UIRoundedRectangle;
        //   509: checkcast       Lgg/essential/elementa/UIComponent;
        //   512: invokestatic    gg/essential/elementa/dsl/ComponentsKt.childOf:(Lgg/essential/elementa/UIComponent;Lgg/essential/elementa/UIComponent;)Lgg/essential/elementa/UIComponent;
        //   515: aload_0         /* this */
        //   516: getstatic       dev/quiteboring/cobalt/ui/ClickGUI.$$delegatedProperties:[Lkotlin/reflect/KProperty;
        //   519: iconst_3       
        //   520: aaload         
        //   521: invokestatic    gg/essential/elementa/dsl/ComponentsKt.provideDelegate:(Lgg/essential/elementa/UIComponent;Ljava/lang/Object;Lkotlin/reflect/KProperty;)Lkotlin/properties/ReadWriteProperty;
        //   524: putfield        dev/quiteboring/cobalt/ui/ClickGUI.mainContent$delegate:Lkotlin/properties/ReadWriteProperty;
        //   527: return         
        // 
        // The error that occurred was:
        // 
        // java.lang.NullPointerException: Cannot read field "references" because "newVariable" is null
        //     at com.strobel.decompiler.ast.AstBuilder.convertLocalVariables(AstBuilder.java:2945)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2501)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:203)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:93)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:868)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createConstructor(AstBuilder.java:799)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:635)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:605)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:195)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:162)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:137)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at com.strobel.decompiler.DecompilerDriver.decompileType(DecompilerDriver.java:333)
        //     at com.strobel.decompiler.DecompilerDriver.decompileJar(DecompilerDriver.java:254)
        //     at com.strobel.decompiler.DecompilerDriver.main(DecompilerDriver.java:144)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    @NotNull
    public final UIRoundedRectangle getContainer() {
        return (UIRoundedRectangle)this.container$delegate.getValue((Object)this, (KProperty)ClickGUI.$$delegatedProperties[0]);
    }
    
    @NotNull
    public final UIContainer getTopBar() {
        return (UIContainer)this.topBar$delegate.getValue((Object)this, (KProperty)ClickGUI.$$delegatedProperties[1]);
    }
    
    @NotNull
    public final UIText getLogoText() {
        return (UIText)this.logoText$delegate.getValue((Object)this, (KProperty)ClickGUI.$$delegatedProperties[2]);
    }
    
    @NotNull
    public final UIContainer getMainContent() {
        return (UIContainer)this.mainContent$delegate.getValue((Object)this, (KProperty)ClickGUI.$$delegatedProperties[3]);
    }
    
    public static final /* synthetic */ WindowScreen access$getClickGUI$cp() {
        return ClickGUI.clickGUI;
    }
    
    public static final /* synthetic */ void access$setClickGUI$cp(final WindowScreen <set-?>) {
        ClickGUI.clickGUI = <set-?>;
    }
    
    static {
        $$delegatedProperties = new KProperty[] { (KProperty)Reflection.property1((PropertyReference1)new PropertyReference1Impl((Class)ClickGUI.class, "container", "getContainer()Lgg/essential/elementa/components/UIRoundedRectangle;", 0)), (KProperty)Reflection.property1((PropertyReference1)new PropertyReference1Impl((Class)ClickGUI.class, "topBar", "getTopBar()Lgg/essential/elementa/components/UIContainer;", 0)), (KProperty)Reflection.property1((PropertyReference1)new PropertyReference1Impl((Class)ClickGUI.class, "logoText", "getLogoText()Lgg/essential/elementa/components/UIText;", 0)), (KProperty)Reflection.property1((PropertyReference1)new PropertyReference1Impl((Class)ClickGUI.class, "mainContent", "getMainContent()Lgg/essential/elementa/components/UIContainer;", 0)) };
        Companion = new Companion(null);
    }
    
    @Metadata(mv = { 2, 2, 0 }, k = 1, xi = 48, d1 = { """
                                                       \u0000\u001a
                                                       \u0002\u0018\u0002
                                                       \u0002\u0010\u0000
                                                       \u0002\b\u0002
                                                       \u0002\u0010\u0002
                                                       \u0000
                                                       \u0002\u0018\u0002
                                                       \u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B	\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\r\u0010\u0005\u001a\u00020\u0004¢\u0006\u0004\b\u0005\u0010\u0003R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00068\u0002@\u0002X\u0082\u000e¢\u0006\u0006
                                                       \u0004\b\u0007\u0010\b¨\u0006	""" }, d2 = { "Ldev/quiteboring/cobalt/ui/ClickGUI$Companion;", "", "<init>", "()V", "", "open", "Lgg/essential/elementa/WindowScreen;", "clickGUI", "Lgg/essential/elementa/WindowScreen;", "CobaltFabric" })
    public static final class Companion
    {
        private Companion() {
        }
        
        public final void open() {
            if (ClickGUI.access$getClickGUI$cp() == null) {
                ClickGUI.access$setClickGUI$cp(new ClickGUI());
            }
            TickScheduler.INSTANCE.schedule(1L, Companion::open$lambda$0);
        }
        
        private static final void open$lambda$0() {
            class_310.method_1551().method_1507((class_437)ClickGUI.access$getClickGUI$cp());
        }
    }
}
