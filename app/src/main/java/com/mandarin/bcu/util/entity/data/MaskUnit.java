package com.mandarin.bcu.util.entity.data;

import com.mandarin.bcu.util.unit.Form;

public interface MaskUnit extends MaskEntity {

	public int getBack();

	public int getFront();

	@Override
	public Form getPack();

	public int getPrice();

	public int getRespawn();

}
