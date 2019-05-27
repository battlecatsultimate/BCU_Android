package com.mandarin.bcu.util.anim;

import com.mandarin.bcu.util.Animable;
import com.mandarin.bcu.util.BattleStatic;
import com.mandarin.bcu.util.system.fake.FakeImage;

public abstract class AnimI extends Animable<AnimI> implements BattleStatic {

	public abstract void check();

	public abstract void load();

	public abstract String[] names();

	public abstract FakeImage parts(int img);

}
