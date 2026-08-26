package io.yukkuric.hexjsneo.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import io.yukkuric.hexjsneo.mixin_interface.MutableActionRegistryEntry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ActionRegistryEntry.class)
public class MutableActionRegistryEntryImpl implements MutableActionRegistryEntry {
    @Mutable
    @Shadow
    @Final
    private Action action;
    @Mutable
    @Shadow
    @Final
    private HexPattern prototype;
    public void setAction(Action newAction) {
        action = newAction;
    }
    public void setPrototype(HexPattern newPattern) {
        prototype = newPattern;
    }
}
