package com.mandarin.bcu.androidutil;

import common.pack.UserProfile;
import common.util.anim.AnimU;
import common.util.anim.EAnimD;
import common.util.pack.EffAnim;
import common.util.pack.NyCastle;
import common.util.pack.Soul;

public class StaticJava {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EAnimD<?> generateEAnimD(Object data, int index) {
        if(data instanceof EffAnim<?>) {
            ((EffAnim<?>) data).load();

            return new EAnimD((EffAnim<?>) data, ((EffAnim<?>) data).mamodel, ((EffAnim<?>) data).anims[index]);
        } else if(data instanceof Soul) {
            ((Soul) data).load();

            return new EAnimD((Soul) data, ((Soul) data).mamodel, ((Soul) data).anims[index]);
        } else if(data instanceof NyCastle) {
            ((NyCastle) data).load();

            if(index == 0) {
                return ((NyCastle) data).getEAnim(NyCastle.NyType.BASE);
            } else if(index == 1) {
                return ((NyCastle) data).getEAnim(NyCastle.NyType.ATK);
            } else if(index == 2) {
                return ((NyCastle) data).getEAnim(NyCastle.NyType.EXT);
            }
        }

        return UserProfile.getBCData().units.get(0).forms[0].getEAnim(AnimU.UType.WALK);
    }
}
