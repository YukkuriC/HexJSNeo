package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ActionRegistryEntry.class)
public interface MutableActionRegistryEntry {
    @Accessor("action")
    @Mutable
    void setAction(Action newAction);
    @Accessor("prototype")
    @Mutable
    void setPrototype(HexPattern newPattern);
}
