package com.mandarin.bcu.util.stage;

import java.util.Queue;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.BattleStatic;
import com.mandarin.bcu.util.system.files.VFile;

public class Limit extends Data implements BattleStatic {

	public static void read() {
		Queue<String> qs = VFile.readLine("./org/data/Stage_option.csv");
		qs.poll();
		for (String str : qs)
			new Limit(str.split(","));
	}

	public int star = -1, sid = -1;
	public int rare, num, line, min, max;
	public CharaGroup group;
	public LvRestrict lvr;

	public Limit() {
	}

	public Limit(MapColc mc, int ver, InStream is) {
		zread(mc, ver, is);
	}

	private Limit(String[] strs) {
		int mid = Integer.parseInt(strs[0]);
		MapColc.getMap(mid).lim.add(this);
		star = Integer.parseInt(strs[1]);
		sid = Integer.parseInt(strs[2]);
		rare = Integer.parseInt(strs[3]);
		num = Integer.parseInt(strs[4]);
		line = Integer.parseInt(strs[5]);
		min = Integer.parseInt(strs[6]);
		max = Integer.parseInt(strs[7]);
		group = CharaGroup.map.get(Integer.parseInt(strs[8]));
	}

	@Override
	public Limit clone() {
		Limit l = new Limit();
		l.star = star;
		l.sid = sid;
		l.rare = rare;
		l.num = num;
		l.line = line;
		l.min = min;
		l.max = max;
		l.group = group;
		l.lvr = lvr;
		return l;
	}

	public void combine(Limit l) {
		if (rare == 0)
			rare = l.rare;
		else if (l.rare != 0)
			rare &= l.rare;
		if (num * l.num > 0)
			num = Math.min(num, l.num);
		else
			num = Math.max(num, l.num);
		line |= l.line;
		min = Math.max(min, l.min);
		max = max > 0 && l.max > 0 ? Math.min(max, l.max) : (max + l.max);
		if (l.group != null)
			if (group != null)
				group = group.combine(l.group);
			else
				group = l.group;
		if (l.lvr != null)
			if (lvr != null)
				lvr.combine(l.lvr);
			else
				lvr = l.lvr;
	}

	public void write(OutStream os) {
		os.writeString("0.3.7");
		os.writeInt(rare);
		os.writeByte((byte) num);
		os.writeByte((byte) line);
		os.writeInt(min);
		os.writeInt(max);
		if (group == null)
			os.writeInt(-1);
		else
			os.writeInt(group.id);
		if (lvr == null)
			os.writeInt(-1);
		else
			os.writeInt(lvr.id);
	}

	private void zread(MapColc mc, int ver, InStream is) {
		if (ver >= 307)
			ver = getVer(is.nextString());
		if (ver >= 307)
			zread$000307(mc, is);
		else
			zread$000000(is);
	}

	private void zread$000000(InStream is) {
		rare = is.nextInt();
		num = is.nextByte();
		line = is.nextByte();
		min = is.nextInt();
		max = is.nextInt();
	}

	private void zread$000307(MapColc mc, InStream is) {
		zread$000000(is);
		int g = is.nextInt();
		if (g >= 0)
			group = mc.groups.get(g);

		int l = is.nextInt();
		if (l >= 0)
			lvr = mc.lvrs.get(l);
	}

}
