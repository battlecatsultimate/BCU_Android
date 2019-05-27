package com.mandarin.bcu.util;

import com.mandarin.bcu.util.anim.AnimI;
import com.mandarin.bcu.util.anim.EAnimI;

public abstract class Animable<T extends AnimI> extends ImgCore {

	public T anim;

	public abstract EAnimI getEAnim(int t);

}
