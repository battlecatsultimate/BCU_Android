package com.mandarin.bcu.util.system;

import com.mandarin.bcu.util.system.fake.FakeGraphics;
import com.mandarin.bcu.util.system.fake.FakeImage;

public class SymCoord {

	public final FakeGraphics g;
	public final double r, x, y;
	public final int type;

	public SymCoord(FakeGraphics fg, double R, double X, double Y, int t) {
		g = fg;
		r = R;
		x = X;
		y = Y;
		type = t;
	}

	public P draw(FakeImage... fis) {
		P size = new P(0, 0);
		for (FakeImage f : fis) {
			size.x += f.getWidth();
			size.y = Math.max(size.y, f.getHeight());
		}
		size.times(r);
		P pos = new P(x, y);
		if ((type & 1) > 0)
			pos.x -= size.x;
		if ((type & 2) > 0)
			pos.y -= size.y;
		for (FakeImage f : fis) {
			if (r == 1)
				g.drawImage(f, pos.x, pos.y);
			else
				g.drawImage(f, pos.x, pos.y, f.getWidth() * r, f.getHeight() * r);
			pos.x += f.getWidth() * r;
		}
		return size;
	}

}
