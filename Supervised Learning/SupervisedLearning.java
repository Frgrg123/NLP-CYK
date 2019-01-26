import java.util.*;


/**
 * @author William Jiang
 *
 */
public class SupervisedLearning {
	
	/** Static data: 2D String ArrayList for grammar, HashMap linking Words to parts of speech for Lexicon, Hashmaps to store the total amount of times parts of speech
	 * or non-terminals appear, a chart as needed for CYKParse, a static int for length of current line, and an ArrayList of Strings containing CYKParse results.
	 * In addition, there are static int variables for number of correct and incorrect parses.
	*/
	static ArrayList<ArrayList<String>> gram = new ArrayList<ArrayList<String>>();
	static HashMap<String, ArrayList<String[]>> lexicon = new HashMap<String, ArrayList<String[]>>();
	static HashMap<String, Integer> speechFreq = new HashMap<String, Integer>();
	static HashMap<String, Integer> expressionFreq = new HashMap<String, Integer>();
	static HashMap<index, treeNode> chart = new HashMap<index, treeNode>();
	static int leng;
	static ArrayList<String> Parses = new ArrayList<String>();
	
	static HashMap<ArrayList<String>, Integer> grammarCount = new HashMap<ArrayList<String>, Integer>();
	
	
	static int correct = 0;
	static int wrong = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//Get command line input for number of lines to train on, as well as whether or not to print detailed input
		int training = Integer.parseInt(args[0]);
		boolean flag = Boolean.parseBoolean(args[1]);
		
		
		//Create scanner for user input
		Scanner input = new Scanner(System.in);
		String line = input.nextLine();
		
		//For loop to learn from specified amount of sentences
		for(int i = 0; i < training; i++) {
			String[] lineSplit = line.split(" ");
		
			learnSentence(lineSplit);
			
			line = input.nextLine();
			
		}
		
		//Adds correct probabilities for grammar and lexicon by taking occurrences of specific rules and dividing by total occurrences of speech/nonterminals
		for(int i = 0; i < gram.size(); i++) {
			double count = 0;
			double k = (double) grammarCount.get(gram.get(i));
			double n = (double) expressionFreq.get(gram.get(i).get(0));
			count = k/n;
			count = ((int) (count*100+0.5))/100.0;
			gram.get(i).add(Double.toString(count));
		}	
		for(String key : lexicon.keySet()) {
			ArrayList<String[]> a = lexicon.get(key);
			for(int i = 0;i < a.size(); i++) {
				double k = (double) Integer.parseInt(a.get(i)[1]);
				double n = (double) speechFreq.get(a.get(i)[0]);
				double count = k/n;
				count = ((int) (count*100+0.5))/100.0;
				a.get(i)[1] = Double.toString(count);
			}
		}
		
		//Get lines to use CYKParse on
		while(!line.equals("end input")) {
			CYKParse(line);
			
			
			//Return highest prob tree
			
			treeNode root = chart.get(new index("S", 0, leng - 1));
			
			//Converts probability tree to an arraylist of sentence structure similar to input
			ArrayList<String> cykResult = convertToLine(root);
			
			
			String[] lineSplit = line.split(" ");			
			
			//ArrayLists for actual sentence structure as specified in input and words in the input line
			ArrayList<String> actual = new ArrayList<String>(Arrays.asList(lineSplit));
			ArrayList<String> wordList = new ArrayList<String>();
			
			//Populates aforementioned arraylists
			for(int i = 0; i < actual.size(); i++) {
				if(!actual.get(i).contains("*") && !actual.get(i).contains("+")) {
					wordList.add(actual.get(i));
					actual.remove(i);
				}
			}
			
			//Cleans actual sentence structure to compare with CYKParse result
			for(int i = 0; i < actual.size(); i++) {
				actual.set(i, actual.get(i).replace("*", ""));
				actual.set(i, actual.get(i).replace("+", ""));
			}
			
			//Adds parse results to an arraylist in same format as input to be printed if necessary
			String lineParse = "";
			if(cykResult.isEmpty()) {
				Parses.add("This sentence cannot be parsed. ");
			}
			else {
				for(int i = 0; i < cykResult.size(); i++) {
					if(speechFreq.containsKey(cykResult.get(i))) {
						lineParse = lineParse.concat("+" + cykResult.get(i) + " ");
						lineParse = lineParse.concat(wordList.remove(0) + " ");
					}
					else if(expressionFreq.containsKey(cykResult.get(i))) {
						lineParse = lineParse.concat("*" + cykResult.get(i) + " ");
					}
				}
				
				Parses.add(lineParse);
			}
			
			
			//Checks if CYKParse result is the same as actual and records results in variables
			if(actual.equals(cykResult)) {
				correct++;
				Parses.set(Parses.size() - 1, Parses.get(Parses.size() - 1) + "Right");
			}
			else {
				wrong++;
				Parses.set(Parses.size() - 1, Parses.get(Parses.size() - 1) + "Wrong");
			}
			
			//Get next line and clear chart of CYK
			line = input.nextLine();
			chart.clear();
			
		}
		
		//Print out Grammar and Lexicon in a relatively sorted format, printing out rules by category
		System.out.println("Grammar");
		for(ArrayList<String> sList : gram) {
			if(sList.get(0).equals("S")) {
				System.out.println(sList.get(0) + " -> " + sList.get(1) + " " + sList.get(2) + "[" + sList.get(3) + "]");
			}
		}
		for(String g : expressionFreq.keySet()) {
			for(ArrayList<String> sList : gram) {
				if(g.equals("S")) {
					continue;
				}
				if(sList.get(0).equals(g)) {
					System.out.println(sList.get(0) + " -> " + sList.get(1) + " " + sList.get(2) + "[" + sList.get(3) + "]");
				}
			}
		}
		System.out.println();
		System.out.println("Lexicon");
		for(String speech : speechFreq.keySet()) {
			for(String s : lexicon.keySet()) {
				ArrayList<String[]> sArrayList = lexicon.get(s);
				for(int i = 0; i < sArrayList.size(); i++) {
					String[] sArray = sArrayList.get(i);
					if(sArray[0].equals(speech)) {
						System.out.println(sArray[0] + " -> " + s + " [" + sArray[1] + "]");
					}
				}
			}
		}
		System.out.println();
		
		//If flag was set in command line, prints out CYK parses
		if(flag) {
			System.out.println("Parses");
			for(int i = 0; i < Parses.size(); i++) {
				System.out.println(Parses.get(i));
			}
			System.out.println();
		}
		
		//Print out summmary of program run
		double accuracy = ((double) correct) / ((double) (correct + wrong));
		System.out.println("Accuracy: The parser was tested on " + (correct+wrong) + " sentences. It got " + correct + " right, for an accuracy of " + accuracy);
		
		input.close();
		
	}
	
	//Recursive method to learn from a specified sentence.
	public static int learnSentence(String[] s) {
		//Variables for number of children found, an arraylist for grammar rule found, and an index to return
		int children = 0;
		ArrayList<String> grammarLine = new ArrayList<String>();
		int index = 1;
		
		//Adds initial nonterminal to grammar rule and adds to total occurrences of that nonterminal
		s[0] = s[0].replace("*", "");
		grammarLine.add(s[0]);
		if(expressionFreq.containsKey(s[0])) {
			expressionFreq.put(s[0], expressionFreq.get(s[0]) + 1);
		}
		else {
			expressionFreq.put(s[0], 1);
		}
		
		//While 2 children of the current nonterminal has not been found, the program will loop through the lines and read the correct grammar rule
		while(children != 2) {
			String cleaned = s[index].replace("*", "");
			cleaned = cleaned.replace("+", "");
			grammarLine.add(cleaned);
			children++;
			//if another nonterminal is the child, make recursive call
			if(s[index].contains("*")) {
				index += learnSentence(Arrays.copyOfRange(s, index, s.length));
				
			}
			else {
				//else must be part of speech, so add word and lexicon rule to lexicon
				String[] n = new String[2];
				n[0] = s[index];
				n[0] = n[0].replace("+", "");
				n[1] = "1";
				String word = s[index+1];
				
				if(speechFreq.containsKey(n[0])) {
					speechFreq.put(n[0], (speechFreq.get(n[0]) + 1));
				}
				else {
					speechFreq.put(n[0], 1);
				}
				
				//add to lexicon if not previously in, else add to occurrences of this rule
				if(lexicon.containsKey(word)) {
					boolean contained = false;
					for(int i = 0; i < lexicon.get(word).size(); i++) {
						if(lexicon.get(word).get(i)[0].equals(n[0])) {
							int temp = Integer.parseInt(lexicon.get(word).get(i)[1]);
							temp += 1;
							lexicon.get(word).get(i)[1] = Integer.toString(temp);
							contained = true;
							break;
						}
					}
					if(!contained) {
						lexicon.get(word).add(n);
					}			
				}
				else {
					ArrayList<String[]> newList = new ArrayList<String[]>();
					newList.add(n);
					lexicon.put(word, newList);
				}

				index += 2;
			}
		}
		
		//Add grammar rule to grammar arraylist
		if(grammarCount.containsKey(grammarLine) ) {
			grammarCount.put(grammarLine, grammarCount.get(grammarLine) + 1);
		}
		else {
			grammarCount.put(grammarLine, 1);
			gram.add(grammarLine);
		}
		
		return index;
		
	}
	
	//Basically same CYKParse method as Programming Assignment 1, only difference is that nonterminals are no longer in a enumerated class and are instead taken from learning
	public static void CYKParse(String s) {
		
		//Create nodes for lexicon
		
		String[] sentence = s.split(" ");
		
		ArrayList<String> t = new ArrayList<String>(Arrays.asList(sentence));
		
		for(int i = 0; i < t.size(); i++) {
			while(t.get(i).contains("*") || t.get(i).contains("+")) {
				t.remove(i);
				if(i > t.size()) {
					break;
				}
			}
		}
		
		sentence = new String[t.size()];
		
		sentence = t.toArray(sentence);
		
		leng = sentence.length;
		
		for(int i = 0; i < leng; i++) {
			String word = sentence[i];
			if(!lexicon.containsKey(word)) {
				return;
			}
			for(int j = 0; j < lexicon.get(word).size(); j++) {
				String[] currentPhrase = lexicon.get(word).get(j);
				chart.put(new index(currentPhrase[0], i, i), new treeNode(currentPhrase[0], i, i, word, null, null, Double.parseDouble(currentPhrase[1])));
			}
		}
		
		//Create and calculate nodes for grammar rules, follows pseudocode provided in class
		
		for(int l = 1; l < leng; l++) {
			for(int i = 0; i < (leng - l); i++) {
				int temp = i + l;
				for(String mc : expressionFreq.keySet()) {
					if(chart.get(new index(mc, i, temp)) == null) {
						chart.put(new index(mc, i, temp), new treeNode(mc, i, temp, null, null, null, 0.0));
					}
					for(int k = i; k < (temp); k++) {
						for(int h = 0; h < gram.size(); h++) {
							if(gram.get(h).get(0).equals(mc)) {
								double leftP, rightP;
								if(chart.get(new index(gram.get(h).get(1), i, k)) == null) {
									leftP = 0.0;
								}
								else {
									leftP = chart.get(new index(gram.get(h).get(1), i, k)).probability;
								}
								if(chart.get(new index(gram.get(h).get(2), k+1, temp)) == null) {
									rightP = 0.0;
								}
								else {
									rightP = chart.get(new index(gram.get(h).get(2), k+1, temp)).probability;
								}
								
								double newProb = leftP * rightP * Double.parseDouble(gram.get(h).get(3));
								treeNode curr = chart.get(new index(mc, i, temp));
								if(newProb > curr.probability) {
									curr.left = chart.get(new index(gram.get(h).get(1), i, k));
									curr.right = chart.get(new index(gram.get(h).get(2), k+1, temp));
									curr.probability = newProb;
									
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void printTree(treeNode node, int indent) {
		//Prints out tree data in correct format with necessary indents
		if(node != null) {
			if(node.probability == 0.0) {
				System.out.println("This sentence cannot be parsed");
				return;
			}
			for(int i = 0; i < indent; i++) {
				System.out.print(" ");
			}
			System.out.print(node.phrase);
			if(node.word != null) {
				System.out.print(" " + node.word);
			}
			System.out.println();
			printTree(node.left, indent+3);
			printTree(node.right, indent+3);
		}
	}
	
	//wrapper method to convery probability tree to an arraylist containing sentence structure via a preorder traversal
	public static ArrayList<String> convertToLine(treeNode root) {
		
		ArrayList<String> s = preorderAdd(root);	
		
		return s;
		
	}
	
	//basic preorder traversal
	static ArrayList<String> preorderAdd(treeNode node) 
    { 
        if (node == null) 
            return new ArrayList<String>(); 
        
        ArrayList<String> s = new ArrayList<String>();
        
        s.add(node.phrase);
  
        s.addAll(preorderAdd(node.left));
  
        s.addAll(preorderAdd(node.right));
        
        return s;
    } 

}

//treeNode class containing data for parse
class treeNode {
	String phrase;
	int start, end;
	String word;
	treeNode left;
	treeNode right;
	double probability;
	
	//Basic constructor with all necessary arguments to create node
	public treeNode(String p, int startIndex, int endIndex, String w, treeNode l, treeNode r, double prob) {
		this.phrase = p;
		this.start = startIndex;
		this.end = endIndex;
		this.word = w;
		this.left = l;
		this.right = r;
		this.probability = prob;
	}
	
}

//Class used as index for treeNodes, use in mapping phrases to Nodes in a hashmap
class index {
	String M;
	int startIndex;
	int endIndex;
	
	//Basic constructor
	public index(String index1, int index2, int index3) {
		M = index1;
		startIndex = index2;
		endIndex = index3;
	}
	
	//Equals override allowing comparison of indexes based on class data
	@Override
	public boolean equals(Object obj)
	{
		  if(obj == null) {
			  return false;
		  }
		  if(!(obj instanceof index)) {
			  return false;
		  }
		  index other = (index) obj;
		  if(this.M.equals(other.M) && this.startIndex == other.startIndex && this.endIndex == other.endIndex) {
			  return true;
		  }
		  else {
			  return false;
		  }
	}
	
	//hashCode override using hash of string and index data
	@Override
	public int hashCode() {
		String code = Integer.toString(startIndex);
		code.concat(Integer.toString(endIndex));
		//System.out.println(code + " p1");
		code = code + M.toString();
		//System.out.println(Integer.toString(M.hashCode()));
		//System.out.println(code + " p2");
		return code.hashCode();
		
	}
}
