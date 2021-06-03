package first_follow;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class FirstFollow {
    final private static String EPSILON = "e";
    final private static String DELIMITER = ";";
    final private static String TOKEN_DELIMITER = ",";
    //TODO: Make this non-static.
    private static boolean hasChanged;
    private LinkedHashMap<String, ArrayList<String>> cfg;
    private LinkedHashMap<String, TreeSet<String>> first;
    private LinkedHashMap<String, TreeSet<String>> follow;
    private HashSet<String> hasEpsilon;

    public FirstFollow(String input) {
        init();

        readInput(input);

        initFirst();

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

            if (rules.stream().anyMatch(rule -> rule.equals(EPSILON)))
                hasEpsilon.add(symbol);

            cfg.put(symbol, rules);
        }
    }

    private void init() {
        cfg = new LinkedHashMap<>();
        follow = new LinkedHashMap<>();
        hasEpsilon = new HashSet<>();
    }

    private void initFirst() {
        first = new LinkedHashMap<>();

        cfg.forEach((symbol, rules) -> {
            first.put(symbol, new TreeSet<>());
            rules.forEach(rule -> {
                Stream.of(rule.split((""))).
                        filter(beta -> !cfg.containsKey(beta)).
                        forEach(beta -> first.put(beta, new TreeSet<>(List.of(beta))));
            });
        });

//        first.forEach((symbol, firsts) -> {
//            System.out.println(symbol + " " + firsts);
//        });
    }

    private void computeFirst() {
//        boolean hasChanged = true;
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
//                            System.out.println("changed in the if");
                            hasChanged = true;
                        }
                    } //else {
                    for (int i = 0; i < rule.length(); i++) {
                        areAllEpsilon = true;
                        for (int j = 0; j < i; j++) {
                            if (!first.get(String.valueOf(rule.charAt(j))).contains(EPSILON))
                                areAllEpsilon = false;
                        }
                        if (i == 0 || areAllEpsilon) {
                            if (isNotSubset(String.valueOf(rule.charAt(i)), symbol)) {
                                for (String bSymbol : first.get(String.valueOf(rule.charAt(i)))) {
                                    if (!bSymbol.equals(EPSILON))
                                        first.get(symbol).add(bSymbol);
                                }
//                                    System.out.println("changed in the else");
                                hasChanged = true;
                            }
                        }
                    }
                }
                //}
            });

//            cfg.forEach((symbol, firsts) -> {
//                System.out.println(symbol + " " + first.get(symbol));
//            });
        }
    }

    private boolean isNotSubset(String b, String a) {
        // something in |b| is not in |a|

        for (String bSymbol : first.get(b)) {
            if (!bSymbol.equals(EPSILON) && !first.get(a).contains(bSymbol))
                return true;
        }

        return false;
    }

    private void computeFollow() {

    }

    private void printOutput() {
        // Change First&Follow ArrayLists to TreeSets to have them ordered?
        PrintWriter pw = new PrintWriter(System.out);
        StringBuilder sb = new StringBuilder();
        for (String symbol : cfg.keySet()) {
            sb.append(symbol);

            TreeSet<String> firsts = first.get(symbol);

            for (String first : firsts)
                sb.append(TOKEN_DELIMITER).append(first);

            sb.append(DELIMITER);
        }

        pw.println(sb.substring(0, sb.length() - 1));
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