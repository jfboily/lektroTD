package com.jfboily.gtd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

import com.jfboily.fxgea.Game;
import com.jfboily.fxgea.Screen;


public class Level
{

	public static enum TYPE
	{
		DEFENSE,
		SURVIE,
		COLLECTE,
	}
	
	private int nbWaves;
	private Creep[][] waves;
	private int[] waveCash;
	private TYPE type;
	private String name;
	private GTDScreen screen;
	private String fnameTilemap;
	private int lives;
	private int cash;
	private int repeats;
	
	public Level(String fname, GTDScreen screen)
	{	
		this.screen = screen;
		loadXML(fname);
	}
	

	private void loadXML(String fname)
	{
		try
		{
			InputStream is = Game.getGame().getAssets().open(fname);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
				
				
				public InputSource resolveEntity(String publicId, String systemId)
						throws SAXException, IOException {
					// TODO Auto-generated method stub
					return new InputSource(new ByteArrayInputStream(new byte[0]));
				}
			});
			
			Document doc = builder.parse(is);
			
			// le docElement = tag 'level'
			Element docElement = doc.getDocumentElement();
			
			
			// attributs du level
			name = docElement.getAttribute("name");
			String t = docElement.getAttribute("type");
			fnameTilemap = docElement.getAttribute("tilemap");
			cash = Integer.parseInt(docElement.getAttribute("cash"));
			lives = Integer.parseInt(docElement.getAttribute("lives"));
			repeats = Integer.parseInt(docElement.getAttribute("waverepeats"));
		
			if(t.equals("defense"))
			{
				type = TYPE.DEFENSE;
			}
			else if(t.equals("survie"))
			{
				type = TYPE.SURVIE;
			}
			else
			{
				type = TYPE.COLLECTE;
			}
			
			// load les waves
			NodeList waveNodes = docElement.getElementsByTagName("wave");
			
			nbWaves = waveNodes.getLength() * repeats;
			waves = new Creep[nbWaves][];
			waveCash = new int[nbWaves];
			
			int wavesOrig = waveNodes.getLength();
			
			for(int r = 0; r < repeats; r++)
			{
				// waves 'originales' (pas repetees)
				for(int w = 0; w < waveNodes.getLength(); w++)
				{
					int indexW = w + (r * wavesOrig);
					
					Element curWave = (Element)waveNodes.item(w);
					waveCash[indexW] = Integer.parseInt(curWave.getAttribute("points"));
					
					NodeList creepNodes = curWave.getElementsByTagName("creep");
					int nbCreeps = creepNodes.getLength();
					waves[indexW] = new Creep[nbCreeps];
					
					for(int c = 0; c < nbCreeps; c++)
					{
						int type, distance, life, speed;
						
						Element curCreep = (Element)creepNodes.item(c);
						
						type = Integer.parseInt(curCreep.getAttribute("type"));
						distance = Integer.parseInt(curCreep.getAttribute("distance"));
						life = Integer.parseInt(curCreep.getAttribute("life"));
						life += (life * 1.25 * r);
						speed = Integer.parseInt(curCreep.getAttribute("speed"));
						waves[indexW][c] = new Creep(type, distance, life, speed, screen);
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.e("Level:loadXML", "Exception au parse du fichier XML ("+e.getMessage()+")");
		}
	}
	
	public int getNbWaves()
	{
		return nbWaves;
	}
	
	public Creep[] getWave(int waveIndex)
	{
		return waves[waveIndex];
	}

	public String getTilemapName()
	{
		return fnameTilemap;
	}
	
	public int getStartCash()
	{
		return cash;
	}
	
	public int getLives()
	{
		return lives;
	}
	
	public TYPE getType()
	{
		return type;
	}
	
	public int getWaveCash(int waveIndex)
	{
		return waveCash[waveIndex];
	}

}
