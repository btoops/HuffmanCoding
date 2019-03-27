import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

// Import any package as required


public class HuffmanSubmit implements Huffman {

	//assign nodes based on hashmap to priority queue
	public PriorityQueue<Node> createPQ(HashMap<Character, Integer> hashmap) {
		PriorityQueue<Node> priorityq = new PriorityQueue<Node>(new NodeComparator());
		for(Entry<Character, Integer> entry : hashmap.entrySet()) {
			Node root = new Node(entry.getKey(), entry.getValue());
			priorityq.offer(root);
		}
		return priorityq;
	}

	//creates a tree based on the priorityQueue
	public Node createTree(PriorityQueue<Node> priorityq) {
		Node subRoot = null;

		while(priorityq.size() > 1) {
			Node first = priorityq.poll();
			Node second = priorityq.poll();
			subRoot = new Node(first.frequency + second.frequency, first, second);
			priorityq.add(subRoot);
		}
		return subRoot;
	}
	
	//creates a hashmap with characters as the value and the huffman code as the binary String
	public void createCharToBinary(Node root, String s, HashMap charToBinary) {
		if(root.left == null && root.right == null && root.letter != null) {
			if(!charToBinary.containsKey(root.letter)) {
				charToBinary.put(root.letter, s);

			}
			return;
		}

		createCharToBinary(root.left, s + "0", charToBinary);
		createCharToBinary(root.right, s + "1", charToBinary);
	}
	//tree traversal
	public void preorder(Node root) {
		System.out.println(root);
		if(root == null) {
			return;
		}
		preorder(root.left);
		preorder(root.right);
	}

	public class NodeComparator implements Comparator<Node>{
		public int compare(Node a, Node b) {
			return a.frequency - b.frequency;
		}

	}
	public class Node{
		int frequency;
		Character letter = null;
		Node left;
		Node right;

		public Node(int frequency) {
			this.frequency = frequency;
		}
		public Node(char letter) {
			this.letter = letter;
		}
		public Node(char letter, int frequency) {
			this.frequency = frequency;
			this.letter = letter;
		}
		public Node(int frequency, Node left, Node right) {
			this.frequency = frequency;
			this.left = left;
			this.right = right;
		}
		public Node(char letter, int frequency, Node left, Node right) {
			this.frequency = frequency;
			this.letter = letter;
			this.left = left;
			this.right = right;
		}
		public String toString() {
			return letter + " " + frequency + " ";
		}
	}
	
	//swaps keys and values of two maps
	public <K, V> HashMap swapMap(HashMap<V, K> charToBinary) {
		HashMap<K, V> binaryToChar = new HashMap<>();
		charToBinary.forEach(
				(k,v) -> binaryToChar.put(v, k));
		return binaryToChar;

	}

	public void encode(String inputFile, String outputFile, String freqFile){
		BinaryIn bin = new BinaryIn(inputFile);
		BinaryOut bout = new BinaryOut(outputFile);
		HashMap<Character, Integer> freqMap = word_count(inputFile, freqFile);
		createFreqFile(freqFile, freqMap);
		PriorityQueue<Node> priorityq = createPQ(freqMap);
		Node tree = createTree(priorityq);
		HashMap<Character, String> charToBinary = new HashMap<>();
		createCharToBinary(tree, "", charToBinary);
		while(!bin.isEmpty()) {
			char c = bin.readChar();
			if(charToBinary.containsKey(c)) {
				char[] binary = charToBinary.get(c).toCharArray();
				for(char ch : binary) {
					if(ch == '0') {
						bout.write(false);
					}else {
						bout.write(true);
					}

				}

			}
		}
		bout.close();
		System.out.println("end encode");
	}
	//gets frequency and creates the hashmap
	public HashMap<Character, Integer> word_count(String inputFile, String freqFile) {
		HashMap<Character, Integer> hashmap = new HashMap<>();
		BinaryIn bin = new BinaryIn(inputFile);
		while(!bin.isEmpty()) {
			char c = bin.readChar();
			if(!hashmap.containsKey(c)) {
				hashmap.put(c,  1);

			}else {
				hashmap.put(c, hashmap.get(c) + 1);
			}
		}
		return hashmap;
	}
	//creates a frequency file based on the character and its frequency
	public void createFreqFile(String freqFile, HashMap<Character, Integer> hashmap) {
		try {
			PrintWriter pw = new PrintWriter(freqFile);
			for(Map.Entry entry : hashmap.entrySet()) {
				String s = "" + Integer.toBinaryString((Character) entry.getKey());
				for(int i = s.length(); i < 8; i++) {
					s = "0" + s;
				}
				pw.println(s + ":" + entry.getValue() + "\n");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}

	}
	//creates a tree from the frequency file
	public Node makeTreeFromFreqFile(String freqFile) {
		HashMap<Character, Integer> map = new HashMap<>();
		BinaryIn bin = new BinaryIn(freqFile);
		String input = bin.readString();
		String[] eachLine = input.split("\\r?\\n\\s+");

		for(int i = 0; i < eachLine.length; i ++) {
			String[] temp = eachLine[i].split(":"); //temp array of size 2
			int parse = Integer.parseInt(temp[0], 2);
			char c = (char) parse;
			map.put(c, Integer.parseInt(temp[1]));
		}


		PriorityQueue<Node> pq = createPQ(map);
		Node tree = createTree(pq);
		return tree;
	}

	public void decode(String inputFile, String outputFile, String freqFile){		
		BinaryIn bin = new BinaryIn(inputFile);
		BinaryOut bout = new BinaryOut(outputFile);
		Node tree = makeTreeFromFreqFile(freqFile);
		HashMap<Character, String> charToBinary = new HashMap<>();
		createCharToBinary(tree, "", charToBinary);
		HashMap<String, Character> binToChar = swapMap(charToBinary);
		while(!bin.isEmpty()) {
			String binary = "";
			try {
				while(!binToChar.containsKey(binary)) {
					boolean b = bin.readBoolean();
					if(b) {
						binary += "1";
					}else {
						binary += "0";
					}

				}

				bout.write(binToChar.get(binary), 8);
				bout.flush();
			}catch(NoSuchElementException e) {

			}

		}
		bout.close();
		System.out.println("end decode");


	}




	public static void main(String[] args) {
		HuffmanSubmit  huffman = new HuffmanSubmit();
		huffman.encode("alice30.txt", "output.txt", "freq.txt");
		huffman.decode("output.txt", "alice30_dec.txt", "freq.txt");
		//huffman.encode("ur.jpg", "ur.enc", "freqImage.txt");
		//huffman.decode("ur.enc", "ur_dec.jpg", "freqImage.txt");
		// After decoding, both ur.jpg and ur_dec.jpg should be the same. 
		// On linux and mac, you can use `diff' command to check if they are the same. 
	}

}
