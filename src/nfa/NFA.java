package nfa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.HashMap;
import nfa.Transition;

public class NFA {
	private HashSet<String> acceptStates;
	private HashSet<String> seenStates;
	private HashMap<String, ArrayList<String>> dfa;
	private HashMap<String, ArrayList<String>> dfaHelper;
	private HashMap<String, TreeSet<String>> epsilonClosure;
	private HashMap<String, HashMap<Character, ArrayList<String>>> transitionsMap;
	private Queue<String> newStates;
	private static final String DEAD_STATE = "dead_state";
	private static final String START_STATE = "0";
	private static String NEW_START_STATE = "";

	private void init() {
		acceptStates = new HashSet<>();
		seenStates = new HashSet<>();
		dfa = new HashMap<>();
		dfaHelper = new HashMap<>();
		epsilonClosure = new HashMap<>();
		transitionsMap = new HashMap<>();
		newStates = new LinkedList<>();

	}

	public NFA(String structure) {
		dfa(structure);
	}

	private void dfa(String structure) {
		init();

		String[] parsedStructure = structure.split("#");
		String[] zeroTransitions = parsedStructure[0].split(";");
		String[] oneTransitions = parsedStructure[1].split(";");
		String[] epsilonTransitions = parsedStructure[2].split(";");
		String[] acceptedStates = parsedStructure[3].split(",");

		for (String acceptedState : acceptedStates)
			acceptStates.add(acceptedState);

		addTransitions(zeroTransitions, Transition.ZERO);
		addTransitions(oneTransitions, Transition.ONE);
		addTransitions(epsilonTransitions, Transition.EPSILON);

		epsilonClosure();

		createDFA();
	}

	private void addTransitions(String[] transitions, Transition transitionType) {
		char transitionAsChar = transitionType.getName().charAt(0);
//		System.out.println(transitionType.getName());
		for (String transition : transitions) {
			String[] parsedOneTransition = transition.split(",");
			String from = parsedOneTransition[0];
			String to = parsedOneTransition[1];
//			System.out.println("from: " + from + ", to: " + to);

			if (!transitionsMap.containsKey(from)) {
				// Check if this can be implemented in a better way.
				HashMap map = new HashMap<>();
				map.put(Transition.ZERO.getName().charAt(0), new ArrayList<>());
				map.put(Transition.ONE.getName().charAt(0), new ArrayList<>());
				map.put(Transition.EPSILON.getName().charAt(0), new ArrayList<>());

				map.put(transitionAsChar, new ArrayList<>(Arrays.asList(to)));

				transitionsMap.put(from, map);
			} else {
				HashMap<Character, ArrayList<String>> map = transitionsMap.get(from);
				ArrayList<String> list = map.get(transitionAsChar);
				list.add(to);
//				System.out.println("========" + from + "========");
//				for (String s : list)
//					System.out.println(s);
//				System.out.println("========" + from + "========");
				map.put(transitionAsChar, list);
				transitionsMap.put(from, map);
			}

			// Verify this.
			if (!transitionsMap.containsKey(to)) {
				HashMap map = new HashMap<>();
				map.put(Transition.ZERO.getName().charAt(0), new ArrayList<>());
				map.put(Transition.ONE.getName().charAt(0), new ArrayList<>());
				map.put(Transition.EPSILON.getName().charAt(0), new ArrayList<>());

				transitionsMap.put(to, map);
			}
			// Verify this.
		}
	}

	private void epsilonClosure() {
		for (String from : transitionsMap.keySet()) {
			doEpsilonClosure(from);
		}
	}

	private void doEpsilonClosure(String from) {
		TreeSet<String> visited = new TreeSet<>();
		Queue<String> queue = new LinkedList<>();

		visited.add(from);
		queue.add(from);

		while (!queue.isEmpty()) {
			String currentFrom = queue.poll();
			ArrayList<String> futureFroms = transitionsMap.get(currentFrom).get(Transition.EPSILON.getName().charAt(0));
			for (String futureFrom : futureFroms) {
				if (!visited.contains(futureFrom))
					queue.add(futureFrom);
				visited.add(futureFrom);
			}
		}

		epsilonClosure.put(from, visited);

//		System.out.println(epsilonClosure.get(from));
	}

	private void createDFA() {
		for (String from : transitionsMap.keySet()) {
			addDFAHelperTransition(from, Transition.ZERO);
			addDFAHelperTransition(from, Transition.ONE);
//			System.out.println(dfaHelper.get(from));
		}

		addNewStartState();
		addDeadState();

		while (!newStates.isEmpty()) {
			addDFATransition(newStates.peek(), Transition.ZERO);
			addDFATransition(newStates.peek(), Transition.ONE);
			seenStates.add(newStates.peek());
			System.out.println(newStates.peek());
			System.out.println(dfa.get(newStates.peek()));
			newStates.poll();
		}

//		for (String s : dfa.keySet()) {
//			System.out.println(s);
//			ArrayList<String> list = dfa.get(s);
//			System.out.println(list.toString());
//		}
	}

	private void addDeadState() {
		ArrayList<String> deadStateTransitions = new ArrayList<>();
		deadStateTransitions.add(DEAD_STATE);
		deadStateTransitions.add(DEAD_STATE);

		seenStates.add(DEAD_STATE);

		dfa.put(DEAD_STATE, deadStateTransitions);
	}

	private void addNewStartState() {
		// Add the epsilon closure of the initial start state
		TreeSet<String> startStateEpsilonClosure = epsilonClosure.get(START_STATE);

		NEW_START_STATE = createCompoundState(startStateEpsilonClosure);

		newStates.add(NEW_START_STATE);
	}

	private void addDFAHelperTransition(String from, Transition transition) {
		ArrayList<String> outgoingTransitions = transitionsMap.get(from).get(transition.getName().charAt(0));
		TreeSet<String> outgoingEpsilonClosure = new TreeSet<>();

		for (String outgoingTransition : outgoingTransitions) {
			for (String outgoingTransitionEpsilonClosure : epsilonClosure.get(outgoingTransition))
				outgoingEpsilonClosure.add(outgoingTransitionEpsilonClosure);
		}
		String outgoingCompoundState = createCompoundState(outgoingEpsilonClosure);
		if (!dfaHelper.containsKey(from))
			dfaHelper.put(from, new ArrayList<>(Arrays.asList(outgoingCompoundState)));
		else {
			ArrayList<String> list = dfaHelper.get(from);
			list.add(outgoingCompoundState);
			dfaHelper.put(from, list);
		}
	}

	// Test thoroughly.
	private void addDFATransition(String compoundFrom, Transition transition) {
		if (seenStates.contains(compoundFrom))
			return;

		String[] from = compoundFrom.split(",");
		TreeSet<String> statesUnion = new TreeSet<>();
		for (String simpleState : from) {
			if (dfaHelper.get(simpleState) == null
					|| dfaHelper.get(simpleState).size() < transition.getName().charAt(0) - '0') {
				continue;
			}

			String[] states = dfaHelper.get(simpleState).get((int) (transition.getName().charAt(0)) - '0').split(",");
			for (String state : states)
				if (state.length() != 0)
					statesUnion.add(state);

		}

		String outgoingState = createCompoundState(statesUnion);
		if (outgoingState.length() == 0)
			outgoingState = "dead_state";

		if (!dfa.containsKey(compoundFrom))
			dfa.put(compoundFrom, new ArrayList<>(Arrays.asList(outgoingState)));
		else {
			ArrayList<String> list = dfa.get(compoundFrom);
			list.add(outgoingState);
			dfa.put(compoundFrom, list);
		}
		if (!seenStates.contains(outgoingState))
			newStates.add(outgoingState);
	}

	private String createCompoundState(TreeSet<String> states) {
		if (states.size() == 0)
			return "";

		String compoundState = "";

		for (String state : states)
			compoundState += state + ",";

		return compoundState.substring(0, compoundState.length() - 1);
	}

	private boolean Run(String input) {
		String currentState = NEW_START_STATE;

		for (char transition : input.toCharArray())
			currentState = dfa.get(currentState).get((int) transition - '0');
		String[] states = currentState.split(",");

		for (String state : states)
			if (acceptStates.contains(state))
				return true;

		return false;
	}

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner("src/nfa/test_2.in");
		PrintWriter pw = new PrintWriter(System.out);

		String nfaInput = sc.nextLine();
		nfaInput = "0,0;1,2;3,3#0,0;0,1;2,3;3,3#1,2#3";
		NFA nfa = new NFA(nfaInput);

//		while (sc.ready())
//			pw.println(nfa.Run(sc.nextLine()));
////
//		pw.flush();

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
