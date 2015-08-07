package com.jfboily.gtd;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.jfboily.fxgea.Game;
import com.jfboily.fxgea.Screen;
import com.jfboily.fxgea.Sprite;

public class Creep 
{
	private Sprite sprite;
	private Sprite explose;
	private int hits = 0;
	private Paint paint = new Paint();
	private int pathTarget = 0;
	private Point target;
	private float speed;
	private float speed1k;
	private int life;
	private Random random = new Random();
	private float lifeIndicatorRatio;
	
	public static final int STATE_ALIVE = 1;
	public static final int STATE_DEAD = 2;
	public static final int STATE_HIT = 3;
	public static final int STATE_DYING = 4;
	public static final int STATE_OUT = 5;
	
	private int state;
	
	private Path.PathNode curpath;
	private float distance = 0;
	private float deltaDistance = 0;
	private Path path;
	private int type;
	
	private int cash;
	private int points;
	
	private int origLife;
	
	private String[] creepSprites = new String[]
			{
				"creep1.png",
				"creep1.png",
				"creep2.png",
				"creep3.png",
			};
	
	public Creep(int type, int distance, int life, int speed, GTDScreen screen)
	{
		explose = screen.createSprite("creepex.png", 32, 32, Screen.PLANE_3, Sprite.RefPixel.CENTER);
		explose.setVisible(false);
		
		
		this.distance = -distance;
		this.speed = (float)speed;
		speed1k = speed / 1000.0f;
		this.life = life;
		this.type = type;
		this.points = life;
		origLife = life;


		sprite = screen.createSprite(creepSprites[this.type], 32, 32, Screen.PLANE_3, Sprite.RefPixel.CENTER);
		sprite.setVisible(false);
		
		lifeIndicatorRatio = 32.0f / (float)life;
		state = STATE_ALIVE;
		
		path = screen.getPath2();
		curpath = null;
		
		cash = 10;
	}
	
	public void setPath(Path path)
	{
		this.path = path;
	}
	
	public int getState()
	{
		return state;
	}
	
	public boolean isAlive()
	{
		return state == STATE_ALIVE || state == STATE_HIT;
	}
	
	public void update(long deltaTime)
	{
		switch(state)
		{
		case STATE_ALIVE:
		case STATE_HIT:
			
			deltaDistance = speed1k * deltaTime;
			distance += deltaDistance;
			
			if(distance > 0)
			{
				if(curpath == null)
				{
					curpath = path.getStartNode();
					sprite.setVisible(true);
					sprite.playAnim(Sprite.DEFAULT_ANIM, true);
				}
				
				curpath = curpath.getNodeAtDistance((int)distance);
				
				if(curpath == null)
				{
					sprite.setVisible(false);
					state = STATE_OUT;
					return;
				}
				int x = curpath.getX((int)distance);
				int y = curpath.getY((int)distance);
				sprite.setPos(x, y);
			}
			
			
			
			
			sprite.setFrame(0);
			if(state == STATE_HIT)
			{
				sprite.setFrame(1);
			}
			break;
			
		case STATE_DYING:
			if(explose.isVisible())
			{
				if(explose.isAnimDone())
				{
					state = STATE_DEAD;
					explose.setVisible(false);
				}
			}
			else
			{
				sprite.setVisible(false);
				explose.setVisible(true);
				explose.setPos(sprite.getX(), sprite.getY());
				explose.playAnim(Sprite.DEFAULT_ANIM, false);
				Game.getGame().getAudio().playSound(R.raw.splat, false);
			}
			break;
			
		case STATE_DEAD:
			
			break;
		
		
		case STATE_OUT:
			
			break;
		}

	}
	
	public int getX()
	{
		return sprite.getX();
	}
	
	public int getY()
	{
		return sprite.getY();
	}
	
	public Sprite getSprite()
	{
		return sprite;
	}
	
	public void hit(int damage)
	{
		hits+=damage;
		if(hits >= life)
		{
			hits = life;
			state = STATE_DYING;
		}
	}
	
	public int getHits()
	{
		return hits;
	}
	
	public int getDistance()
	{
		return (int)distance;
	}
	
	public void draw(Canvas canvas)
	{
		if(isAlive() && hits > 0)
		{
			float x = sprite.getX();
			float y = sprite.getY();
			float redRight = hits * lifeIndicatorRatio;
			paint.setColor(Color.GREEN);
			canvas.drawRect(x-16, y-20, x+16, y-16, paint);
			paint.setColor(Color.RED);
			canvas.drawRect(x-16, y-20, x-16+redRight, y-16, paint);
		}
	}
	
	
	
	public int getLife()
	{
		return life;
	}
	
	public void setLife(int life)
	{
		this.life = life;
	}
	
	public int getCash()
	{
		return cash;
	}
	
	public int getPoints()
	{
		return points;
	}
	
	public void reset(int repeat)
	{
		life = life + (int)(life * (repeat * 0.4));
		state = STATE_ALIVE;
	}
}
