package dev.hail.wulfrum.client;

import dev.hail.wulfrum.WulfrumMod;
import org.confluence.mod.client.connected.AllCTTypes;
import org.confluence.mod.client.connected.CTSpriteShiftEntry;
import org.confluence.mod.client.connected.CTSpriteShifter;

public class CWAllSpriteShifts {
    public static final CTSpriteShiftEntry WULFRUM_PLATING = omni("wulfrum_plating");
    private static CTSpriteShiftEntry omni(String name) {
        return CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL, WulfrumMod.asResource("block/" + name), WulfrumMod.asResource("block/" + name + "_connected"));
    }
}
