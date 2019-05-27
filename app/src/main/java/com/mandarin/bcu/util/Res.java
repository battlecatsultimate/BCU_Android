package com.mandarin.bcu.util;

import com.mandarin.bcu.util.anim.ImgCut;
import com.mandarin.bcu.util.entity.AbEntity;
import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.SymCoord;
import com.mandarin.bcu.util.system.VImg;
import com.mandarin.bcu.util.system.fake.FakeImage;
import com.mandarin.bcu.util.system.fake.ImageBuilder;
import com.mandarin.bcu.util.unit.Form;

public class Res extends ImgCore {

	public static VImg[] slot = new VImg[3];
	public static VImg[][] ico = new VImg[2][];
	public static VImg[][] num = new VImg[9][11];
	public static VImg[][] battle = new VImg[3][];

	private static VImg[][] icon = new VImg[4][];

	public static P getBase(AbEntity ae, SymCoord coor) {
		long h = ae.health;
		if (h < 0)
			h = 0;
		int[] val0 = getLab(h);
		int[] val1 = getLab(ae.maxH);
		FakeImage[] input = new FakeImage[val0.length + val1.length + 1];
		for (int i = 0; i < val0.length; i++)
			input[i] = num[5][val0[i]].getImg();
		input[val0.length] = num[5][10].getImg();
		for (int i = 0; i < val1.length; i++)
			input[val0.length + i + 1] = num[5][val1[i]].getImg();
		return coor.draw(input);
	}

	public static P getCost(int cost, boolean enable, SymCoord coor) {
		if (cost == -1)
			return coor.draw(battle[0][3].getImg());
		int[] val = getLab(cost);
		FakeImage[] input = new FakeImage[val.length];
		for (int i = 0; i < val.length; i++)
			input[i] = num[enable ? 3 : 4][val[i]].getImg();
		return coor.draw(input);
	}

	public static FakeImage getIcon(int type, int id) {
		type += id / 100;
		id %= 100;
		if (icon[type][id] == null)
			return null;
		return icon[type][id].getImg();
	}

	public static P getMoney(int mon, int max, SymCoord coor) {
		int[] val0 = getLab(mon);
		int[] val1 = getLab(max);
		FakeImage[] input = new FakeImage[val0.length + val1.length + 1];
		for (int i = 0; i < val0.length; i++)
			input[i] = num[0][val0[i]].getImg();
		input[val0.length] = num[0][10].getImg();
		for (int i = 0; i < val1.length; i++)
			input[val0.length + i + 1] = num[0][val1[i]].getImg();
		return coor.draw(input);
	}

	public static P getWorkerLv(int lv, boolean enable, SymCoord coor) {
		return coor.draw(num[enable ? 1 : 2][10].getImg(), num[enable ? 1 : 2][lv].getImg());
	}

	public static void readData() {
		Form.unicut = ImgCut.newIns("./org/data/uni.imgcut");
		Form.udicut = ImgCut.newIns("./org/data/udi.imgcut");
		VImg uni = new VImg("./org/page/uni.png");
		uni.setCut(Form.unicut);
		slot[0] = uni;

		ico[0] = new VImg[6];
		ico[1] = new VImg[4];
		ico[0][0] = new VImg("./org/page/foreground.png");
		ico[0][1] = new VImg("./org/page/starFG.png");
		ico[0][2] = new VImg("./org/page/EFBG.png");
		ico[0][3] = new VImg("./org/page/TFBG.png");
		ico[0][4] = new VImg("./org/page/glow.png");
		ico[0][5] = new VImg("./org/page/EFFG.png");
		ico[1][0] = new VImg("./org/page/uni_f.png");
		ico[1][1] = new VImg("./org/page/uni_c.png");
		ico[1][2] = new VImg("./org/page/uni_s.png");
		ico[1][3] = new VImg("./org/page/uni_box.png");
		for (VImg vs : ico[1])
			vs.setCut(Form.unicut);

		ImgCut ic029 = ImgCut.newIns("./org/page/img029.imgcut");
		VImg img029 = new VImg("./org/page/img029.png");
		FakeImage[] parts = ic029.cut(img029.getImg());
		slot[1] = new VImg(parts[9]);
		slot[2] = new VImg(parts[10]);
		readAbiIcon();
		readBattle();
	}

	private static int[] getLab(long cost) {
		if (cost < 0)
			cost = 0;
		int len = ("" + cost).length();
		int[] input = new int[len];
		for (int i = 0; i < len; i++) {
			input[len - i - 1] = (int) (cost % 10);
			cost /= 10;
		}
		return input;
	}

	private static void readAbiIcon() {
		ImageBuilder.icon = true;
		ImgCut ic015 = ImgCut.newIns("./org/page/img015.imgcut");
		VImg img015 = new VImg("./org/page/img015.png");
		FakeImage[] parts = ic015.cut(img015.getImg());
		icon[0] = new VImg[ABI_TOT];
		icon[1] = new VImg[PROC_TOT];
		icon[2] = new VImg[ATK_TOT];
		icon[3] = new VImg[TRAIT_TOT];
		icon[3][TRAIT_RED] = new VImg(parts[77]);
		icon[3][TRAIT_FLOAT] = new VImg(parts[78]);
		icon[3][TRAIT_BLACK] = new VImg(parts[79]);
		icon[3][TRAIT_METAL] = new VImg(parts[80]);
		icon[3][TRAIT_ANGEL] = new VImg(parts[81]);
		icon[3][TRAIT_ALIEN] = new VImg(parts[82]);
		icon[3][TRAIT_ZOMBIE] = new VImg(parts[83]);
		icon[3][TRAIT_RELIC] = new VImg(parts[84]);
		icon[0][ABI_EKILL] = new VImg(parts[110]);
		icon[2][ATK_OMNI] = new VImg(parts[112]);
		icon[1][P_IMUCURSE] = new VImg(parts[116]);
		icon[1][P_WEAK] = new VImg(parts[195]);
		icon[1][P_STRONG] = new VImg(parts[196]);
		icon[1][P_STOP] = new VImg(parts[197]);
		icon[1][P_SLOW] = new VImg(parts[198]);
		icon[1][P_LETHAL] = new VImg(parts[199]);
		icon[0][ABI_BASE] = new VImg(parts[200]);
		icon[1][P_CRIT] = new VImg(parts[201]);
		icon[0][ABI_ONLY] = new VImg(parts[202]);
		icon[0][ABI_GOOD] = new VImg(parts[203]);
		icon[0][ABI_RESIST] = new VImg(parts[204]);
		icon[0][ABI_EARN] = new VImg(parts[205]);
		icon[0][ABI_MASSIVE] = new VImg(parts[206]);
		icon[1][P_KB] = new VImg(parts[207]);
		icon[1][P_WAVE] = new VImg(parts[208]);
		icon[0][ABI_METALIC] = new VImg(parts[209]);
		icon[1][P_IMUWAVE] = new VImg(parts[210]);
		icon[2][ATK_AREA] = new VImg(parts[211]);
		icon[2][ATK_LD] = new VImg(parts[212]);
		icon[1][P_IMUWEAK] = new VImg(parts[213]);
		icon[1][P_IMUSTOP] = new VImg(parts[214]);
		icon[1][P_IMUSLOW] = new VImg(parts[215]);
		icon[1][P_IMUKB] = new VImg(parts[216]);
		icon[2][ATK_SINGLE] = new VImg(parts[217]);
		icon[0][ABI_WAVES] = new VImg(parts[218]);
		icon[0][ABI_WKILL] = new VImg(parts[258]);
		icon[0][ABI_RESISTS] = new VImg(parts[122]);
		icon[0][ABI_MASSIVES] = new VImg(parts[114]);

		icon[0][ABI_ZKILL] = new VImg(parts[260]);
		icon[1][P_IMUWARP] = new VImg(parts[262]);
		icon[1][P_BREAK] = new VImg(parts[264]);
		icon[1][P_WARP] = new VImg(parts[266]);
		icon[0][ABI_THEMEI] = new VImg("./org/page/icons/ThemeX.png");
		icon[0][ABI_TIMEI] = new VImg("./org/page/icons/TimeX.png");
		icon[0][ABI_IMUSW] = new VImg("./org/page/icons/BossWaveX.png");
		icon[0][ABI_SNIPERI] = new VImg("./org/page/icons/SnipeX.png");
		icon[0][ABI_POII] = new VImg("./org/page/icons/PoisonX.png");
		icon[0][ABI_SEALI] = new VImg("./org/page/icons/SealX.png");
		icon[0][ABI_GHOST] = new VImg("./org/page/icons/Ghost.png");
		icon[1][P_THEME] = new VImg("./org/page/icons/Theme.png");
		icon[1][P_TIME] = new VImg("./org/page/icons/Time.png");
		icon[1][P_BOSS] = new VImg("./org/page/icons/BossWave.png");
		icon[1][P_SNIPER] = new VImg("./org/page/icons/Snipe.png");
		icon[1][P_POISON] = new VImg("./org/page/icons/Poison.png");
		icon[1][P_SEAL] = new VImg("./org/page/icons/Seal.png");
		icon[1][P_MOVEWAVE] = new VImg("./org/page/icons/Moving.png");
		icon[1][P_SUMMON] = new VImg("./org/page/icons/Summon.png");
		icon[0][ABI_MOVEI] = new VImg("./org/page/icons/MovingX.png");
		icon[1][P_CURSE] = new VImg("./org/page/icons/Curse.png");
		icon[0][ABI_GLASS] = new VImg("./org/page/icons/Suicide.png");
		icon[1][P_BURROW] = new VImg("./org/page/icons/Burrow.png");
		icon[1][P_REVIVE] = new VImg("./org/page/icons/Revive.png");
		ImageBuilder.icon = false;
	}

	private static void readBattle() {
		battle[0] = new VImg[4];
		battle[1] = new VImg[12];
		battle[2] = new VImg[5];
		ImgCut ic001 = ImgCut.newIns("./org/page/img001.imgcut");
		VImg img001 = new VImg("./org/page/img001.png");
		FakeImage[] parts = ic001.cut(img001.getImg());
		int[] vals = new int[] { 5, 19, 30, 40, 51, 62, 73, 88, 115 };
		int[] adds = new int[] { 1, 2, 2, 0, 0, 1, 1, 1, 0 };
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 10; j++)
				num[i][j] = new VImg(parts[vals[i] - 5 + j]);
			if (adds[i] == 1)
				num[i][10] = new VImg(parts[vals[i] + 5]);
			if (adds[i] == 2)
				num[i][10] = new VImg(parts[vals[i] - 6]);
		}
		battle[0][3] = new VImg(parts[81]);

		ImgCut ic002 = ImgCut.newIns("./org/page/img002.imgcut");
		VImg img002 = new VImg("./org/page/img002.png");

		parts = ic002.cut(img002.getImg());
		battle[0][0] = new VImg(parts[5]);
		battle[0][1] = new VImg(parts[24]);
		battle[0][2] = new VImg(parts[6]);
		battle[1][0] = new VImg(parts[8]);
		battle[1][1] = new VImg(parts[7]);
		for (int i = 0; i < 10; i++)
			battle[1][2 + i] = new VImg(parts[11 + i]);
		battle[2][0] = new VImg(parts[27]);
		battle[2][1] = new VImg(parts[29]);
		battle[2][2] = new VImg(parts[32]);
		battle[2][3] = new VImg(parts[33]);
		battle[2][4] = new VImg(parts[38]);
		// money, lv, lv dark,cost,cost dark,hp, money light,time,point
	}

}
