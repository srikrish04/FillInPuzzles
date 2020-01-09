import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FillInPuzzle {
	int totalColumns, totalRows, totalWords = 0;
	String[][] wordPuzzle;

	Set<String> pathVisitedAfterChoices = new HashSet<String>();
	HashMap<Integer, Queue<String>> wordQueue = new HashMap<>();
	ArrayList<String> wordIndexMap = new ArrayList<String>();
	Set<String> visitedWords = new HashSet<String>();
	int choice = 0;
	String index = "";

	/**
	 * Sets up the number of rows and columns in 2-D array,
	 * 
	 * @param stream inputs number of rows and column, index and words of the matrix
	 * @return boolean true if puzzle is read.
	 */
	Boolean loadPuzzle(BufferedReader stream) {
		boolean status = false;
		String val;
		try {
			choice = 0;
			int count = 0;
			while ((val = stream.readLine()) != null) {
				val = val.trim();
				if (count == 0) {
					String[] firstLineInputs = val.split(" ");
					count++;
					totalColumns = stringToIntConvertion(firstLineInputs[0]);
					totalRows = stringToIntConvertion(firstLineInputs[1]);
					totalWords = stringToIntConvertion(firstLineInputs[2]);
					wordPuzzle = new String[totalRows][totalColumns];
					continue;
				}
				if (count < totalWords + 1) {
					String nextLines[] = val.split(" ");
					if (nextLines[3] != null && nextLines[3].equals("h")) {
						int row = stringToIntConvertion(nextLines[1]);
						int startCol = stringToIntConvertion(nextLines[0]);
						int endIndex = stringToIntConvertion(nextLines[2]);

						String wordMapStr = "";
						for (int i = startCol; i < startCol + endIndex; i++) {
							wordMapStr = wordMapStr + row + "," + i + " ";
						}
						wordIndexMap.add("h-" + wordMapStr);

					}

					if (nextLines[3] != null && nextLines[3].equals("v")) {
						int startRow = stringToIntConvertion(nextLines[1]);
						int col = stringToIntConvertion(nextLines[0]);
						int endIndex = stringToIntConvertion(nextLines[2]);

						String wordMapStr = "";

						for (int i = startRow; i > startRow - endIndex; i--) {
							wordMapStr = wordMapStr + i + "," + col + " ";

						}
						// adding set of indexes that form a word to a list
						wordIndexMap.add("v-" + wordMapStr);

					}
					count++;
					continue;
				}
				if (count > totalWords && count <= (totalWords * 2) + 1) {
					if (wordQueue.get(val.length()) != null) {
						wordQueue.get(val.length()).add(val);
					} else {
						Queue<String> addVal = new LinkedList<String>();
						addVal.add(val);
						wordQueue.put(val.length(), addVal);
					}
					count++;
				}
				if (count == totalWords * 2 + 1) {
					status = true;
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("Error occured while parsing the input");
			;
		} catch (Exception e) {
			System.out.println("Error occured please retry with correct values.");
		}


		return status;
	}

	/**
	 * Tries to solve the puzzle with unique words, that is words not having same
	 * lengths in the given set of words.
	 */
	void checkUniqueWordLength() {
		for (Integer q : wordQueue.keySet()) {
			// check for unique word length from the HashMap
			if (wordQueue.get(q).size() == 1) {
				Queue<String> word = wordQueue.get(q);
				String value = "";
				// removing first inserted word from the queue
				String valToStore = word.remove();
				for (String val : wordIndexMap) {
					String[] ar = val.split("-");
					String[] indexes = ar[1].split(" ");
					// checks if the word length match with the empty index space
					if (indexes.length == q) {
						for (int i = 0; i < indexes.length; i++) {
							String[] rowColSplit = indexes[i].split(",");
							if (wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer
									.parseInt(rowColSplit[1])] == null) {
								wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer.parseInt(rowColSplit[1])] = ""
										+ valToStore.charAt(i);
							} else {
								if (wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer.parseInt(rowColSplit[1])] == ""
										+ valToStore.charAt(i)) {
									wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer.parseInt(rowColSplit[1])] = ""
											+ valToStore.charAt(i);
								}

							}
							value = val;
						}

					}
				}
				//adds word to visited set
					visitedWords.add(valToStore);
				
					pathVisitedAfterChoices.add(value);
				
			}
		}
	}
	/**
	 * Tries to solve the puzzle with words having same length,
	 * typically solves for words with maximum length first and then next maximum length
	 */
	void solveIteratePuzzles() {
		//checks for maximum length of word from the given words
		int maxLength = 0;
		for (Integer i : wordQueue.keySet()) {
			if (wordQueue.get(i).size() > 0 && maxLength < i) {
				maxLength = i;
			}
		}
		//solves if hashmap has words in the given max length key
		if (wordQueue.get(maxLength)!=null && wordQueue.get(maxLength).size() >= 1) {
			//dequeue from the queue for solving
			String checkWord = wordQueue.get(maxLength).remove();
			//checks if there are any characters in the given word indexes.
			boolean status = checkForLetteOccurenceInWords(checkWord);
			if (status) {
				String[] wordSplit = index.split("-");
				String[] splitIndx = wordSplit[1].split(" ");
				for (int i = 0; i < splitIndx.length; i++) {
					String[] indexs = splitIndx[i].split(",");
					if (wordPuzzle[Integer.parseInt(indexs[0])][Integer.parseInt(indexs[1])] == null) {
						wordPuzzle[Integer.parseInt(indexs[0])][Integer.parseInt(indexs[1])] = checkWord.charAt(i) + "";
					}
				}
				for (String e : wordIndexMap) {
					if (e.equals(index)) {
							pathVisitedAfterChoices.add(e);
						
						index = "";
					}
				}
			} else {
				//if there is no occurrence of character of that word in the given search index
				// adds back the word to the queue for next solve process
				wordQueue.get(maxLength).add(checkWord);
				choice++;
				//solves words with no occurrence of previous characters in its specific indexes
				checkMaxWordLength(maxLength);

			}

				visitedWords.add(checkWord);
			//recursive call if total words solved is less than total number of words
			if (visitedWords.size() < totalWords) {
				solveIteratePuzzles();

			}
		}

	}
	/**
	 * To identify characters if already present in the given indexes,
	 * @param word checks for index length having same as word for previous character occurrence
	 * @return status if there is previous occurence of character
	 */
	boolean checkForLetteOccurenceInWords(String word) {
		boolean status = false;
		for (String e : wordIndexMap) {
			String[] wordMapSplit = e.split("-");
			String[] splitIndx = wordMapSplit[1].split(" ");
			if (word.length() == splitIndx.length && !pathVisitedAfterChoices.contains(e)) {
				for (int i = 0; i < splitIndx.length; i++) {
					String[] indexs = splitIndx[i].split(",");
					
					if (wordPuzzle[Integer.parseInt(indexs[0])][Integer.parseInt(indexs[1])] != null) {
						if (wordPuzzle[Integer.parseInt(indexs[0])][Integer.parseInt(indexs[1])]
								.equals(word.charAt(i) + "")) {
							choice++;
							status = true;
							index = e;
						}
					}
				}

			}
		}
		return status;
	}
	
	/**
	 * To solve for words having no previous character occurrence,
	 * @param maxLength word length to be checked
	 */
	void checkMaxWordLength(int maxLength) {
		String valToStore;
		if (wordQueue.get(maxLength).size() == 1) {
			//removes first word from the queue
			valToStore = wordQueue.get(maxLength).remove();

			for (String val : wordIndexMap) {
				String[] ar = val.split("-");
				String[] indexes = ar[1].split(" ");
				//checks if length of vacant indexes set and word length are equal
				if (indexes.length == maxLength && !pathVisitedAfterChoices.contains(val)) {
					ArrayList<String> checkIndexes = new ArrayList<String>();
					String outval = "";
					for (int i = 0; i < indexes.length; i++) {

						String[] rowColSplit = indexes[i].split(",");
						if (!visitedWords.contains(valToStore)) {
							if (wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer
									.parseInt(rowColSplit[1])] != null) {
								outval = i + "-" + Integer.parseInt(rowColSplit[0]) + ","
										+ Integer.parseInt(rowColSplit[1]);
								checkIndexes.add(outval);
								outval = "";
							}
						}
					}
					int count = 0;

					for (String e : checkIndexes) {
						
						String[] ind = e.split("-");
						String[] chk = ind[1].split(",");
						for (int i = 0; i < chk.length; i++) {
							if (wordPuzzle[Integer.parseInt(chk[0])][Integer.parseInt(chk[1])] == ""
									+ valToStore.charAt(Integer.parseInt(ind[0]))) {
								count++;
							}
						}
					}

					if (count == checkIndexes.size()) {
						for (int k = 0; k < indexes.length; k++) {
							String[] rowColSplit = indexes[k].split(",");
							if (wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer
									.parseInt(rowColSplit[1])] == null) {
								wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer.parseInt(rowColSplit[1])] = ""
										+ valToStore.charAt(k);
							} else {
								if (wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer.parseInt(rowColSplit[1])] == ""
										+ valToStore.charAt(k)) {
									wordPuzzle[Integer.parseInt(rowColSplit[0])][Integer.parseInt(rowColSplit[1])] = ""
											+ valToStore.charAt(k);
								}
							}
						}

							visitedWords.add(valToStore);
						
					}
				}

			}
		}

	}

	/**
	 * Tries to solve the puzzle with given words,
	 * 
	 * @return boolean true if puzzle is solved.
	 */
	Boolean solve() {
		boolean status = false;

		// invoked to solve puzzle initially for unique words with different lengths if
		// any.
		checkUniqueWordLength();

	    // invoked to solve puzzle for words having same length. typically starting from
		// maximum length words.
		solveIteratePuzzles();
		

		//checks if all words from the words are filled to the puzzle
		if (totalWords == visitedWords.size()) {
			status = true;
		}
		return status;
	}
	/**
	 * prints the solved puzzle.
	 * @param outstream
	 */
	void print(PrintWriter outstream) {
		if(outstream!=null) {
		for (int i = totalRows - 1; i >= 0; i--) {
			for (int j = 0; j < totalColumns; j++) {
				if (wordPuzzle[i][j] != null) {
					outstream.print(wordPuzzle[i][j] + ",");
				} else {
					outstream.print(" " + ",");
				}
			}
			outstream.println();
		}
		outstream.flush();
		outstream.close();
		}else {
			System.out.println("Empty or null printwriter");
		}
	}

	/**
	 * converts string value to integer
	 * @param value to be converted
	 */
	int stringToIntConvertion(String val) {
		return (!val.isEmpty() && val != null && Integer.parseInt(val) != 0) ? Integer.parseInt(val) : 0;
	}
	/**
	 * @return number of choice made while solving the puzzle
	 */
	int choices( ) {
		return choice;
	}

}
