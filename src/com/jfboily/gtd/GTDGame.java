package com.jfboily.gtd;

import com.jfboily.fxgea.Game;
import com.jfboily.fxgea.Screen;

public class GTDGame extends Game
{
	public GTDProfile profile = new GTDProfile();

	@Override
	public Screen getStartScreen() {
		// TODO Auto-generated method stub
		return new Splash(this);
	}

}
