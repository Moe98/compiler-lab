package fdfa;

import java.io.*;
import java.util.*;

public class FDFA {
    private HashMap<String, ArrayList<String>> transitions;
    private HashMap<String, String> actions;
    private HashSet<String> acceptStates;
    private static final String START_STATE = "0";

    private void init() {
        transitions = new HashMap<>();
        actions = new HashMap<>();
        acceptStates = new HashSet<>();
    }

    public FDFA(String structure) {
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
                if (i == parsedStateTransition.length - 1)
                    actions.put(node, parsedStateTransition[i]);
                else
                    outgoingStates.add(parsedStateTransition[i]);

            transitions.put(node, outgoingStates);
        }
    }

    public String Run(String input) {
        return simulateFallback(input);
    }

    private String simulateFallback(String input) {
        Stack<FallbackState> fallbackStack = constructFallbackStack(input);
        StringBuilder fallbackResult = new StringBuilder();

        while (!fallbackStack.isEmpty()) {
            Stack<FallbackState> rejectedStack = new Stack<FallbackState>();
            String fallbackAction = actions.get(fallbackStack.peek().getState());

            while (!fallbackStack.isEmpty()) {
                if (fallbackStack.peek().isAccepted())
                    break;
                rejectedStack.push(fallbackStack.pop());
            }

            if (!fallbackStack.isEmpty()) {
                fallbackAction = actions.get(fallbackStack.peek().getState());
                fallbackStack = constructFallbackStack(stackToString(rejectedStack));
            }

            fallbackResult.append(fallbackAction);
        }

        return fallbackResult.toString();
    }

    private String stackToString(Stack<FallbackState> stack) {
        StringBuilder result = new StringBuilder();

        while (!stack.isEmpty())
            result.append(stack.pop().getValue());

        return result.toString();
    }

    private Stack<FallbackState> constructFallbackStack(String input) {
        Stack<FallbackState> fallbackStack = new Stack<FallbackState>();
        String currentState = START_STATE;

        for (char transition : input.toCharArray()) {
            currentState = transitions.get(currentState).get((int) transition - '0');
            fallbackStack.push(new FallbackState(acceptStates.contains(currentState), transition, currentState));
        }

        return fallbackStack;
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner("src/fdfa/test_2.in");
        PrintWriter pw = new PrintWriter(System.out);

        String fdfaInput = sc.nextLine();
        FDFA fdfa = new FDFA(fdfaInput);

        while (sc.ready())
            pw.println(fdfa.Run(sc.nextLine()));

        pw.flush();
    }

    public class FallbackState {
        private boolean isAccepted;
        private char value;
        private String state;

        public FallbackState(boolean isAccepted, char value, String state) {
            this.isAccepted = isAccepted;
            this.value = value;
            this.state = state;
        }

        public boolean isAccepted() {
            return isAccepted;
        }

        public char getValue() {
            return value;
        }

        public String getState() {
            return state;
        }

        @Override
        public String toString() {
            return "FallbackState{" +
                    "isAccepted=" + isAccepted +
                    ", value=" + value +
                    ", state='" + state + '\'' +
                    '}';
        }
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