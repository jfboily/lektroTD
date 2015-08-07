package com.jfboily.gtd;

import android.graphics.Canvas;

import com.jfboily.fxgea.Game;
import com.jfboily.fxgea.Input;
import com.jfboily.fxgea.Screen;
import com.jfboily.fxgea.Sprite;
import com.jfboily.fxgea.Sprite.RefPixel;

public class Splash extends Screen
{
	private final int STATE_SPLASH1 = 1;
	private final int STATE_SPLASH2 = 2;
	private final int STATE_FADE = 3;
	private final int STATE_END = 4;
	
	private Sprite splash;
	private long timer;
	
	private int nextState = 0;
	

	public Splash(Game game) 
	{
		super(game);
		setState(STATE_SPLASH1);
	}

	@Override
	public void update(int state, boolean newState, long currentTime, long deltaTime) 
	{
		switch(state)
		{
		case STATE_SPLASH1:
			if(newState)
			{
				splash = createSprite("fxgea.png", PLANE_0, RefPixel.CENTER);
				splash.setPos(Game.getGame().getWidth() / 2, Game.getGame().getHeight() / 2);
				fadeIn(500);
				timer = currentTime + 5000;
			}
			
			if(Input.Touch.newDown || currentTime > timer)
			{
				setState(STATE_FADE);
				nextState = STATE_SPLASH2;
			}
			

			break;
			
		case STATE_FADE:
			if(newState)
			{
				fadeOut(500);
			}
			
			if(!isFading())
			{
				setState(nextState);
			}
			break;
			
		case STATE_SPLASH2:
			if(newState)
			{
				splash = createSprite("lektro.png", PLANE_0, RefPixel.CENTER);
				splash.setPos(Game.getGame().getWidth() / 2, Game.getGame().getHeight() / 2);
				fadeIn(500);
				timer = currentTime + 5000;
			}
			
			if(Input.Touch.newDown || currentTime > timer)
			{
				setState(STATE_FADE);
				nextState = STATE_END;
			}
			break;
			
		case STATE_END:
			Game.getGame().setScreen(new LevelSelectScreen((GTDGame)Game.getGame()));
			break;
		}
	}

	@Override
	public void draw(Canvas canvas) 
	{
		
	}

	@Override
	public void dispose() 
	{
		
	}

	@Override
	public void drawUI(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

}
