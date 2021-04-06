package nfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import nfa.Transition;

public class NFA {
	private HashMap<String, HashMap<Character, ArrayList<String>>> transitionsMap;
	private HashSet<String> acceptStates;

	public void init() {
		transitionsMap = new HashMap<>();
		acceptStates = new HashSet<>();
	}

	public NFA(String structure) {
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
	}

	public void addTransitions(String[] transitions, Transition transitionType) {
		char transitionChar = transitionType.getName().charAt(0);
		System.out.println(transitionType.getName());
		for (String transition : transitions) {
			String[] parsedOneTransition = transition.split(",");
			String from = parsedOneTransition[0];
			String to = parsedOneTransition[1];
			System.out.println("from: " + from + ", to: " + to);

			if (!transitionsMap.containsKey(from)) {
				// Check if this can be implemented in a better way.
				HashMap map = new HashMap<>();
				map.put(Transition.ZERO.getName().charAt(0), new ArrayList<>());
				map.put(Transition.ONE.getName().charAt(0), new ArrayList<>());
				map.put(Transition.EPSILON.getName().charAt(0), new ArrayList<>());

				map.put(transitionChar, Arrays.asList(to));

				transitionsMap.put(from, map);
			} else {
				HashMap<Character, ArrayList<String>> map = transitionsMap.get(from);
				ArrayList<String> list = map.get(transitionChar);
				list.add(to);
//				System.out.println("========" + from + "========");
//				for (String s : list)
//					System.out.println(s);
//				System.out.println("========" + from + "========");
				map.put(transitionChar, list);
				transitionsMap.put(from, map);
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NFA nfa = new NFA("0,0;1,2;3,3#0,0;0,1;2,3;3,3#1,2#3");
	}

}
