package com.jfboily.gtd;

import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class GTDProfile 
{
	public  int curLevel = 0;
	public  String curLevelFName = null;
	
	public  HighScore[] highScores = new HighScore[10];
	
	public  String playerName = "jfboily";
	
	
	public class HighScore
	{
		public String name;
		public int level;
		public int score;
	}
	
	
	public  void save(String fname)
	{
		String res;
		
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try
		{
			serializer.setOutput(writer);
			serializer.startDocument("utf-8", true);
			serializer.startTag("", "LektroData");
			
			// player tag
			serializer.startTag("", "Player");
				// name
				serializer.attribute("", "name", playerName);
				// curLevel
				serializer.attribute("", "lastLevel", String.valueOf(curLevel));
			serializer.endTag("", "Player");
			
			// high scores
			serializer.startTag("", "HighScores");
			for(int i = 0; i < 10; i++)
			{
				serializer.startTag("", "Score");
					serializer.attribute("", "name", highScores[i].name);
					serializer.attribute("", "level", String.valueOf(highScores[i].level));
					serializer.attribute("", "score", String.valueOf(highScores[i].score));
				serializer.endTag("", "Score");
			}
			serializer.endTag("", "HighScores");
			
			serializer.endTag("", "LektroData");
			serializer.endDocument();
			
			res = writer.toString();
			Log.d("XML PROFILE", res);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public  void load(String fname)
	{
		for(int i = 0; i < 10; i++)
		{
			highScores[i] = new HighScore();
			highScores[i].name = "jfboily"+i;
			highScores[i].level = i;
			highScores[i].score = i * 1000;
		}
		
		playerName = "jfboily";
		curLevel = 0;
		curLevelFName = "level1.xml";
	}
	
}
