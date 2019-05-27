package com.mandarin.bcu.util.entity.data;

import java.util.Set;
import java.util.TreeSet;

import com.mandarin.bcu.util.basis.Basis;
import com.mandarin.bcu.util.unit.AbEnemy;
import com.mandarin.bcu.util.unit.Enemy;

public interface MaskEnemy extends MaskEntity {

	public double getDrop();

	@Override
	public Enemy getPack();

	public int getStar();

	public default Set<AbEnemy> getSummon() {
		return new TreeSet<AbEnemy>();
	}

	public double multi(Basis b);

}
