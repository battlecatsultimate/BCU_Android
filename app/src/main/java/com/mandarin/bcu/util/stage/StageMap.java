package com.mandarin.bcu.util.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.mandarin.bcu.io.Reader;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.system.BasedCopable;
import com.mandarin.bcu.util.system.files.AssetData;

public class StageMap extends Data implements BasedCopable<StageMap, MapColc> {

	public final List<Stage> list = new ArrayList<>();
	public final List<Limit> lim = new ArrayList<>();

	public String name = "";
	public int id;
	public int price = 1, retyp, pllim, set;
	public final MapColc mc;
	public int cast = -1;
	public int[] stars = new int[] { 100 };

	private Queue<String> qs;

	public StageMap(MapColc map) {
		mc = map;
		name = "new stage map";
	}

	protected StageMap(MapColc map, int ID, AssetData m) {
		mc = map;
		id = ID;
		qs = m.readLine();
		qs.poll();
		qs.poll();
	}

	protected StageMap(MapColc map, int ID, AssetData m, int cas) {
		this(map, ID, m);
		cast = cas;
	}

	public void add(Stage s) {
		if (s == null)
			return;
		list.add(s);
		if (qs != null) {
			int[] ints = Reader.parseIntsN(qs.poll());
			if (ints.length <= 4)
				return;
			s.mus0 = ints[2];
			s.mush = ints[3];
			s.mus1 = ints[4];
		}
	}

	@Override
	public StageMap copy(MapColc mc) {
		StageMap sm = new StageMap(mc);
		sm.name = name;
		if (name.length() == 0)
			sm.name = toString();
		sm.stars = stars.clone();
		for (Stage st : list)
			sm.add(st.copy(sm));
		return sm;
	}

	@Override
	public String toString() {
		String desp = "";// MultiLangCont.get(this);
		if (desp != null && desp.length() > 0)
			return desp;
		if (name.length() == 0)
			return mc + " - " + id + " (" + list.size() + ")";
		return name;
	}

}
