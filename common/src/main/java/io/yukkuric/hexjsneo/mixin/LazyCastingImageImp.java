package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import io.yukkuric.hexjsneo.mixin_interface.LazyCastingImage;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.collections.CollectionsKt;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;

@Mixin(CastingImage.class)
public class LazyCastingImageImp implements LazyCastingImage {
    @Shadow
    @Final
    private TreeList<Iota> stack;
    @Shadow
    @Final
    private TreeList<CastingImage.ParenthesizedIota> parenthesized;

    private Lazy<ArrayList<Iota>> _lazyStack;
    private Lazy<ArrayList<CastingImage.ParenthesizedIota>> _lazyParenList;

    @Override
    public Lazy<ArrayList<Iota>> getLazyStack(boolean refresh) {
        if (refresh || _lazyStack == null)
            _lazyStack = LazyKt.lazy(() -> (ArrayList<Iota>) CollectionsKt.toMutableList(stack));
        return _lazyStack;
    }
    @Override
    public Lazy<ArrayList<CastingImage.ParenthesizedIota>> getLazyParenList(boolean refresh) {
        if (refresh || _lazyParenList == null)
            _lazyParenList = LazyKt.lazy(() -> (ArrayList<CastingImage.ParenthesizedIota>) CollectionsKt.toMutableList(parenthesized));
        return _lazyParenList;
    }
}
