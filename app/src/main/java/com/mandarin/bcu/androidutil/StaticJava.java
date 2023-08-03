package com.mandarin.bcu.androidutil;

import common.pack.Identifier;
import common.pack.UserProfile;
import common.util.anim.AnimU;
import common.util.anim.EAnimD;
import common.util.pack.DemonSoul;
import common.util.pack.EffAnim;
import common.util.pack.NyCastle;
import common.util.pack.Soul;
import common.util.unit.Enemy;
import common.util.unit.Unit;

public class StaticJava {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EAnimD<?> generateEAnimD(Object data, int dataId) {
        if(data instanceof EffAnim<?>) {
            ((EffAnim<?>) data).load();

            return new EAnimD((EffAnim<?>) data, ((EffAnim<?>) data).mamodel, ((EffAnim<?>) data).anims[dataId], ((EffAnim<?>) data).types[dataId]);
        } else if(data instanceof Soul) {
            ((Soul) data).anim.load();

            return new EAnimD(((Soul) data).anim, ((Soul) data).anim.mamodel, ((Soul) data).anim.anims[dataId], ((Soul) data).anim.types[dataId]);
        } else if(data instanceof NyCastle) {
            ((NyCastle) data).load();

            if(dataId == 0) {
                return ((NyCastle) data).getEAnim(NyCastle.NyType.BASE);
            } else if(dataId == 1) {
                return ((NyCastle) data).getEAnim(NyCastle.NyType.ATK);
            } else if(dataId == 2) {
                return ((NyCastle) data).getEAnim(NyCastle.NyType.EXT);
            }
        } else if(data instanceof DemonSoul) {
            ((DemonSoul) data).anim.load();

            return new EAnimD(((DemonSoul) data).anim, ((DemonSoul) data).anim.mamodel, ((DemonSoul) data).anim.anims[dataId], ((DemonSoul) data).anim.types[dataId]);
        } else if(data instanceof Identifier<?>) {
            Object entity = ((Identifier<?>) data).get();

            if (entity != null) {
                if (entity instanceof Unit) {
                    return ((Unit) entity).forms[dataId].getEAnim(AnimU.UType.WALK);
                } else if (entity instanceof Enemy) {
                    return ((Enemy) entity).getEAnim(AnimU.UType.WALK);
                }
            }
        }

        return UserProfile.getBCData().units.get(0).forms[0].getEAnim(AnimU.UType.WALK);
    }
}
