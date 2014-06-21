package org.garyrusso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Find Pairs - Co-occurence
 *
 */

public class FindPairs {

	Map<String, List<Tuple>> wordIndex = new HashMap<String, List<Tuple>>();
	Map<String, Integer>     wordPairs = new HashMap<String, Integer>();
	
	private int wordListNum = 0;

	private class Tuple
	{
		private int listno;
		private int position;

		public Tuple(int listno, int position)
		{
			this.listno = listno;
			this.position = position;
		}
	}

	public void indexFile(File file) throws IOException
	{
		int pos = 0;

		BufferedReader reader = new BufferedReader(new FileReader(file));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			wordListNum++;
			System.out.println(wordListNum + ". " + line);

			//for (String _word : line.split("\\W+"))
			for (String _word : line.split(","))
			{
				String word = _word.toLowerCase();
				pos++;

				//System.out.println(_word);

				List<Tuple> idx = wordIndex.get(word);

				if (idx == null)
				{
					idx = new LinkedList<Tuple>();
					wordIndex.put(word, idx);
				}

				idx.add(new Tuple(wordListNum, pos));
			}
		}

		System.out.println("\nindexed " + file.getPath() + " " + pos + " words");
	}

	public void search(List<String> words)
	{
		for (String _word : words)
		{
			String        word = _word.toLowerCase();
			List<Tuple>    idx = wordIndex.get(word);
			
			System.out.println("\nsearch word: " + word);
			System.out.println("index size: " + idx.size());
			System.out.println("word list size: " + wordIndex.size());

			if (idx != null)
			{
				for (Tuple t : idx)
				{
					System.out.println("word: " + _word + " | position: " + t.position + " | listno: " + t.listno);
				}
			}
		}
	}

	public void createPairsList()
	{
		for (Map.Entry<String, List<Tuple>> entry : wordIndex.entrySet())
		{
		    for (Tuple t : entry.getValue())
		    {
			   	System.out.println(entry.getKey() + " / " + t.listno + " / " + t.position);
		    }
		}
	}

	public int getWordListCount()
	{
		return wordListNum;
	}
	
	public void printPairsList()
	{
		for (HashMap.Entry<String, Integer> pairEntry : wordPairs.entrySet())
		{
			System.out.println(pairEntry.getKey() + " / " + pairEntry.getValue());
		}
	}
	
	public void buildPairsListById(int id)
	{
		List<String> words = new ArrayList<String>();
		
		for (Map.Entry<String, List<Tuple>> entry : wordIndex.entrySet())
		{
		    for (Tuple t : entry.getValue())
		    {
		    	if (t.listno == id)
			    {
				   	words.add(entry.getKey());
			    }
		    }
		}
		
		String pairKey = "";
		
		for (int i = 0; i < words.size(); i++)
		{
			if (i == words.size()-1)
				pairKey = words.get(0) + "-" + words.get(words.size()-1);
			else
				pairKey = words.get(i) + "-" + words.get(i+1);
		
			Integer pairCount = wordPairs.get(pairKey);
	
			if (pairCount == null)
			{
				wordPairs.put(pairKey, 1);
			}
			else
			{
				wordPairs.put(pairKey, pairCount + 1);
			}
		}
	}

	public static void main(String[] args1)
	{
		/*
		 * Read word lists
		 * For each list, create unique list of word pairs
		 * For each list, compare list of word pairs
		 * 
		 * */
		
		String[] args = { "C:/projects/knewton/docs/test2.txt" };

		try
		{
			FindPairs idx = new FindPairs();

			for (int i = 0; i < args.length; i++)
			{
				idx.indexFile(new File(args[i]));
			}
			
			for (int i = 1; i < idx.getWordListCount() +1; i++)
			{
				idx.buildPairsListById(i);
			}
			
			System.out.println("wordPair size: " + idx.wordPairs.size());
			
			idx.printPairsList();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

