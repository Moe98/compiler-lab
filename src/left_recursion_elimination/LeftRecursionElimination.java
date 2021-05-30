package left_recursion_elimination;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

public class LeftRecursionElimination {
    final private static String PRIME_SYMBOL = "'";
    final private static String EPSILON = "e";
    final private static String DELIMITER = ";";
    final private static String TOKEN_DELIMITER = ",";
    private HashMap<String, ArrayList<String>> cfg;
    private ArrayList<String> order;

    public LeftRecursionElimination(String input) {
        init();

        readInput(input);

        eliminateLeftRecursion();

        printOutput();
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner("src/left_recursion_elimination/private_test_1.in");
        while (sc.ready())
            new LeftRecursionElimination(sc.nextLine());
    }

    private void init() {
        cfg = new HashMap<>();
        order = new ArrayList<>();
    }

    private void readInput(String input) {
        String[] splitInput = input.split(DELIMITER);

        for (String symbolRules : splitInput) {
            String[] symbolRule = symbolRules.split(TOKEN_DELIMITER);
            final String symbol = symbolRule[0];
            order.add(symbol);

            ArrayList<String> rules = new ArrayList<>(Arrays.asList(symbolRule).subList(1, symbolRule.length));

            cfg.put(symbol, rules);
        }
    }

    private void eliminateLeftRecursion() {

        for (int i = 0; i < order.size(); i++) {
            for (int j = 0; j < i; j++) {
                doMagic(order.get(i), order.get(j));
            }

            eliminateImmediateLeftRecursion(order.get(i));
        }
    }

    private void printOutput() {
        PrintWriter pw = new PrintWriter(System.out);

        for (String symbol : order) {
            pw.print(symbol);
            ArrayList<String> rules = cfg.get(symbol);

            for (String rule : rules)
                pw.print(TOKEN_DELIMITER + rule);

            final String symbolPrime = symbol + PRIME_SYMBOL;

            if (cfg.containsKey(symbolPrime)) {
                pw.print(DELIMITER);
                pw.print(symbolPrime);
                ArrayList<String> symbolPrimeRules = cfg.get(symbolPrime);

                for (String rule : symbolPrimeRules)
                    pw.print(TOKEN_DELIMITER + rule);
            }

            if (!symbol.equals(order.get(order.size() - 1)))
                pw.print(DELIMITER);
        }

        pw.println();
        pw.flush();
    }

    private void doMagic(String symbol, String precedingSymbol) {
        ArrayList<String> rules = cfg.get(symbol);
        boolean isLeftRecursive = false;

        for (String rule : rules)
            if (beginsWith(rule, precedingSymbol)) {
                isLeftRecursive = true;
                break;
            }

        if (!isLeftRecursive)
            return;

        ArrayList<String> precedingSymbolRules = cfg.get(precedingSymbol);

        ArrayList<String> newRules = new ArrayList<>();

        for (String rule : rules) {
            if (beginsWith(rule, precedingSymbol)) {
                for (String precedingSymbolRule : precedingSymbolRules)
                    newRules.add(precedingSymbolRule + rule.substring(1));
            } else {
                newRules.add(rule);
            }
        }

        cfg.replace(symbol, newRules);
    }

    private void eliminateImmediateLeftRecursion(String symbol) {
        ArrayList<String> rules = cfg.get(symbol);
        boolean isLeftRecursive = false;

        for (String rule : rules)
            if (beginsWith(rule, symbol)) {
                isLeftRecursive = true;
                break;
            }

        if (!isLeftRecursive)
            return;

        doEliminateImmediateLeftRecursion(symbol);
    }

    private void doEliminateImmediateLeftRecursion(String symbol) {
        ArrayList<String> rules = cfg.get(symbol);
        ArrayList<String> alphas = new ArrayList<>();
        ArrayList<String> betas = new ArrayList<>();


        for (String rule : rules) {
            if (isAlpha(rule, symbol))
                alphas.add(rule.substring(symbol.length()));
            else
                betas.add(rule);
        }

        ArrayList<String> newSymbol = new ArrayList<>();
        ArrayList<String> newSymbolPrime = new ArrayList<>();

        for (String alpha : alphas)
            newSymbolPrime.add(alpha + symbol + PRIME_SYMBOL);
        newSymbolPrime.add(EPSILON);

        for (String beta : betas)
            newSymbol.add(beta + symbol + PRIME_SYMBOL);

        cfg.replace(symbol, newSymbol);
        cfg.put(symbol + PRIME_SYMBOL, newSymbolPrime);
    }

    private boolean isAlpha(String rule, String symbol) {
        return beginsWith(rule, symbol);
    }

    private boolean beginsWith(String rule, String symbol) {
        return rule.startsWith(symbol);
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