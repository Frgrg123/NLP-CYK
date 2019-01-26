package parser;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CYK {
	
	//Create fields for grammar, lexicon, chart, and value for sentence length
	static ArrayList<ArrayList<String>> gram = new ArrayList<ArrayList<String>>();
	static HashMap<String, ArrayList<String[]>> lexicon = new HashMap<String, ArrayList<String[]>>();
	static HashMap<index, treeNode> chart = new HashMap<index, treeNode>();
	static int leng;

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		//Read file containing grammar and lexicon
		
		File grammar = new File("grammar.txt");
		
		BufferedReader br = new BufferedReader(new FileReader(grammar));
		
		String line;
		
		
		//Read grammar into arraylist
		
		while((line = br.readLine()) != null) {
			if(line.length() > 1) {
				String[] g = line.split(" ");
				ArrayList<String> lineList = new ArrayList<String>();
				lineList.add(g[0]);
				lineList.add(g[2]);
				lineList.add(g[3]);
				g[4] = g[4].replaceAll("[\\(\\)\\[\\]\\{\\}]","");
				lineList.add(g[4]);
				gram.add(lineList);
			}
			else {
				break;
			}
			
		}
		
		
		//Read lexicon into arraylist
		
		while((line = br.readLine()) != null) {
			String[] l = line.split(" ");
			l[3].replace("[", "");
			l[3].replace("]", "");
			String[] temp = new String[2];
			temp[0] = l[0];
			temp[1] = l[3];
			temp[1] = temp[1].replaceAll("[\\(\\)\\[\\]\\{\\}]","");
			//System.out.println(l[2]);
			//System.out.println(temp[1]);
			if(lexicon.containsKey(l[2])) {
				lexicon.get(l[2]).add(temp);
			}
			else {
				ArrayList<String[]> newList = new ArrayList<String[]>();
				newList.add(temp);
				lexicon.put(l[2], newList);
			}
			
		}
		
		br.close();
		
		//Wait for User Input
		
		Scanner input = new Scanner(System.in);
		String sent = input.nextLine();
		sent = sent.toLowerCase();
		
		CYKParse(sent);
		
		input.close();
		
		
		//Return highest prob tree
		
		treeNode root = chart.get(new index(nonterms.S, 0, leng - 1));
		
		
		//Print results
		
		printTree(root, 0);
		
		double finalP = root.probability;
		
		if(finalP != 0.0) {
			System.out.println("Probability = " + root.probability);
		}
		
		
		
	}
	
	
	
	public static void CYKParse(String s) {
		//System.out.println(s);
		
		
		
		//Create nodes for lexicon
		
		String[] sentence = s.split(" ");
		leng = sentence.length;
		for(int i = 0; i < leng; i++) {
			String word = sentence[i];
			//System.out.println(word);
			for(int j = 0; j < lexicon.get(word).size(); j++) {
				String[] currentPhrase = lexicon.get(word).get(j);
				chart.put(new index(nonterms.valueOf(currentPhrase[0]), i, i), new treeNode(nonterms.valueOf(currentPhrase[0]), i, i, word, null, null, Double.parseDouble(currentPhrase[1])));
				//System.out.println(nonterms.valueOf(currentPhrase[0]) + " " +  i + " " + i + " with p " + Double.parseDouble(currentPhrase[1]));
			}
		}
		
		//Create and calculate nodes for grammar rules
		
		for(int l = 1; l < leng; l++) {
			for(int i = 0; i < (leng - l); i++) {
				int temp = i + l;
				for(nonterms mc : nonterms.values()) {
					if(chart.get(new index(mc, i, temp)) == null) {
						chart.put(new index(mc, i, temp), new treeNode(mc, i, temp, null, null, null, 0.0));
						//System.out.println(mc.toString() + " " +  i + " " + temp + " marker");
					}
					for(int k = i; k < (temp); k++) {
						for(int h = 0; h < gram.size(); h++) {
							if(nonterms.valueOf(gram.get(h).get(0)) == mc) {
								//System.out.println(mc.toString() + " main");
								//System.out.println(k + " and " + temp);
								//System.out.println(new index(nonterms.S, 1, 1).equals(new index(nonterms.S, 1, 1)));
								//System.out.println(nonterms.valueOf(gram.get(h).get(1)) + " " + i + " " + k);
								//System.out.println(chart.get(new index(nonterms.valueOf(gram.get(h).get(1)), i, k)).probability);
								//System.out.println(nonterms.valueOf(gram.get(h).get(2)) + " " + (k+1) + " " + temp);
								//System.out.println(chart.get(new index(nonterms.valueOf(gram.get(h).get(2)), k+1, temp)).probability);
								double leftP, rightP;
								if(chart.get(new index(nonterms.valueOf(gram.get(h).get(1)), i, k)) == null) {
									leftP = 0.0;
								}
								else {
									leftP = chart.get(new index(nonterms.valueOf(gram.get(h).get(1)), i, k)).probability;
								}
								if(chart.get(new index(nonterms.valueOf(gram.get(h).get(2)), k+1, temp)) == null) {
									rightP = 0.0;
								}
								else {
									rightP = chart.get(new index(nonterms.valueOf(gram.get(h).get(2)), k+1, temp)).probability;
								}
								
								double newProb = leftP * rightP * Double.parseDouble(gram.get(h).get(3));
								//System.out.println(newProb);
								treeNode curr = chart.get(new index(mc, i, temp));
								if(newProb > curr.probability) {
									curr.left = chart.get(new index(nonterms.valueOf(gram.get(h).get(1)), i, k));
									curr.right = chart.get(new index(nonterms.valueOf(gram.get(h).get(2)), k+1, temp));
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
		//Prints out tree data in correct format in necessary indents
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

}

//Enumerated class for Non-terminals
enum nonterms {
	
	Verb, Noun, Prep, VPWithPPList, VerbAndObject, PPList, PP, NP, S;
}

//treeNode class containing data for parse
class treeNode {
	nonterms phrase;
	int start, end;
	String word;
	treeNode left;
	treeNode right;
	double probability;
	
	//Basic constructor with all necessary arguments to create node
	public treeNode(nonterms p, int startIndex, int endIndex, String w, treeNode l, treeNode r, double prob) {
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
	nonterms M;
	int startIndex;
	int endIndex;
	
	//Basic constructor
	public index(nonterms index1, int index2, int index3) {
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
		  if(this.M == other.M && this.startIndex == other.startIndex && this.endIndex == other.endIndex) {
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