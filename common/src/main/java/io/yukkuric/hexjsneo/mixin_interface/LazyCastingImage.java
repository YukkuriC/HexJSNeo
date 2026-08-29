package io.yukkuric.hexjsneo.mixin_interface;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import kotlin.Lazy;

import java.util.ArrayList;

public interface LazyCastingImage {
    Lazy<ArrayList<Iota>> getLazyStack(boolean refresh);
    Lazy<ArrayList<CastingImage.ParenthesizedIota>> getLazyParenList(boolean refresh);
}
