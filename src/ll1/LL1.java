package ll1;

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

    public LL1(String input) {
        init();

        readInput(input);

        setTerminals();

        constructLL1Table();

        printOutput();
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

                    if(a.equals(EPSILON)) {
                        hasEpsilon = true;
                        continue;
                    }

                    LinkedHashMap<String, String> x = ll1_parsing_table.get(a);
                    x.put(symbol, rule);
                    ll1_parsing_table.put(a, x);
                }

                if(hasEpsilon) {
                    String follows = follow.get(symbol).get(0);
                    for(char c : follows.toCharArray()) {
                        String a = String.valueOf(c);
                        LinkedHashMap<String, String> x = ll1_parsing_table.get(a);
                        x.put(symbol, rule);
                        ll1_parsing_table.put(a, x);
                    }
                }
            }
        });

        ll1_parsing_table.forEach((terminal, symbols) -> {
            symbols.forEach((symbol, rule) -> {
                System.out.printf("Terminal: %s\nSymbol: %s\nRule: %s\n", terminal, symbol, rule);
            });
        });
    }


    private void init() {
        cfg = new LinkedHashMap<>();
        first = new LinkedHashMap<>();
        follow = new LinkedHashMap<>();
        ll1_parsing_table = new LinkedHashMap<>();
        terminals = new HashSet<>();
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

    private void printOutput() {
    }

    public static void main(String[] args) {
        LL1 ll1 = new LL1("S,iST,e;T,cS,a#S,i,e;T,c,a#S,ca$;T,ca$");

    }
}
