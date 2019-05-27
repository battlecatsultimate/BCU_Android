package com.mandarin.bcu.util.basis;

import java.util.ArrayList;
import java.util.List;

import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.BattleObj;
import com.mandarin.bcu.util.stage.EStage;
import com.mandarin.bcu.util.stage.Recd;
import com.mandarin.bcu.util.stage.Stage;

public class SBCtrl extends BattleField {

	public final List<Integer> action = new ArrayList<>();

	public final Recd re;

	public SBCtrl(Stage stage, int star, BasisLU bas, int[] ints, long seed) {
		super(new EStage(stage, star), bas, ints, seed);
		re = new Recd(bas, stage, star, ints, seed);
	}

	protected SBCtrl(StageBasis sb, Recd r) {
		super(sb);
		re = r.clone();
	}

	public Recd getData() {
		re.name = "";
		re.action = sb.rx.write();
		return re;
	}

	/** process the user action */
	@Override
	protected void actions() {
		if (sb.ebase.health <= 0)
			return;
	}

}

class Recorder extends BattleObj {

	private final List<Integer> recd = new ArrayList<>();

	private int num, rep;

	protected void add(int rec) {
		if (rec == num)
			rep++;
		else {
			if (rep > 0) {
				recd.add(num);
				recd.add(rep);
			}
			num = rec;
			rep = 1;
		}
	}

	protected OutStream write() {
		OutStream os = OutStream.getIns();
		if (rep > 0) {
			recd.add(num);
			recd.add(rep);
		}
		num = 0;
		rep = 0;
		os.writeInt(recd.size());
		for (int i : recd)
			os.writeInt(i);
		os.terminate();
		return os;
	}

}
