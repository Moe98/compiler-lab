package first_follow;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class FirstFollow {
    final private static String EPSILON = "e";
    final private static String DELIMITER = ";";
    final private static String TOKEN_DELIMITER = ",";
    final private static String START_SYMBOL = "S";
    //TODO: Make this non-static.
    private static boolean hasChanged;
    private LinkedHashMap<String, ArrayList<String>> cfg;
    private LinkedHashMap<String, TreeSet<String>> first;
    private LinkedHashMap<String, TreeSet<String>> follow;

    public FirstFollow(String input) {
        init();

        readInput(input);

        initFirstAndFollow();

        computeFirst();

        computeFollow();

        printOutput();
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner("src/first_follow/test_1.in");
        while (sc.ready()) {
            new FirstFollow(sc.nextLine());
        }
    }

    private void readInput(String input) {
        String[] splitInput = input.split(DELIMITER);

        for (String symbolRules : splitInput) {
            String[] symbolRule = symbolRules.split(TOKEN_DELIMITER);
            final String symbol = symbolRule[0];

            ArrayList<String> rules = new ArrayList<>(Arrays.asList(symbolRule).subList(1, symbolRule.length));

            cfg.put(symbol, rules);
        }
    }

    private void init() {
        cfg = new LinkedHashMap<>();
        follow = new LinkedHashMap<>();
    }

    private void initFirstAndFollow() {
        first = new LinkedHashMap<>();

        cfg.forEach((symbol, rules) -> {
            first.put(symbol, new TreeSet<>());
            follow.put(symbol, new TreeSet<>());

            if (symbol.equals(START_SYMBOL))
                follow.get(symbol).add("$");

            rules.forEach(rule -> Stream.of(rule.split((""))).
                    filter(beta -> !cfg.containsKey(beta)).
                    forEach(beta -> first.put(beta, new TreeSet<>(List.of(beta)))));
        });
    }

    private void computeFirst() {
        hasChanged = true;

        while (hasChanged) {
            hasChanged = false;

            cfg.forEach((symbol, rules) -> {
                for (String rule : rules) {
                    boolean areAllEpsilon = true;

                    for (char beta : rule.toCharArray()) {
                        if (!first.get(String.valueOf(beta)).contains(EPSILON))
                            areAllEpsilon = false;
                    }

                    if (areAllEpsilon) {
                        if (!first.get(symbol).contains(EPSILON)) {
                            first.get(symbol).add(EPSILON);
                            hasChanged = true;
                        }
                    }

                    for (int i = 0; i < rule.length(); i++) {
                        areAllEpsilon = true;
                        for (int j = 0; j < i; j++) {
                            if (!first.get(String.valueOf(rule.charAt(j))).contains(EPSILON))
                                areAllEpsilon = false;
                        }
                        if (i == 0 || areAllEpsilon) {
                            if (isNotSubset(first.get(String.valueOf(rule.charAt(i))), first.get(symbol))) {
                                for (String bSymbol : first.get(String.valueOf(rule.charAt(i)))) {
                                    if (!bSymbol.equals(EPSILON))
                                        first.get(symbol).add(bSymbol);
                                }
                                hasChanged = true;
                            }
                        }
                    }
                }
            });
        }
    }

    private boolean isNotSubset(TreeSet<String> b, TreeSet<String> a) {
        for (String bSymbol : b) {
            if (!bSymbol.equals(EPSILON) && !a.contains(bSymbol))
                return true;
        }

        return false;
    }

    private void computeFollow() {
        hasChanged = true;

        while (hasChanged) {
            hasChanged = false;

            cfg.forEach((symbol, rules) -> {
                for (String rule : rules) {
                    for (int i = 0; i < rule.length(); i++) {
                        final String b = String.valueOf(rule.charAt(i));
                        // Terminal.
                        if (!cfg.containsKey(String.valueOf(rule.charAt(i))))
                            continue;

                        boolean areAllEpsilon = true;

                        for (int j = i + 1; j < rule.length() && areAllEpsilon; j++) {
                            final String beta = String.valueOf(rule.charAt(j));
                            if (isNotSubset(first.get(beta), follow.get(b))) {
                                for (String bSymbol : first.get(beta)) {
                                    if (!bSymbol.equals(EPSILON))
                                        follow.get(b).add(bSymbol);
                                }
                                hasChanged = true;
                            }

                            areAllEpsilon = first.get(beta).contains(EPSILON);
                        }

                        if (areAllEpsilon) {
                            if (isNotSubset(follow.get(symbol), follow.get(b))) {
                                for (String bSymbol : follow.get(symbol))
                                    follow.get(b).add(bSymbol);
                                hasChanged = true;
                            }
                        }

                    }
                }
            });
        }
    }

    private void printOutput() {
        PrintWriter pw = new PrintWriter(System.out);
        StringBuilder firstOutput = new StringBuilder();
        StringBuilder followOutput = new StringBuilder();

        for (String symbol : cfg.keySet()) {
            firstOutput.append(symbol).append(TOKEN_DELIMITER);
            followOutput.append(symbol).append(TOKEN_DELIMITER);

            TreeSet<String> firsts = first.get(symbol);
            TreeSet<String> follows = follow.get(symbol);

            for (String first : firsts)
                firstOutput.append(first);

            boolean hasDollarSign = false;

            for (String follow : follows) {
                if (follow.equals("$")) {
                    hasDollarSign = true;
                    continue;
                }
                followOutput.append(follow);
            }

            if (hasDollarSign)
                followOutput.append("$");

            firstOutput.append(DELIMITER);
            followOutput.append(DELIMITER);
        }

        pw.printf("First: %s\n", firstOutput.substring(0, firstOutput.length() - 1));
        pw.printf("Follow: %s\n", followOutput.substring(0, followOutput.length() - 1));
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