package io.yukkuric.hexjsneo.mixin_interface;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;

public interface MutableCastingImage {
    void setStack(TreeList<Iota> newStack);
}
