package org.garyrusso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Find Artist Pairs - Uses a Phrase Index to Phrase Pairs.
 * 
 * Finding common "phrase pairs" is also known as co-occurrence.
 * 
 * In this case, a "word phrase" is a specific artist.
 * 
 * This program will read one or more UTF-8 encoded files. Each file contains lists of artists.
 * 
 * @author Gary Russo
 *
 */

public class FindPairs {

	/* phraseIndex is an Inverted Index.
	 * 
	 * An Inverted Index is a commonly used data structure used in natural language processors.
	 * In this case, its a phrase lookup table that maps all phrases (artists) to a specific list and to a position within the list.
	 * The hash key is the lower cased artist name which ensures uniqueness.
	 * 
	 * phrasePair is similar to phraseIndex but only stores the Artist Pair and the pair count.
	 * phrasePair hash key used is the combination of the 2 lower cased artist names in alphanumeric order.
	 * 
	 * phraseIndex list will be much smaller than the phrasePair index.
	 */
	
	private Map<String, List<PhraseTriple>> phraseIndex = new HashMap<String, List<PhraseTriple>>();
	private Map<String, PairTuple>          phrasePairs = new HashMap<String, PairTuple>();

	private int phraseListNum = 0;

	/**
	 * Inner Class
	 */
	private class PhraseTriple
	{
		private String phrase;
		private int listno;
		private int position;

		public PhraseTriple(String phrase, int listno, int position)
		{
			this.phrase   = phrase;
			this.listno   = listno;
			this.position = position;
		}
	}

	/**
	 * Inner Class
	 */
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

	/**
	 * Inner Class
	 */
	private class PairTupleComparator implements Comparator<PairTuple>
	{
	    @Override
	    public int compare(PairTuple o1, PairTuple o2)
	    {
	    	int retVal = o2.count - o1.count;
	    	
	    	if (retVal == 0)
	    	{
	    		retVal = o1.pair.compareToIgnoreCase(o2.pair);
	    	}
	    	
	    	//System.out.println("compare1: " + (o2.count - o1.count));
	    	//System.out.println("compare2: " + o1.pair.compareToIgnoreCase(o2.pair));
	    	//System.out.println("compare: " + o1.pair + " | " + o2.pair);
	    	
	        return retVal;
	    }
	}
	
    /**
     * Default No Argument Constructor
     */
    public FindPairs() {
    }

	/**
	 * Indexer
	 */
	private void indexFile(File file) throws IOException
	{
		int pos = 0;

		BufferedReader reader = new BufferedReader(new FileReader(file));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			phraseListNum++;
			//System.out.println(phraseListNum + ". " + line);

			for (String _phrase : line.split(","))
			{
				String phrase = _phrase.toLowerCase();
				pos++;

				//System.out.println(_phrase);

				List<PhraseTriple> idx = phraseIndex.get(phrase);

				if (idx == null)
				{
					idx = new LinkedList<PhraseTriple>();
					
					//hash key is the lower cased phrase (artist).
					phraseIndex.put(phrase, idx);
				}

				idx.add(new PhraseTriple(_phrase, phraseListNum, pos));
			}
		}
	}

	/**
	 * List Count Getter
	 */
	private int getPhraseListCount()
	{
		return phraseListNum;
	}
	
	/**
	 * Hash Key Generator
	 */
	private String createHashKey(String str1, String str2)
	{
		
		String key = "";
		
		String str1NoSpace = str1.replace(' ','-').toLowerCase();
		String str2NoSpace = str2.replace(' ','-').toLowerCase();
		
		if (str1NoSpace.compareTo(str2NoSpace) > 0)
			key = str2NoSpace + "-" + str1NoSpace;
		else
			key = str1NoSpace + "-" + str2NoSpace;
		
		return key;
	}
	
	/**
	 *
	 */
	private void buildPairsListById(int id)
	{
		List<String> phrases = new ArrayList<String>();
		
		// temporary sub-index because it's the list of phrases in one list.
		for (Map.Entry<String, List<PhraseTriple>> entry : phraseIndex.entrySet())
		{
		    for (PhraseTriple t : entry.getValue())
		    {
		    	if (t.listno == id)
			    {
				   	phrases.add(t.phrase);
			    }
		    }
		}
		
		// Sort to so that the generated hash key is in alphanumeric order
		Arrays.sort(phrases.toArray());
		
	    List<String> list = createPairsList(id, phrases);
	}

	/**
	 *
	 */
	private void addPairsHelper(int listId, List<String> phrases)
	{
		String pairKey = "";
		
		int phrasedIdx1 = 0;
		int phrasedIdx2 = 0;
		
		for (int i = 0; i < phrases.size()-1; i++)
		{
			phrasedIdx1 = 0;
			phrasedIdx2 = i + 1;
			
			pairKey = createHashKey(phrases.get(phrasedIdx1), phrases.get(phrasedIdx2));
		
			PairTuple pairTuple = phrasePairs.get(pairKey);
			
			// Sort the original phrase to be consistent.
			String phrase1 = phrases.get(phrasedIdx1);
			String phrase2 = phrases.get(phrasedIdx2);

			String pair = "";
			
			if (phrase1.toLowerCase().compareTo(phrase2.toLowerCase()) > 0)
				pair = phrase2 + ", " + phrase1;
			else
				pair = phrase1 + ", " + phrase2;
			
			//System.out.println("list " + listId + " : phrase: " + pair + " : pairKey: " + pairKey);

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
	
	/**
	 *
	 */
	private List<String> createPairsList(int id, List<String> list)
	{
	    //for (String p : list)
	    //{
		//    System.out.println("rl: " + list.size() + " : phrase: " + p);
	    //}
		
	    if (list.size() == 1) 
	         return list;

		addPairsHelper(id, list);
		
	    return createPairsList(id, list.subList(1, list.size()));
	}
	
	/**
	 *
	 */
	private void displayResults(String[] args)
	{
		NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);
		
		System.out.println("");
		for (int i = 0; i < args.length; i++)
		{
			System.out.println("indexed file:      " + args[i]);
		}
		
		System.out.println("");
		System.out.println("artist list count: " + String.format("%1$" + 7 + "s", numberFormatter.format(getPhraseListCount())));
		System.out.println("artist count:      " + String.format("%1$" + 7 + "s", numberFormatter.format(phraseIndex.size())));
		System.out.println("artist pair count: " + String.format("%1$" + 7 + "s", numberFormatter.format(phrasePairs.size())));
		System.out.println("");
		
		printPairsList();
	}

	/**
	 *
	 */
	private void printPairsList()
	{
		List<PairTuple> pairTuples = new ArrayList<PairTuple>();
		
		for (HashMap.Entry<String, PairTuple> pairEntry : phrasePairs.entrySet())
		{
			pairTuples.add(pairEntry.getValue());
		}
		
		Collections.sort(pairTuples, new PairTupleComparator());

		for (PairTuple t : pairTuples)
		{
			if (t.count > 49)
			{
				printUtf8Line(t.pair + " : " + t.count);
			}
		}
	}

	/**
	 *
	 */
	private void printUtf8Line(String out)
	{
        PrintStream ps;
        
		try
		{
			ps = new PrintStream(System.out, true, "UTF-8");
	        //System.out.println(out);
	        ps.println(out);
		}
		catch (UnsupportedEncodingException e)
		{
			System.out.println("Unsupported UTF-8 code");
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public static void main(String[] args)
	{
		/*
		 * Read phrase lists
		 * For each list, create unique list of phrase pairs
		 * For each list, compare list of phrase pairs
		 * 
		 * */
		
		//String[] args = { "C:/projects/knewton/docs/Artist_lists_small.txt" };
		//String[] args = { "C:/projects/knewton/docs/test1.txt" };

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
			
			// Pass 3 - print Pair Phrases (artist pairs) to stdout.
			idx.displayResults(args);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

