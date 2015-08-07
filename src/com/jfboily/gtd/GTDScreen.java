package com.jfboily.gtd;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.jfboily.fxgea.Button;
import com.jfboily.fxgea.ButtonClickListener;
import com.jfboily.fxgea.Game;
import com.jfboily.fxgea.Screen;
import com.jfboily.fxgea.Sprite;
import com.jfboily.fxgea.TileMap;
import com.jfboily.fxgea.TileMap.Tile;
import com.jfboily.fxgea.Touchable;

public class GTDScreen extends Screen implements ButtonClickListener, Touchable
{
	private Level level;
	private Button btnRestart;
	private Button btnPause;
	private Button btnPlay;
	private Button btnQuit;
	private long deltaTime;
	
	ArrayList<Creep> creeps = new ArrayList<Creep>(100);
	ArrayList<Tower> towers = new ArrayList<Tower>(50);;
	
	private ArrayList<Creep> creepsToRemove = new ArrayList<Creep>();
	
	private TileMap tilemap;
	private final Rect rect=new Rect(0, 0, Game.getGame().getWidth(), Game.getGame().getHeight());
	
	private Path lepath;
	
	private int life;
	private int lifeMax;
	private int score;
	private int cash;
	private int oldCash;
	private int oldScore;
	private int oldLife;
	private int oldCurWave;
	
	private Button btnUp = null;
	private Button btnCash = null;
	private Button btnTowers[] = new Button[Tower.towerInfos.length];
	private Button btnOK = null;
	private Button btnCancel = null;
	private Button btnX1 = null;
	private Button btnX2 = null;
	private Sprite tileSelectSprite;
	private Sprite towerSelectBG;
	private Sprite towerCreateSprite;
	
	private Tower selectedTower = null;
	private Tile selectedTile = null;
	private Tower tempoNewTower = null;
	private boolean towerSelectActive = false;
	private boolean towerUpgradeActive = false;
	
	private Paint hudPaint = new Paint();
	private Paint btnPaint = new Paint();
	private final int TEXT_SIZE = 24;
	private int scoreSize = TEXT_SIZE;
	private int cashSize = TEXT_SIZE;
	private int lifeSize = TEXT_SIZE;
	private int waveSize = TEXT_SIZE;
	
	private boolean x2 = false;
	
	private int curWave;
	private int nbWaves;
	private int curRepeat;
	private int creepsToKill;
	private boolean gameOver = false;
	
	public GTDScreen(GTDGame game) 
	{
		super(game);
		
		if(game.profile.curLevelFName != null)
		{
			level = new Level(game.profile.curLevelFName, this);
		}

		
		cash = level.getStartCash();
		score = 0;
		lifeMax = level.getLives();
		life = lifeMax;
		

		
		tilemap = new TileMap(level.getTilemapName());
		this.setBackground(tilemap.renderAllLayers());
		
		lepath = new Path(tilemap);
		
		
		btnRestart = new Button(704, 16, 32, 32, "restart.png", this);
		btnPause = new Button(16, 16, 32, 32, "pause.png", this);
		btnPlay = new Button(16, 16, 32, 32, "play.png", this);
		btnQuit = new Button(752, 16, 32, 32, "btnQuit.png", this);
		btnPause.setVisible(false);
		btnPlay.setFlashing(true);
	
		// boutons pour upgrade des Towers
		btnUp = new Button(0, 0, 32, 32, "up.png", this);
		btnUp.setVisible(false);

		btnCash = new Button(0, 0, 32, 32, "cash.png", this);
		btnCash.setVisible(false);
		
		btnOK = new Button(0, 0, 32, 32, "ok.png", this);
		btnOK.setVisible(false);
		
		btnCancel = new Button(0, 0, 32, 32, "cancel.png", this);
		btnCancel.setVisible(false);
		
		// boutons pour selection des Towers
		for(int i = 0; i < btnTowers.length; i++)
		{
			btnTowers[i] = new Button(0, 0, 32, 32, Tower.towerBtns[i], this);
			btnTowers[i].setVisible(false);
		}
		
		btnX1 = new Button(48, 16, 32, 32, "x1.png", this);
		btnX1.setVisible(true);
		btnX2 = new Button(48, 16, 32, 32, "x2.png", this);
		btnX2.setVisible(false);
		x2 = false;
		
		
		tileSelectSprite = this.createSprite("tileselect.png", 32, 32, Screen.PLANE_1, Sprite.RefPixel.CENTER);
		tileSelectSprite.setVisible(false);
		towerSelectBG = this.createSprite("towerselbg.png", Screen.PLANE_1, Sprite.RefPixel.CENTER);
		towerSelectBG.setVisible(false);
		
		towerCreateSprite = createSprite("towerup.png", 32, 32, PLANE_1, Sprite.RefPixel.CENTER);
		towerCreateSprite.setVisible(false);
		
		Game.getGame().getInput().registerTouchable(this);
		
		initPaints();
		
		nbWaves = level.getNbWaves();
		curWave = 0;
		initCreeps();
		
		oldCash = cash;
		oldLife = life;
		oldScore = score;
		oldCurWave = curWave;
		
		setPause(true);
		
		Game.getGame().getAudio().loadMusic(R.raw.shining);
		Game.getGame().getAudio().playMusic();
	}

	void initPaints()
	{
		hudPaint.setColor(Color.GREEN);
		hudPaint.setTextSize(TEXT_SIZE);
		scoreSize = TEXT_SIZE;
		waveSize = TEXT_SIZE;
		lifeSize = TEXT_SIZE;
		cashSize = TEXT_SIZE;
		
		btnPaint.setColor(Color.WHITE);
		btnPaint.setTextSize(12);
	}
	
	private void initCreeps()
	{
		creeps.clear();
		Creep[] cs = level.getWave(curWave);
		for(int i = 0; i < cs.length; i++)
		{
			cs[i].setPath(lepath);
			cs[i].setLife((int)(cs[i].getLife() + (curRepeat * 0.4)));
			creeps.add(cs[i]);
		}
		creepsToKill = cs.length;
	}
	
	@Override
	public void update(int state, boolean newState, long currentTime, long deltaTime) 
	{
		this.deltaTime = deltaTime;
		
		if(x2)
		{
			this.deltaTime *= 3;
		}
		
		if(!paused)
		{
			creepsToRemove.clear();
			
			for(int i = 0; i < creeps.size(); i++)
			{
				Creep c = creeps.get(i);
				
				// update!
				c.update(this.deltaTime);
				
				// check si on doit deleter
				if(c.getState() == Creep.STATE_DEAD)
				{
					creepsToRemove.add(c);
					// cash-in!
					cash += c.getCash();
					score += c.getPoints();
					creepsToKill--;
				}
				// check si le creep a reussi a sortir
				else if(c.getState() == Creep.STATE_OUT)
				{
					creepsToRemove.add(c);
					life--;
					creepsToKill--;
				}
			}
			
			creeps.removeAll(creepsToRemove);
			
			for(int i = 0; i < towers.size(); i++)
			{
				towers.get(i).update(this.deltaTime);
			}	
			
			// update menus
			if(towerSelectActive)
			{
				for(int i = 0; i < btnTowers.length; i++)
				{
					if(cash >= Tower.towerInfos[i][0].cost)
					{
						btnTowers[i].setActive(true);
					}
					else
					{
						btnTowers[i].setActive(false);
					}
				}
			}
			
			if(towerUpgradeActive)
			{
				if(cash >= selectedTower.getUpgradeCost())
				{
					btnUp.setActive(true);
				}
				else
				{
					btnUp.setActive(false);
				}
			}
			
			if(creepsToKill <= 0)
			{
				nextWave();
			}
		}
		else
		{
			if(btnPause.isVisible())
			{
				btnPause.setVisible(false);
				btnPlay.setVisible(true);
			}
		}
	}
	
	private void nextWave()
	{
		
		if(curWave >= nbWaves-1)
		{
			gameOver = true;
		}
		else
		{
			cash += level.getWaveCash(curWave);
			curWave++;
			initCreeps();
		}
	}

	@Override
	public void draw(Canvas canvas) 
	{
		// dessine les creeps
		for(int i = 0; i < creeps.size(); i++)
		{
			creeps.get(i).draw(canvas);
		}
		
		
		// dessine les towers
		for(int i = 0; i < towers.size(); i++)
		{
			towers.get(i).draw(canvas);
		}
		
		
	}

	@Override
	public void dispose() 
	{
		
	}


	
	public ArrayList<Creep> getCreeps()
	{
		return creeps;
	}
	
	public ArrayList<Tower> getTowers()
	{
		return towers;
	}

	
	public Path getPath2()
	{
		return lepath;
	}
	
	public boolean onTouch(int x, int y) 
	{
		Tile tile = tilemap.getTileScreen(x, y, 0);
		
		if(selectedTower != null)
		{
			hideTowerUpgradeMenu();
			//mustHideTowerUpgradeMenu = true;
			return false;
		}
		
		if(selectedTile != null)
		{
			hideTowerSelectMenu();
			return false;
		}
		
		if(btnOK.isVisible())
		{
			btnOK.setVisible(false);
			tempoNewTower.destroy();
			tempoNewTower = null;
		}
		
		if(tile != null)
		{
			if(tile.type == 1 && tile.state == 0)
			{
				showTowerSelectMenu(tile);
				return true;
			}
			else if(tile.state != 0)
			{
				showTowerUpgradeMenu(tile);
				return true;
			}
			
			
		}
		
		return false;
	}

	
	public boolean onMove(int x, int y) 
	{
		return true;
	}

	
	public boolean onRelease(int x, int y) 
	{
		return true;
	}

	
	public Rect getRect() 
	{
		return rect;
	}
	
	
	private void setPause(boolean pause)
	{
		this.paused = pause;
		
		if(paused)
		{
			btnPause.setVisible(false);
			btnPlay.setVisible(true);
		}
		else
		{
			btnPlay.setVisible(false);
			btnPause.setVisible(true);
		}
	}

	public void buttonClick(Button button) 
	{
		if(button == btnRestart)
		{
			Game.getGame().setScreen(new GTDScreen((GTDGame)Game.getGame()));
		}
		
		if(button == btnPause)
		{
			setPause(true);
		}
		
		if(button == btnPlay)
		{
			setPause(false);
		}
		
		if(button == btnUp)
		{
			cash -= selectedTower.getUpgradeCost();
			selectedTower.upgradeLevel();
			//hideTowerUpgradeMenu();
			showTowerUpgradeMenu(selectedTower.getTile());
			
			towerCreateSprite.setPos(tempoNewTower.getX(), tempoNewTower.getY());
			towerCreateSprite.setVisible(true);
			towerCreateSprite.playAnim(Sprite.DEFAULT_ANIM, false);
		}
		
		if(button == btnCash)
		{
			cash += selectedTower.getTowerInfos().sellValue;
			towers.remove(selectedTower);
			selectedTower.getTile().state = 0;
			selectedTower.destroy();
			Game.getGame().getAudio().playSound(R.raw.splat, false);
			hideTowerUpgradeMenu();
			
			towerCreateSprite.setPos(tempoNewTower.getX(), tempoNewTower.getY());
			towerCreateSprite.setVisible(true);
			towerCreateSprite.playAnim(Sprite.DEFAULT_ANIM, false);
		}
		
		for(int i = 0; i < btnTowers.length; i++)
		{
			if(button == btnTowers[i])
			{
				tempoNewTower = new Tower(selectedTile, i, this);
				hideTowerSelectMenu();
//				if(tempoNewTower.getTile().mapY < 4)
//				{
//					btnOK.setPos(tempoNewTower.getX() - 16, tempoNewTower.getY() + 32);
//				}
//				else
//				{
//					btnOK.setPos(tempoNewTower.getX() - 16, tempoNewTower.getY() - 64);
//				}
				btnOK.setPos(btnTowers[i].getX(), btnTowers[i].getY());
				btnOK.setVisible(true);
			}
		}
		
		if(button == btnOK)
		{
			cash -= tempoNewTower.getTowerInfos().cost;
			towers.add(tempoNewTower);
			tempoNewTower.getTile().state = 1;
			btnOK.setVisible(false);
			towerCreateSprite.setPos(tempoNewTower.getX(), tempoNewTower.getY());
			towerCreateSprite.setVisible(true);
			towerCreateSprite.playAnim(Sprite.DEFAULT_ANIM, false);
		}
		
		if(button == btnX1)
		{
			btnX1.setVisible(false);
			btnX2.setVisible(true);
			x2 = true;
		}
		
		if(button == btnX2)
		{
			btnX2.setVisible(false);
			btnX1.setVisible(true);
			x2 = false;
		}
		
		if(button == btnQuit)
		{
			Game.getGame().setScreen(new LevelSelectScreen(Game.getGame()));
		}
	}	
	
	public void selectTower(Tower t)
	{
		if(selectedTower == null)
		{
			selectedTower = t;
		}
	}
	
	private void showTowerSelectMenu(Tile tile)
	{
		int x = tile.screenRect.centerX(), y = tile.screenRect.centerY();
		
		selectedTile = tile;
		
		if(x < towerSelectBG.getWidth() / 2)
		{
			x = towerSelectBG.getWidth() / 2;
		}
		if(x > Game.getGame().getWidth() - (towerSelectBG.getWidth() / 2))
		{
			x = Game.getGame().getWidth() - (towerSelectBG.getWidth() / 2);
		}
		
		if(tile.mapY < 4)
		{
			y += 16;
		}
		else 
		{
			y -= 16;
		}
		
		towerSelectBG.setPos(x, y);
		towerSelectBG.setVisible(true);
		
		int tx = towerSelectBG.getX() - (towerSelectBG.getWidth() / 2) + 4;
		int ty = towerSelectBG.getY() - (towerSelectBG.getHeight() / 2) + 4;
		for(int i = 0; i < btnTowers.length; i++)
		{
			btnTowers[i].setPos(tx, ty);
			btnTowers[i].setVisible(true);
			if(cash >= Tower.towerInfos[i][0].cost)
			{
				btnTowers[i].setActive(true);
			}
			else
			{
				btnTowers[i].setActive(false);
			}
			tx += 40;
		}
		
		tileSelectSprite.setPos(tile.screenRect.centerX(), tile.screenRect.centerY());
		tileSelectSprite.playAnim(Sprite.DEFAULT_ANIM, true);
		tileSelectSprite.setVisible(true);
		towerSelectActive = true;
	}
	
	private void hideTowerSelectMenu()
	{
		towerSelectBG.setVisible(false);
		for(int i = 0; i < btnTowers.length; i++)
		{
			//btnTowers[i].setPos(tile.screenRect.left, tile.screenRect.top - 40);
			btnTowers[i].setVisible(false);
		}
		
		tileSelectSprite.setVisible(false);
		selectedTile = null;
		towerSelectActive = false;
	}
	
	private void showTowerUpgradeMenu(Tile tile)
	{
		selectedTower = null;
		for(Tower t : towers)
		{
			if(t.getTile() == tile)
			{
				selectedTower = t;
				break;
			}
		}	
		
		if(selectedTower != null)
		{
			btnUp.setPos(selectedTower.getX() - 16, selectedTower.getY() - 64);
			btnUp.setVisible(true);
			btnCash.setPos(selectedTower.getX() - 16, selectedTower.getY() + 32);
			btnCash.setVisible(true);
			selectedTower.setSelected(true);	
			
			btnUp.setActive(false);
			if(selectedTower.getUpgradeLevel() < Tower.MAX_UPGRADE_LEVEL)
			{
				if(cash >= selectedTower.getUpgradeCost())
				{
					btnUp.setActive(true);
				}
			}
			
			towerUpgradeActive = true;
		}
	}
	
	private void hideTowerUpgradeMenu()
	{
		if(selectedTower != null)
		{
			selectedTower.setSelected(false);
		}
		
		btnUp.setVisible(false);
		btnCash.setVisible(false);
		selectedTower = null;
		towerUpgradeActive = false;
	}

	@Override
	public void drawUI(Canvas canvas) 
	{
		if(cash != oldCash)
		{
			cashSize = 28;
		}
		if(life != oldLife)
		{
			lifeSize = 28;
		}
		if(curWave != oldCurWave)
		{
			waveSize = 28;
		}
		if(score != oldScore)
		{
			scoreSize = 28;
		}
		
		// dessine le HUD
		hudPaint.setTextSize(TEXT_SIZE);
		canvas.drawText("Score : ", 96, 44, hudPaint);
		canvas.drawText("wave     / "+nbWaves, 250, 44, hudPaint);
		canvas.drawText("$ ", 400, 44, hudPaint);
		canvas.drawText("life     / "+lifeMax, 500, 44, hudPaint);
		hudPaint.setTextSize(scoreSize);
		canvas.drawText(String.valueOf(score), 180, 44, hudPaint);
		hudPaint.setTextSize(waveSize);
		canvas.drawText(String.valueOf(curWave+1), 312, 44, hudPaint);
		hudPaint.setTextSize(cashSize);
		canvas.drawText(String.valueOf(cash), 420, 44, hudPaint);
		hudPaint.setTextSize(lifeSize);
		canvas.drawText(String.valueOf(life), 540, 44, hudPaint);
		if(scoreSize > TEXT_SIZE)
		{
			scoreSize--;
		}
		if(waveSize > TEXT_SIZE)
		{
			waveSize--;
		}
		if(cashSize > TEXT_SIZE)
		{
			cashSize--;
		}
		if(lifeSize > TEXT_SIZE)
		{
			lifeSize--;
		}
		oldLife = life;
		oldScore = score;
		oldCash = cash;
		oldCurWave = curWave;
		
		// dessine les menus
		if(towerUpgradeActive)
		{
			if(selectedTower.getUpgradeLevel() < Tower.MAX_UPGRADE_LEVEL)
			{
				canvas.drawText(""+selectedTower.getUpgradeCost(), btnUp.getX(), btnUp.getY()+32, btnPaint);
			}
			else
			{
				canvas.drawText("MAX", btnUp.getX(), btnUp.getY()+32, btnPaint);
			}
			canvas.drawText(""+selectedTower.getTowerInfos().sellValue, btnCash.getX(), btnCash.getY()+32, btnPaint);
		}
		
		if(towerSelectActive)
		{
			for(int i = 0; i < btnTowers.length; i++)
			{
				canvas.drawText(""+Tower.towerInfos[i][0].cost, btnTowers[i].getX(), btnTowers[i].getY()+32, btnPaint);
			}
		}
		
		if(towerCreateSprite.isVisible())
		{
			if(towerCreateSprite.isAnimDone())
			{
				towerCreateSprite.setVisible(false);
			}
		}

		
	}

}
