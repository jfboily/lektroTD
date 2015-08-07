package com.jfboily.gtd;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.jfboily.fxgea.Game;
import com.jfboily.fxgea.Screen;
import com.jfboily.fxgea.Sprite;
import com.jfboily.fxgea.TileMap.Tile;
import com.jfboily.fxgea.Touchable;
import com.jfboily.gtd.TowerInfos.FireType;

public class Tower// implements Touchable
{
	public static final int MAX_UPGRADE_LEVEL = 2;
	private Sprite turret;
	private GTDScreen screen;
	private final int MAX_BALLES = 10;
	private Balle balles[] = new Balle[MAX_BALLES];

	
	private long delaiRecharge = 250;
	private long timerRecharge;
	private final float VIT_BALLES = 460.0f;
	
	private final int screenW, screenH;
	private boolean inRange = false;
	
	private Paint paint = new Paint();
	
	private int radius;
	private int radius2;
	
	private Creep targetCreep;
	
	private int damage = 1;
	
	private int ANIM_FIRE;
	private int ANIM_BALLES;
	
	private boolean isSelected = false;
	
	private final int TOWER_SIZE = 32;
	private final int x, y;
	private int upgradeLevel;
	private final int type;
	private TowerInfos infos;
	private Tile tile;
	private long radiusTime = 0;
	private int radiusSID = -1;
	
	public Tower(Tile tile, int type, GTDScreen screen)
	{
		screenW = Game.getGame().getWidth();
		screenH = Game.getGame().getHeight();
		
		this.tile = tile;
		this.screen = screen;
		this.x = tile.screenRect.centerX();
		this.y = tile.screenRect.centerY();
		this.type = type;
		
		timerRecharge = 0;
		upgradeLevel = 0;
		// generation des balles
		for(int i = 0; i < MAX_BALLES; i++)
		{
			balles[i] = new Balle();
		}
		initInfos();
		
		//Game.getGame().getInput().registerTouchable(this);
	}
	
	private void initInfos()
	{
	
		// nouvelles infos
		infos = towerInfos[type][upgradeLevel];
		
		if(turret == null)
		{
			turret = screen.createSprite(infos.turretSprite, TOWER_SIZE, TOWER_SIZE, Screen.PLANE_2, Sprite.RefPixel.CENTER);
			turret.setPos(x, y);
		}
		ANIM_FIRE = turret.createAnim(anims[upgradeLevel]);
		turret.setFrame(anims[upgradeLevel][0]);
				
		// generation des balles
		for(int i = 0; i < MAX_BALLES; i++)
		{
			if(balles[i].sprite == null)
			{
				if(infos.fireType == TowerInfos.FireType.BULLET)
				{
					balles[i].sprite = screen.createSprite(infos.bulletSprite, 16, 16, Screen.PLANE_2, Sprite.RefPixel.CENTER);
				}
				else
				{
					balles[i].sprite = screen.createSprite(infos.bulletSprite, 96, 96, Screen.PLANE_2, Sprite.RefPixel.CENTER);
				}
				balles[i].sprite.setVisible(false);
			}
			
			ANIM_BALLES = balles[i].sprite.createAnim(animsballes[upgradeLevel]);
			
			if(balles[i].hitSprite == null)
			{
				balles[i].hitSprite = screen.createSprite(infos.hitSprite, 16, 16, Screen.PLANE_0, Sprite.RefPixel.CENTER);
				balles[i].hitSprite.setVisible(false);
			}
		}
		
		
		
		damage = infos.damage;
		radius = infos.radius;
		radius2 = radius * radius;
		delaiRecharge = infos.fireDelay;
	}
	
	public void upgradeLevel()	
	{
		if(upgradeLevel < MAX_UPGRADE_LEVEL)
		{
			upgradeLevel++;
		}
		
		initInfos();
		Game.getGame().getAudio().playSound(R.raw.towerup, false);
	}
	
	public int getUpgradeCost()
	{
		int ret = 1000000;
		if(upgradeLevel < MAX_UPGRADE_LEVEL)
		{
			ret =  towerInfos[type][upgradeLevel+1].cost;
		}
		return ret;
	}
	
	public void update(long deltaTime)
	{
		timerRecharge -= deltaTime;
		inRange = false;	
		
		targetCreep = getTarget();
	
		if(targetCreep != null)
		{
			turret.rotateTo(targetCreep.getX(), targetCreep.getY());
			
			if(timerRecharge <= 0)
			{
				timerRecharge = delaiRecharge;
				if(infos.fireType == TowerInfos.FireType.BULLET)
				{
					tireBalle(targetCreep);
				}
				else if(infos.fireType == TowerInfos.FireType.RADIUS)
				{
					tireRadius();
				}
			}
			
			inRange = true;
		}
		
		if(infos.fireType == TowerInfos.FireType.BULLET)
		{
			updateBalles(deltaTime);
		}
		else if(infos.fireType == TowerInfos.FireType.RADIUS)
		{
			updateRadius(deltaTime);
		}
	}
	
	private Creep getTarget()
	{
		ArrayList<Creep> creeps = screen.getCreeps();
		Creep target = null;
		
		for(int i = 0; i < creeps.size(); i++)
		{
			Creep c = creeps.get(i);
			if(c.isAlive())
			{
				int cx = c.getX();
				int cy = c.getY();
				int tx = turret.getX();
				int ty = turret.getY();
				int dist2 = ((cx - tx)*(cx - tx)) + ((cy - ty)*(cy - ty));
				
				if(dist2 < radius2)
				{
					if(target == null)
					{
						target = c;
					}
					else
					{
						if(c.getDistance() > target.getDistance())
						{
							target = c;
						}
					}
					
				}
			}
		}
		
		return target;
	}
	
	private void tireBalle(Creep target)
	{
		float dx = target.getSprite().getX() - turret.getX();
		float dy = target.getSprite().getY() - turret.getY();
		float longueur = (float)Math.sqrt(dx*dx + dy*dy);
//		float vitesse = VIT_BALLES;
		float vxunit = dx / longueur;
		float vyunit = dy / longueur;
//		float vx = vxunit * vitesse;
//		float vy = vyunit * vitesse;
		
		for(int i = 0; i < MAX_BALLES; i++)
		{
			if(!balles[i].active)
			{
				balles[i].sprite.setPos((int)(turret.getX()+16*vxunit), (int)(turret.getY()+16*vyunit));
				balles[i].sprite.rotateTo(target.getSprite().getX(), target.getSprite().getY());
				balles[i].sprite.setVisible(true);
				balles[i].sprite.setSpeed(VIT_BALLES);
				balles[i].sprite.playAnim(ANIM_BALLES, true);
				balles[i].target = target;
				balles[i].active = true;
				break;
			}
		}
		
		Game.getGame().getAudio().playSound(infos.fireSound, false);
		turret.playAnim(ANIM_FIRE, false);
	}
	
	private void updateBalles(long deltaTime)
	{
		int bx, by;
		Balle balle;
		
		for(int i = 0; i < MAX_BALLES; i++)
		{
			balle = balles[i];
			if(balle.active)
			{
				if(balle.sprite.isVisible())
				{
					bx = balle.sprite.getX();
					by = balle.sprite.getY();
					
					balle.sprite.rotateTo(balles[i].target.getSprite().getX(), balles[i].target.getSprite().getY());
					balle.sprite.moveDir(deltaTime);
					
					if(bx < 0 || bx > screenW || by < 0 || by > screenH)
					{
						balle.sprite.setVisible(false);
						balle.active = false;
					}
					
					//ArrayList<Creep> creeps = level.getCreeps();
					//for(int j = 0; j < creeps.size(); j++)
					{
						Creep c = balle.target;//creeps.get(j);
						// collisions avec un creep
						if(c.isAlive())
						{
							if(balle.sprite.collidesWith(c.getSprite()))
							{
								balle.sprite.setVisible(false);
								c.hit(damage);
								balle.hitSprite.setVisible(true);
								balle.hitSprite.playAnim(Sprite.DEFAULT_ANIM, false);
								balle.hitSprite.setPos(c.getX(), c.getY());
								break;
							}
						}
						else
						{
							balle.sprite.setVisible(false);
							balle.active = false;
						}
					}
				}
				else
				{
					if(balle.hitSprite.isAnimDone())
					{
						balle.hitSprite.setVisible(false);
						balle.active = false;
					}
				}
			}
		}
	}
	
	private void tireRadius()
	{
		balles[0].sprite.setPos(turret.getX(), turret.getY());
		balles[0].sprite.setVisible(true);
		balles[0].sprite.playAnim(Sprite.DEFAULT_ANIM, true);
		radiusTime = 250L;
		hitRadius();
		if(radiusSID != -1)
		{
			Game.getGame().getAudio().stopSound(radiusSID);
		}
		radiusSID = Game.getGame().getAudio().playSound(infos.fireSound, true);
		turret.playAnim(ANIM_FIRE, false);
	}
	
	private void hitRadius()
	{
		ArrayList<Creep> creeps = screen.getCreeps();
		for(int i = 0; i < creeps.size(); i++)
		{
			Creep c = creeps.get(i);
			if(c.isAlive())
			{
				int cx = c.getX();
				int cy = c.getY();
				int tx = turret.getX();
				int ty = turret.getY();
				int dist2 = ((cx - tx)*(cx - tx)) + ((cy - ty)*(cy - ty));
				
				if(dist2 < radius2)
				{
					c.hit(infos.damage);
					
				}
			}
		}
		
	}
	
	private void updateRadius(long deltaTime)
	{
		if(!balles[0].sprite.isVisible())
		{
			return;
		}
		else
		{
			radiusTime -= deltaTime;
			
			if(radiusTime <= 0)
			{
				balles[0].sprite.setVisible(false);
				Game.getGame().getAudio().stopSound(radiusSID);
			}
			
		}
	}
	
	public boolean inRange()
	{
		return inRange;
	}
	
	public void draw(Canvas canvas)
	{
		if(isSelected)
		{
			paint.setARGB(64, 255, 255, 255);
			canvas.drawCircle(x, y, radius, paint);
		}
	}
	
	private class Balle
	{
		public Sprite sprite;
		public Creep target;
		public Sprite hitSprite;
		public boolean active = false;
	}

	
	public boolean onTouch(int x, int y) 
	{
		//level.selectTower(this);
		return true;
	}
	
	public TowerInfos getTowerInfos()
	{
		return infos;
	}

	
	public boolean onMove(int x, int y) {
		// TODO Auto-generated method stub
		return true;
		
	}

	
	public boolean onRelease(int x, int y) {
		// TODO Auto-generated method stub
		return true;
	}

	
	public Rect getRect() {
		// TODO Auto-generated method stub
		return turret.getRect();
	}
	
	public int getX()
	{
		return turret.getX();
	}
	
	public int getY()
	{
		return turret.getY();
	}
	
	public Tile getTile()
	{
		return tile;
	}

	public int getUpgradeLevel()
	{
		return upgradeLevel;
	}
	
	public void setSelected(boolean selected)
	{
		isSelected = selected;
	}
	
	public void destroy()
	{
		// delete l'ancien stock
		if(turret != null)
		{
			screen.deleteSprite(turret);
		}
		
		for(int i =0; i < MAX_BALLES; i++)
		{
			if(balles[i].sprite != null)
			{
				screen.deleteSprite(balles[i].sprite);
			}
			
			if(balles[i].hitSprite != null)
			{
				screen.deleteSprite(balles[i].hitSprite);
			}
		}
	}
	
	public static final TowerInfos[][] towerInfos = new TowerInfos[][]
			{
				new TowerInfos[]
						{
							new TowerInfos("Canon", "tower1.png", "balle1.png", "hit1.png", R.raw.fire1, 500, TowerInfos.FireType.BULLET, 5, 64, 100, 33),
							new TowerInfos("Canon", "tower1.png", "balle1.png", "hit1.png", R.raw.fire1, 300, TowerInfos.FireType.BULLET, 5, 80, 110, 70),
							new TowerInfos("Canon", "tower1.png", "balle1.png", "hit1.png", R.raw.fire1, 200, TowerInfos.FireType.BULLET, 7, 96, 200, 136),
						},
				new TowerInfos[]
						{
							new TowerInfos("Laser", "tower2.png", "fire2.png", "hit1.png", R.raw.fire2, 550, TowerInfos.FireType.BULLET, 3, 80, 80, 20),
							new TowerInfos("Laser", "tower2.png", "fire2.png", "hit1.png", R.raw.fire2, 350, TowerInfos.FireType.BULLET, 3, 96, 60, 30),
							new TowerInfos("Laseer", "tower2.png", "fire2.png", "hit1.png", R.raw.fire2, 210, TowerInfos.FireType.BULLET, 5, 108, 120, 50),
						},
				new TowerInfos[]
						{
							new TowerInfos("Bomber", "tower3.png", "fire3.png", "hit1.png", R.raw.fire, 800, TowerInfos.FireType.BULLET, 8, 108, 300, 100),
							new TowerInfos("Bomber", "tower3.png", "fire3.png", "hit1.png", R.raw.fire, 500, TowerInfos.FireType.BULLET, 10, 120, 200, 130),
							new TowerInfos("Bomber", "tower3.png", "fire3.png", "hit1.png", R.raw.fire, 400, TowerInfos.FireType.BULLET, 12, 132, 180, 250),
						},						
				new TowerInfos[]
						{
							new TowerInfos("Radius", "tower4.png", "radius1.png", "hit1.png", R.raw.buzz, 550, TowerInfos.FireType.RADIUS, 1, 48, 75, 20),
							new TowerInfos("Radius", "tower4.png", "radius1.png", "hit1.png", R.raw.buzz, 350, TowerInfos.FireType.RADIUS, 1, 48, 50, 30),
							new TowerInfos("Radius", "tower4.png", "radius1.png", "hit1.png", R.raw.buzz, 210, TowerInfos.FireType.RADIUS, 2, 48, 100, 50),
						},							
			};
	
	public static final String[] towerBtns = new String[]
			{
				"tower1btn.png",
				"tower2btn.png",
				"tower3btn.png",
				"tower4btn.png"
			};
	
	private static final int[][] anims = new int[][]
			{
				new int[]{0, 1, 2, 3, 0},
				new int[]{4, 5, 6, 7, 4},
				new int[]{8, 9, 10, 11, 8},
			};
	
	private static final int[][] animsballes = new int[][]
			{
				new int[]{0, 1},
				new int[]{2, 3},
				new int[]{4, 5, 6},
			};
}
