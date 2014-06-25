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

	Map<String, List<PhraseTuple>> phraseIndex = new HashMap<String, List<PhraseTuple>>();
	Map<String, PairTuple>         phrasePairs = new HashMap<String, PairTuple>();
	
	private int phraseListNum = 0;

	private class PhraseTuple
	{
		private int listno;
		private int position;

		public PhraseTuple(int listno, int position)
		{
			this.listno = listno;
			this.position = position;
		}
	}

	private class PairTuple
	{
		private String pair;
		private int count;

		public PairTuple(String pair, int count)
		{
			this.pair = pair;
			this.count = count;
		}
	}

	public void indexFile(File file) throws IOException
	{
		int pos = 0;

		BufferedReader reader = new BufferedReader(new FileReader(file));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			phraseListNum++;
			System.out.println(phraseListNum + ". " + line);

			//for (String _phrase : line.split("\\W+"))
			for (String _phrase : line.split(","))
			{
				String phrase = _phrase.toLowerCase();
				pos++;

				//System.out.println(_phrase);

				List<PhraseTuple> idx = phraseIndex.get(phrase);

				if (idx == null)
				{
					idx = new LinkedList<PhraseTuple>();
					phraseIndex.put(phrase, idx);
				}

				idx.add(new PhraseTuple(phraseListNum, pos));
			}
		}

		System.out.println("\nindexed " + file.getPath() + " " + pos + " phrases");
	}

	public void search(List<String> phrases)
	{
		for (String _phrase : phrases)
		{
			String phrase = _phrase.toLowerCase();
			List<PhraseTuple> idx = phraseIndex.get(phrase);
			
			System.out.println("\nsearch phrase: " + phrase);
			System.out.println("index size: " + idx.size());
			System.out.println("phrase list size: " + phraseIndex.size());

			if (idx != null)
			{
				for (PhraseTuple t : idx)
				{
					System.out.println("phrase: " + _phrase + " | position: " + t.position + " | listno: " + t.listno);
				}
			}
		}
	}

	public void createPairsList()
	{
		for (Map.Entry<String, List<PhraseTuple>> entry : phraseIndex.entrySet())
		{
		    for (PhraseTuple t : entry.getValue())
		    {
			   	System.out.println(entry.getKey() + " / " + t.listno + " / " + t.position);
		    }
		}
	}

	public int getPhraseListCount()
	{
		return phraseListNum;
	}
	
	public void printPairsList()
	{
		List<String> sortedPairs = new ArrayList<String>();
				
		for (HashMap.Entry<String, PairTuple> pairEntry : phrasePairs.entrySet())
		{
			sortedPairs.add(pairEntry.getKey());
			System.out.println("111: " + pairEntry.getKey());
			//System.out.println(pairEntry.getKey() + " / " + pairEntry.getValue().pair + " / " + pairEntry.getValue().count);
		}
		
		Arrays.sort(sortedPairs.toArray());
		
		for (String key : sortedPairs)
		{
			System.out.println(key + " / " + phrasePairs.get(key).pair + " / " + phrasePairs.get(key).count);
		}
	}
	
	public String createHashKey(String str1, String str2) {
		
		String key = "";
		
		String str1NoSpace = str1.replace(' ','-');
		String str2NoSpace = str2.replace(' ','-');
		
		if (str1NoSpace.compareTo(str2NoSpace) > 0)
			key = str2NoSpace + "|" + str1NoSpace;
		else
			key = str1NoSpace + "|" + str2NoSpace;
		
		return key;
	}
	
	public void buildPairsListById(int id)
	{
		List<String> phrases = new ArrayList<String>();
		
		// temporary sub-index because it's the list of phrases in 1 list.
		for (Map.Entry<String, List<PhraseTuple>> entry : phraseIndex.entrySet())
		{
		    for (PhraseTuple t : entry.getValue())
		    {
		    	if (t.listno == id)
			    {
				   	phrases.add(entry.getKey());
			    }
		    }
		}
		
		// Sort to so that the generated hash key is in alphanumeric order
		Arrays.sort(phrases.toArray());
		for (String phrase : phrases)
		{
			System.out.println("list " + id + " : phrase: " + phrase);
		}
		
	    List<String> list = createPairsList(id, phrases);
	    
		System.out.println("list size: " + list.size());
	}

	public void addPairsHelper(int listId, List<String> phrases)
	{
		String pairKey = "";
		
		int phrasedIdx1 = 0;
		int phrasedIdx2 = 0;
		
		for (int i = 0; i < phrases.size(); i++)
		{
			if (i == phrases.size()-1)
			{
				phrasedIdx1 = 0;
				phrasedIdx2 = i;
			}
			else
			{
				phrasedIdx1 = 0;
				phrasedIdx2 = i + 1;
			}
			
			pairKey = createHashKey(phrases.get(phrasedIdx1), phrases.get(phrasedIdx2));
		
			PairTuple pairTuple = phrasePairs.get(pairKey);
			
			String pair = phrases.get(phrasedIdx1) + ", " + phrases.get(phrasedIdx2);
	
			System.out.println("list " + listId + " : phrase: " + pair + " : pairKey: " + pairKey);

			if (pairTuple == null)
			{
				phrasePairs.put(pairKey, new PairTuple(pair, 1));
			}
			else
			{
				phrasePairs.put(pairKey, new PairTuple(pair, pairTuple.count + 1));
			}
		}
	}
	
	public List<String> createPairsList(int id, List<String> list)
	{
	    for (String p : list)
	    {
		    System.out.println("rl: " + list.size() + " : phrase: " + p);
	    }
		
	    if (list.size() == 1) 
	         return list;

		addPairsHelper(id, list);
		
	    return createPairsList(id, list.subList(1, list.size()));
	}

	public static void main(String[] args1)
	{
		/*
		 * Read phrase lists
		 * For each list, create unique list of phrase pairs
		 * For each list, compare list of phrase pairs
		 * 
		 * */
		
		String[] args = { "C:/projects/knewton/docs/test3.txt" };

		try
		{
			FindPairs idx = new FindPairs();
			
			// Pass 1 - read file and create phrase index
			for (int i = 0; i < args.length; i++)
			{
				idx.indexFile(new File(args[i]));
			}
			
			// Pass 2 - create Pairs Index using phrase index
			for (int i = 1; i < idx.getPhraseListCount() + 1; i++)
			{
				idx.buildPairsListById(i);
			}
			
			System.out.println("phrasePair size: " + idx.phrasePairs.size());
			
			idx.printPairsList();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

