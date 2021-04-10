package dfa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class DFA {
	private HashMap<String, ArrayList<String>> transitions;
	private HashSet<String> acceptStates;
	private static final String START_STATE = "0";

	public void init() {
		transitions = new HashMap<>();
		acceptStates = new HashSet<>();
	}

	public DFA(String structure) {
		init();

		String[] parsedStructure = structure.split("#");
		String[] stateTransitions = parsedStructure[0].split(";");
		String[] acceptedStates = parsedStructure[1].split(",");

		for (String acceptedState : acceptedStates)
			acceptStates.add(acceptedState);

		for (String stateTransition : stateTransitions) {
			String[] parsedStateTransition = stateTransition.split(",");
			String node = parsedStateTransition[0];
			ArrayList<String> outgoingStates = new ArrayList<>();

			for (int i = 1; i < parsedStateTransition.length; i++)
				outgoingStates.add(parsedStateTransition[i]);

			transitions.put(node, outgoingStates);
		}

	}

	public boolean Run(String input) {
		String currentState = START_STATE;

		for (char transition : input.toCharArray())
			currentState = transitions.get(currentState).get((int) transition - '0');

		return acceptStates.contains(currentState);
	}

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner("src/dfa/test_1.in");
		PrintWriter pw = new PrintWriter(System.out);

		String dfaInput = sc.nextLine();
		DFA dfa = new DFA(dfaInput);

		while (sc.ready())
			pw.println(dfa.Run(sc.nextLine()));

		pw.flush();

	}

	static class Scanner {
		StringTokenizer st;
		BufferedReader br;

		public Scanner(InputStream system) {
			br = new BufferedReader(new InputStreamReader(system));
		}

		Scanner(String fileName) throws FileNotFoundException {
			br = new BufferedReader(new FileReader(fileName));
		}

		public String next() throws IOException {
			while (st == null || !st.hasMoreTokens())
				st = new StringTokenizer(br.readLine());
			return st.nextToken();
		}

		public String nextLine() throws IOException {
			return br.readLine();
		}

		public int nextInt() throws IOException {
			return Integer.parseInt(next());
		}

		public double nextDouble() throws IOException {
			return Double.parseDouble(next());
		}

		public char nextChar() throws IOException {
			return next().charAt(0);
		}

		public Long nextLong() throws IOException {
			return Long.parseLong(next());
		}

		public boolean ready() throws IOException {
			return br.ready();
		}

		public void waitForInput() throws InterruptedException {
			Thread.sleep(4000);
		}
	}
}
