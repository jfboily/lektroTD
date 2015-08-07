package com.jfboily.gtd;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

import com.jfboily.fxgea.Button;
import com.jfboily.fxgea.ButtonClickListener;
import com.jfboily.fxgea.Game;
import com.jfboily.fxgea.Screen;

public class LevelSelectScreen extends Screen implements ButtonClickListener
{
	private final String[] levels = new String[]
			{
			"level1.xml",
			"level2.xml",
			"level3.xml",
			"level4.xml",
			};
	
	private Button[] buttons = new Button[levels.length];
	private final int bw = 64;
	private final int bspacer = 8;
	private final int bx = 32;
	private final int by = 32;
	private Paint btnPaint = new Paint();
	
	public LevelSelectScreen(Game game) 
	{
		super(game);
		int x, y;
		
		
		x = bx;
		y = by;
		for(int i = 0; i < levels.length; i++)
		{
			buttons[i] = new Button(x, y, 64, 64, "btnlevel.png", this);
			x += (bw + bspacer);
			if(x > Game.getGame().getWidth() - bw - bspacer)
			{
				x = bx;
				y += (bw + bspacer);
			}
		}
	}

	@Override
	public void update(int state, boolean newState, long currentTime, long deltaTime) 
	{

	}

	@Override
	public void draw(Canvas canvas) 
	{
		btnPaint.setTextAlign(Align.CENTER); 
		btnPaint.setTextSize(18);
		btnPaint.setColor(Color.WHITE);
		for(int i = 0; i < levels.length; i++)
		{
			canvas.drawText("L"+(i+1), buttons[i].getRect().centerX(), buttons[i].getRect().centerY()+9, btnPaint);
		}
	}

	@Override
	public void dispose() 
	{

	}

	public void buttonClick(Button button) 
	{
		for(int i = 0; i < levels.length; i++)
		{
			if(button == buttons[i])
			{
				GTDProfile profile = ((GTDGame)Game.getGame()).profile;
				profile.curLevel = i+1;
				profile.curLevelFName = levels[i];
				
				//profile.load("");
				//profile.save("");
				
				
				Game.getGame().setScreen(new GTDScreen((GTDGame)Game.getGame()));
			}
		}
		
	}

	@Override
	public void drawUI(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

}
