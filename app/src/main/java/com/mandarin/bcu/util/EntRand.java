package com.mandarin.bcu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mandarin.bcu.util.basis.StageBasis;

public abstract class EntRand<X> extends Data {

	public static final int T_NL = 0, T_LL = 1, T_GL = 2;

	public final List<EREnt<X>> list = new ArrayList<>();

	public final Map<StageBasis, Lock<X>> map = new HashMap<>();

	public int type = 0;

	@SuppressWarnings("unchecked")
	public void updateCopy(StageBasis sb, Object o) {
		if (o != null)
			map.put(sb, (Lock<X>) o);
	}

	protected EREnt<X> getSelection(StageBasis sb, Object obj) {
		if (type != T_NL) {
			Lock<X> l = map.get(sb);
			if (l == null)
				map.put(sb, l = type == T_LL ? new LockLL<X>() : new LockGL<X>());
			EREnt<X> ae = l.get(obj);
			if (ae == null)
				l.put(obj, ae = selector(sb, obj));
			return ae;
		}
		return selector(sb, obj);

	}

	private EREnt<X> selector(StageBasis sb, Object obj) {
		int tot = 0;
		for (EREnt<X> e : list)
			tot += e.share;
		if (tot > 0) {
			int r = (int) (sb.r.nextDouble() * tot);
			for (EREnt<X> ent : list) {
				r -= ent.share;
				if (r < 0)
					return ent;
			}
		}
		return null;
	}

}

interface Lock<X> {

	public EREnt<X> get(Object obj);

	public EREnt<X> put(Object obj, EREnt<X> ae);

}

class LockGL<X> extends BattleObj implements Lock<X> {

	private EREnt<X> ae;

	@Override
	public EREnt<X> get(Object obj) {
		return ae;
	}

	@Override
	public EREnt<X> put(Object obj, EREnt<X> e) {
		EREnt<X> pre = ae;
		ae = e;
		return pre;
	}

}

class LockLL<X> extends HashMap<Object, EREnt<X>> implements Lock<X> {

	private static final long serialVersionUID = 1L;

}