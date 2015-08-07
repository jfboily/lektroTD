package com.jfboily.gtd;

import com.jfboily.fxgea.TileMap;
import com.jfboily.fxgea.TileMap.Tile;

public class Path 
{
	private final int NODE_LENGTH = 32;
	
	private PathNode head;
	public static final int HAUT = 1;
	public static final int BAS = 2;
	public static final int GAUCHE = 3;
	public static final int DROITE = 4;
	
	private final int MIDX;
	
	public Path(TileMap tilemap)
	{
		MIDX = tilemap.getTileWidth() / 2;
		
		Tile startTile = null;
		int dir = GAUCHE;
		int prevdir;
		boolean found = false;
		
		// cherche la tile de dpart : TYPE = 2, 5, 6, 7
		for(int j = 0; j <tilemap.getMapHeight() && !found; j++)
		{
			for(int i = 0; i < tilemap.getMapWidth() && !found; i++)
			{
				Tile t = tilemap.getTileMap(i, j, 0); 
				if( t.type == 2)
				{
					startTile = t;
					dir = DROITE;
					found = true;
				}
				else if(t.type == 5)
				{
					startTile = t;
					dir = BAS;
					found = true;
				}
				else if(t.type == 6)
				{
					startTile = t;
					dir = GAUCHE;
					found = true;
				}
				else if(t.type == 7)
				{
					startTile = t;
					dir = HAUT;
					found = true;
				}
			}
		}
		
		prevdir = dir;
		if(startTile == null)
		{
			throw new IllegalArgumentException("Path : Impossible de trouve la tile de depart (type = 2)");
		}
		
		// demarre le chemin.. ooookaayyyyy...
		head = new PathNode(startTile, 0, dir);

		PathNode cur = head;

		// 4, 13, 14, 21
		while(cur.tile.type != 4 && cur.tile.type != 13 && cur.tile.type != 14 && cur.tile.type != 21)
		{
			Tile nextTile = null;
			
			switch(dir)
			{
			case HAUT:
				nextTile = tilemap.getTileMap(cur.tile.mapX, cur.tile.mapY - 1, 0);
				// -|
				if(nextTile.type == 12)
				{
					dir = GAUCHE;
				}
				
				// |-
				if(nextTile.type == 11)
				{
					dir = DROITE;
				}				
				break;
				
			case BAS:
				nextTile = tilemap.getTileMap(cur.tile.mapX, cur.tile.mapY + 1, 0);
				// _|
				if(nextTile.type == 20)
				{
					dir = GAUCHE;
				}
				
				// |_
				if(nextTile.type == 19)
				{
					dir = DROITE;
				}	
				break;
				
			case GAUCHE:
				nextTile = tilemap.getTileMap(cur.tile.mapX - 1, cur.tile.mapY, 0);
				// |-
				if(nextTile.type == 11)
				{
					dir = BAS;
				}
				//|_
				if(nextTile.type == 19)
				{
					dir = HAUT;
				}
				break;
				
			case DROITE:
				nextTile = tilemap.getTileMap(cur.tile.mapX + 1, cur.tile.mapY, 0);
				// -|
				if(nextTile.type == 12)
				{
					dir = BAS;
				}
				
				// _|
				if(nextTile.type == 20)
				{
					dir = HAUT;
				}
				break;				
			}
			
			cur.next = new PathNode(nextTile, cur.distance + NODE_LENGTH, prevdir);
			cur = cur.next;
			prevdir = dir;
		}
	}
	
	public PathNode getStartNode()
	{
		return head;
	}
	
	
		
	public class PathNode
	{
		public PathNode next;
		public int distance;
		public Tile tile;
		public int dir;
		private int dirtype;
		private static final int rtol = 1;
		private static final int ltor = 2;
		private static final int utod = 3;
		private static final int dtou = 4;
		private static final int rtou = 5;
		private static final int rtod = 6;
		private static final int ltou = 7;
		private static final int ltod = 8;
		private static final int utol = 9;
		private static final int utor = 10;
		private static final int dtol = 11;
		private static final int dtor = 12;
		
		public PathNode(Tile tile, int distance, int dir)
		{
			next = null;
			this.tile = tile;
			this.distance = distance;
			
			switch(tile.type)
			{
			// --
			case 2:
			case 3:
			case 4:
			case 6:
				if(dir == DROITE)
				{
					dirtype = ltor;
				}
				else
				{
					dirtype = rtol;
				}
				break;
				
			// |
			case 10:
			case 5:
			case 7:
				if(dir == HAUT)
				{
					dirtype = dtou;
				}
				else
				{
					dirtype = utod;
				}
				break;
				
			// |-
			case 11:
				if(dir == GAUCHE)
				{
					dirtype = ltod;
				}
				else
				{
					dirtype = utor;
				}
				break;
				
			// -|
			case 12:
				if(dir == DROITE)
				{
					dirtype = rtod;
				}
				else
				{
					dirtype = utol;
				}
				break;
				
			// |_
			case 19:
				if(dir == GAUCHE)
				{
					dirtype = ltou;
				}
				else
				{
					dirtype = dtor;
				}
				break;
				
			// _|
			case 20:
				if(dir == DROITE)
				{
					dirtype = rtou;
				}
				else
				{
					dirtype = dtol;
				}
				break;
			}
		}
		
		public PathNode getNodeAtDistance(int distance)
		{
			int nbForward = (distance - this.distance) / NODE_LENGTH;
			PathNode node = this;
			
			for(int i = 0; i < nbForward; i++)
			{
				node = node.next;
			}
			
			return node;
		}
		
		public int getX(int distance)
		{
			int d = distance - this.distance;
			
			switch(dirtype)
			{
			case rtol:
				return xrtol(d);
			case ltor:
				return xltor(d);
			case utod:
				return xutod(d);
			case dtou:
				return xdtou(d);
			case rtou:
			case rtod:
				return xrtoud(d);
			case ltou:
			case ltod:
				return xltoud(d);
			case utol:
			case dtol:
				return xudtol(d);
			case utor:
			case dtor:
				return xudtor(d);
			}
			
			return 0;
		}
		
		public int getY(int distance)
		{
			int d = distance - this.distance;
			
			switch(dirtype)
			{
			case rtol:
				return yrtol(d);
			case ltor:
				return yltor(d);
			case utod:
				return yutod(d);
			case dtou:
				return ydtou(d);
			case rtou:
			case ltou:
				return ylrtou(d);
			case rtod:
			case ltod:
				return ylrtod(d);
			case dtor:
			case dtol:
				return ydtolr(d);
			case utol:
			case utor:
				return yutolr(d);
			}
			
			return 0;
		}
		
		private int xltor(int d)
		{
			return tile.screenRect.left + d;
		}
		
		private int xrtol(int d)
		{
			return tile.screenRect.right - d;
		}
		
		private int xutod(int d)
		{
			return tile.screenRect.centerX();
		}
		
		private int xdtou(int d)
		{
			return tile.screenRect.centerX();
		}
		
		private int xrtoud(int d)
		{
			if(d < MIDX)
			{
				return tile.screenRect.left + d;
			}
			else
			{
				return tile.screenRect.centerX();
			}
		}
		
		
		private int xltoud(int d)
		{
			if(d < MIDX)
			{
				return tile.screenRect.right - d;
			}
			else
			{
				return tile.screenRect.centerX();
			}
			
		}
		
		
		private int xudtol(int d)
		{
			if(d >= MIDX)
			{
				return tile.screenRect.right - d;
			}
			else
			{
				return tile.screenRect.centerX();
			}
			
		}
		
		private int xudtor(int d)
		{
			if(d >= MIDX)
			{
				return tile.screenRect.left + d;
			}
			else
			{
				return tile.screenRect.centerX();
			}
			
		}		
		
		private int  yrtol(int d)
		{
			return tile.screenRect.centerY();
		}
		
		private int  yltor(int d)
		{
			return tile.screenRect.centerY();
		}
		
		private int  yutod(int d)
		{
			return tile.screenRect.top + d;
		}
		
		private int  ydtou(int d)
		{
			return tile.screenRect.bottom - d;
		}
		
		private int  ylrtou(int d)
		{
			if(d >= MIDX)
			{
				return tile.screenRect.bottom - d;
			}
			else
			{
				return tile.screenRect.centerY();
			}
		}
		
		private int  ylrtod(int d)
		{
			if(d >= MIDX)
			{
				return tile.screenRect.top + d;
			}
			else
			{
				return tile.screenRect.centerY();
			}
		}
		
		private int  ydtolr(int d)
		{
			if(d < MIDX)
			{
				return tile.screenRect.top + d;
			}
			else
			{
				return tile.screenRect.centerY();
			}
		}
		
		private int  yutolr(int d)
		{
			if(d < MIDX)
			{
				return tile.screenRect.bottom - d;
			}
			else
			{
				return tile.screenRect.centerY();
			}
		}
		
		
	}
	
	
}
