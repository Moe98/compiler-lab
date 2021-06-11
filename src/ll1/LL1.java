package ll1;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class LL1 {
    final private static String EPSILON = "e";
    final private static String DELIMITER = ";";
    final private static String TOKEN_DELIMITER = ",";
    final private static String START_SYMBOL = "S";
    private LinkedHashMap<String, ArrayList<String>> cfg;
    private LinkedHashMap<String, ArrayList<String>> first;
    private LinkedHashMap<String, ArrayList<String>> follow;
    private LinkedHashMap<String, LinkedHashMap<String, String>> ll1_parsing_table;
    private HashSet<String> terminals;
    private LinkedList<String> pda;

    public LL1(String input) {
        init();

        readInput(input);

        setTerminals();

        constructLL1Table();
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner("src/ll1/test_1.in");
        String input = "";

        while(sc.ready()) {
            String line = sc.nextLine();

            if(line.contains(";")) {
                input = line;
                continue;
            }

            new LL1(input).parse(line);
        }
    }

    private void setTerminals() {
        cfg.forEach((symbol, rules) -> {
            rules.forEach(rule -> Stream.of(rule.split((""))).
                    filter(beta -> !cfg.containsKey(beta) && !beta.equals(EPSILON)).
                    forEach(beta -> {
                        terminals.add(beta);
                        ll1_parsing_table.put(beta, new LinkedHashMap<>());
                    }));
        });

        terminals.add("$");

        ll1_parsing_table.put("$", new LinkedHashMap<>());
    }

    private void constructLL1Table() {
        first.forEach((symbol, values) -> {
            for (int i = 0; i < values.size(); i++) {
                String rule = cfg.get(symbol).get(i);
                boolean hasEpsilon = false;

                for (char c : values.get(i).toCharArray()) {
                    String a = String.valueOf(c);

                    if (a.equals(EPSILON)) {
                        hasEpsilon = true;
                        continue;
                    }

                    LinkedHashMap<String, String> x = ll1_parsing_table.get(a);
                    x.put(symbol, rule);
                    ll1_parsing_table.put(a, x);
                }

                if (hasEpsilon) {
                    String follows = follow.get(symbol).get(0);
                    for (char c : follows.toCharArray()) {
                        String a = String.valueOf(c);
                        LinkedHashMap<String, String> x = ll1_parsing_table.get(a);
                        x.put(symbol, rule);
                        ll1_parsing_table.put(a, x);
                    }
                }
            }
        });

//        ll1_parsing_table.forEach((terminal, symbols) -> {
//            symbols.forEach((symbol, rule) -> {
//                System.out.printf("Terminal: %s\nSymbol: %s\nRule: %s\n", terminal, symbol, rule);
//            });
//        });
    }

    private void init() {
        cfg = new LinkedHashMap<>();
        first = new LinkedHashMap<>();
        follow = new LinkedHashMap<>();
        ll1_parsing_table = new LinkedHashMap<>();
        terminals = new HashSet<>();
        pda = new LinkedList<>();
    }

    private void readInput(String input) {
        String[] splitInput = input.split("#");
        String[] symbolsRules = splitInput[0].split(DELIMITER);
        String[] firsts = splitInput[1].split(DELIMITER);
        String[] follows = splitInput[2].split(DELIMITER);

        setMapWith(cfg, symbolsRules);
        setMapWith(first, firsts);
        setMapWith(follow, follows);

//        printMap(cfg);
//        printMap(first);
//        printMap(follow);
    }

    private void printMap(LinkedHashMap<String, ArrayList<String>> map) {
        map.forEach((key, value) -> System.out.printf("Key: %s\nValue: %s\n", key, value.toString()));
    }

    private void setMapWith(LinkedHashMap<String, ArrayList<String>> map, String[] values) {
        Arrays.stream(values).map(value -> value.split(TOKEN_DELIMITER)).forEach(value -> {
            final String symbol = value[0];
            ArrayList<String> rules = new ArrayList<>(Arrays.asList(value).subList(1, value.length));
            map.put(symbol, rules);
        });
    }

    private void parse(String input) {
        // Don't print during the steps where a terminal cancels out a terminal.
        PrintWriter pw = new PrintWriter(System.out);
        pda = new LinkedList<>();
        input += "$";

        pda.add("S");
        pw.print(START_SYMBOL);

        int pdaIndex = 0;
        int inputIndex = 0;

        while (true) {
            // What if the input finishes and the pda still has stuff?
            if (inputIndex == input.length() - 1 && pdaIndex == pda.size()) {
                pw.println();
                break;
            }

            if (pdaIndex >= pda.size() || inputIndex >= input.length()) {
                pw.println(",ERROR");
                break;
            }

            String a = String.valueOf(input.charAt(inputIndex));

            if (pda.get(pdaIndex).equals(a)) {
                inputIndex++;
                pdaIndex++;
                continue;
            }

            if (!ll1_parsing_table.get(a).containsKey(pda.get(pdaIndex))) {
                pw.println(",ERROR");
                break;
            }

            String rule = ll1_parsing_table.get(a).get(pda.remove(pdaIndex));

            if (!rule.equals(EPSILON))
                for (int i = pdaIndex; i < pdaIndex + rule.length(); i++)
                    pda.add(i, String.valueOf(rule.charAt(i - pdaIndex)));

            pw.print(",");
            for (int i = 0; i < pda.size(); i++)
                pw.print(pda.get(i));
        }

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
