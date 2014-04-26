package parser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * A CYK Parser for well-formed CNF grammars.
 *
 * Created by zhang on 3/24/14.
 * Java Version: 1.8.0
 */
public class CYKParser {
  private HashMap<String, HashSet<LinkedList<String>>> rules;
  private String DELIMITER = " -> ";
  private String TOKEN_DELIMITER = " ";
  private String START = "S";

  /**
   * Given the filepath, create a generator instance which reads all the
   * context-free grammars in Chomsky normal form in and builds an internal
   * representation of derivation grammars.
   * @param filepath, the path of file containing well-formed CFG in CNF
   * @throws Exception
   */
  public CYKParser(String filepath) throws Exception{
    rules =
      Arrays
      .stream(
              new String(Files.readAllBytes(Paths.get(filepath))).split("\n")
              )
      .map(l -> {
          // first token is the left variable while the second is the derivation
          String[] tokens = l.split(DELIMITER);
          HashMap<String, HashSet<LinkedList<String>>> map = new HashMap<>();
          HashSet<LinkedList<String>> set = new HashSet<>();
          LinkedList<String> list = new LinkedList<>();
          if (Character.isUpperCase(tokens[1].charAt(0))) {
            // if the derivation are variables, store them separately
            list.add(Character.toString(tokens[1].charAt(0)));
            list.add(Character.toString(tokens[1].charAt(1)));
          } else {
            // if the derivation is a single terminal, store this piece
            list.add(tokens[1]);
          }
          set.add(list);
          map.put(tokens[0], set);
          return map;
        })
      .reduce(new HashMap<>()
              , (m1, m2) -> {
                for (String key : m2.keySet()) {
                  if (m1.containsKey(key)) {
                    // if m1 has the same left variable as m2
                    // put m2's corresponding derivations in place
                    HashSet<LinkedList<String>> set = m1.get(key);
                    set.addAll(m2.get(key));
                    m1.put(key, set);
                  } else {
                    // if m1 doesn't have such left variable as m2
                    // add the new rule to m1
                    m1.put(key, m2.get(key));
                  }
                }
                return m1;
              });
  }

  /**
   * Reverse the rules so that the right hand derivation appears as the
   * key and the left hand variable is stored as the value. This can result
   * in faster reverse lookup for rules being used in core CYK algorithm.
   * @param rules, the internal representation of CNF grammars
   * @return
   */
  private HashMap<LinkedList<String>, HashSet<String>>
    reverseRules(HashMap<String, HashSet<LinkedList<String>>> rules) {
    HashMap<LinkedList<String>, HashSet<String>> reversed = new HashMap<>();
    rules.forEach((key, value) -> {
        value.forEach(list -> {
            LinkedList<String> listAsKey = new LinkedList<>(list);
            HashSet<String> set
              = reversed.containsKey(listAsKey)
              ? reversed.get(listAsKey)
              : new HashSet<>();
            set.add(key);
            reversed.put(listAsKey, set);
          });
      });
    return reversed;
  }

  /**
   * Parse the input based on the CYK algorithm, For simplicity, actual
   * variables are stored instead of variable index numbers.
   * c.f. http://en.wikipedia.org/wiki/CYK_algorithm
   * @param input
   * @return
   */
  public boolean parse(String input) {
    String[] tokens = input.split(TOKEN_DELIMITER);
    // initialize the cell
    LinkedList<LinkedList<HashSet<String>>> cell = new LinkedList<>(); {
      for (int i = 0; i < tokens.length; ++i) {
        LinkedList<HashSet<String>> list = new LinkedList<>();
        for (int j = 0; j < tokens.length; ++j) {
          list.add(new HashSet<>());
        }
        cell.add(list);
      }
    }
    HashMap<LinkedList<String>, HashSet<String>> reversed = reverseRules(rules);
    for (int i = 0; i < tokens.length; ++i) {
      HashSet<String> set = cell.get(i).get(0);
      LinkedList<String> listAsKey = new LinkedList<>();
      listAsKey.add(tokens[i] + TOKEN_DELIMITER);
      if (reversed.containsKey(listAsKey)) {
        set.addAll(reversed.get(listAsKey));
        cell.get(i).set(0, set);
      }
    }
    for (int i = 1; i < tokens.length; ++i) {
      for (int j = 0; j < tokens.length - i; ++j) {
        for (int k = 0; k < i; ++k) {
          for (String B : cell.get(j).get(k)) {
            for (String C : cell.get(j + k + 1).get(i - k - 1)) {
              LinkedList<String> listAsKey = new LinkedList<>();
              listAsKey.add(B);
              listAsKey.add(C);
              if (reversed.containsKey(listAsKey)) {
                cell.get(j).get(i).addAll(reversed.get(listAsKey));
              }
            }
          }
        }
      }
    }
    return (cell.get(0).get(tokens.length - 1).contains(START));
  }
}
