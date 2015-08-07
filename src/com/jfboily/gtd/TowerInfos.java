package com.jfboily.gtd;

public class TowerInfos 
{	
	public enum FireType
	{
		BULLET,
		RADIUS,
	}
	
	public String name;
	public String turretSprite;
	public String bulletSprite;
	public String hitSprite;
	public int damage;
	public int cost;
	public int sellValue;
	public int radius;
	public int fireDelay;
	public int fireSound;
	public FireType fireType;
	

	
	public TowerInfos(String name, String turretSprite, String bulletSprite, String hitSprite, int fireSound, int fireDelay, FireType fireType, int damage, int radius, int cost, int sellValue)
	{
		this.name = name;
		this.turretSprite = turretSprite;
		this.bulletSprite = bulletSprite;
		this.hitSprite = hitSprite;
		this.damage = damage;
		this.cost = cost;
		this.sellValue = sellValue;
		this.radius = radius;
		this.fireDelay = fireDelay;
		this.fireSound = fireSound;
		this.fireType = fireType;
	}
}
