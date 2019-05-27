package com.mandarin.bcu.util.entity.data;

import java.util.Queue;

import com.mandarin.bcu.io.Reader;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.system.files.VFile;
import com.mandarin.bcu.util.unit.UnitStore;

public class PCoin extends Data {

	public static void read() {
		Queue<String> qs = VFile.readLine("./org/data/SkillAcquisition.csv");
		qs.poll();
		for (String str : qs)
			new PCoin(str.trim().split(","));
	}

	private final int id;
	private final DataUnit du;

	public final DataUnit full;
	public final int[] max;
	public final int[][] info = new int[5][12];

	private PCoin(String[] strs) {
		id = Reader.parseIntN(strs[0]);
		max = new int[6];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 12; j++)
				info[i][j] = Reader.parseIntN(strs[2 + i * 12 + j]);
			max[i + 1] = info[i][1];
			if (max[i + 1] == 0)
				max[i + 1] = 1;
		}
		du = (DataUnit) UnitStore.get(id, 2, false).du;
		du.pcoin = this;
		full = improve(max);
	}

	public DataUnit improve(int[] lvs) {
		DataUnit ans = du.clone();
		for (int i = 0; i < 5; i++) {
			int maxlv = info[i][1];
			int[] modifs = new int[4];
			if (maxlv > 1 && lvs[i + 1] > 0) {
				for (int j = 0; j < 4; j++) {
					int v0 = info[i][2 + j * 2];
					int v1 = info[i][3 + j * 2];
					modifs[j] = (v1 - v0) * (lvs[i + 1] - 1) / (maxlv - 1) + v0;
				}
			}
			if (maxlv == 0 && lvs[i + 1] > 0)
				for (int j = 0; j < 4; j++)
					modifs[j] = info[i][3 + j * 2];

			int[] type = PC_CORRES[info[i][0]];
			if (type[0] == PC_P) {
				int[] tar = ans.proc[type[1]];
				for (int j = 0; j < Math.min(tar.length, 4); j++)
					tar[j] += modifs[j];
				if (type[1] == P_STRONG)
					tar[0] = 100 - tar[0];
				if (type[1] == P_WEAK)
					tar[2] = 100 - tar[2];
			} else if (type[0] == PC_AB)
				ans.abi |= type[1];
			else if (type[0] == PC_BASE)
				if (type[1] == PC2_HP)
					ans.hp *= 1 + modifs[0] * 0.01;
				else if (type[1] == PC2_ATK) {
					double atk = 1 + modifs[0] * 0.01;
					ans.atk *= atk;
					ans.atk1 *= atk;
					ans.atk2 *= atk;
				} else if (type[1] == PC2_SPEED)
					ans.speed += modifs[0];
				else if (type[1] == PC2_CD)
					ans.respawn -= modifs[0];
				else if (type[1] == PC2_COST)
					ans.price -= modifs[0];
				else
					;
			else if (type[0] == PC_IMU && lvs[i + 1] > 0)
				ans.proc[type[1]][0] = 100;
			else if (type[0] == PC_TRAIT && lvs[i + 1] > 0)
				ans.type |= type[1];

		}
		return ans;
	}

}
