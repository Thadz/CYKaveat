package generator;

/**
 * Given the filepath containing well-formed Chomsky normal form grammars,
 * generate all possible strings within the given number of steps of derivation
 * using leftmost derivation approach.
 *
 * Created by Honglin Zhang on 3/23/14
 * Java Version: 1.8.0
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class CNFGenerator {
  private static final String DELIMITER = " -> ";
  public HashMap<String, HashSet<LinkedList<String>>> rules;
  private final boolean DEBUG_MODE = false;

  /**
   * Given the filepath, create a generator instance which reads all the
   * context-free grammars in Chomsky normal form in and builds an internal
   * representation of derivation grammars.
   * @param filepath, the path of file containing well-formed CFG in CNF
   * @throws Exception
   */
  public CNFGenerator(String filepath) throws Exception {
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
   * Main entrance of this program. Given the filepath of CNF grammar,
   * print out all possible strings within the given number of derivation.
   * @param args
   */
  public static void main(String[] args) {
    try {
      if (args.length < 5) {
        Arrays.stream(new String[]
          { "usage: generator <filepath> <startVariable> <numberOfDerivation>"
            + "<trueCasesPath> <falseCasesPath>"
            , "<filepath>: the path of file containing well-formatted CNF rules"
            , "<startVariable>: the start of the CNF derivation"
            , "<numberOfDerivation>: the number of derivation from the start"
            , "<trueCasesPath>: the path of file to write to for derivable strings"
            , "<falseCasesPath>: the path of file to write to for underivable strings"
            , "rule example:\n\tA" + DELIMITER + "AA\n\tA" + DELIMITER + "a"
          }).forEach(System.out::println);
      } else {
        int steps = Integer.parseInt(args[2]);
        CNFGenerator generator = new CNFGenerator(args[0]);
        HashSet<String> language = generator.generate(args[1], steps);
        HashSet<String> terminals =
          generator.rules.entrySet().stream()
          .map(
               entry -> entry.getValue()
               )
          .reduce(new HashSet<>(), (s1, s2) -> {
                   s1.addAll(s2);
                   return s1;
            })
          .stream()
          .filter(l -> Character.isLowerCase(l.get(0).charAt(0)))
          .map(l -> {
              HashSet<String> set = new HashSet<>();
              set.add(l.get(0));
              return set;
            })
          .reduce(new HashSet<>(), (s1, s2) -> {
              s1.addAll(s2);
              return s1;
            });
        // TODO: The number of dpeth is hard-coded
        // Generate a bunch of random strings using terminals;
        HashSet<String> underivables = new HashSet<>(); {
          terminals.forEach(t1 -> {
              underivables.add(t1);
              terminals.forEach(t2 -> {
                  underivables.add(t1 + t2);
                  terminals.forEach(t3 -> {
                      underivables.add(t1 + t2 + t3);
                    });
                });
            });
          underivables.removeAll(language);
        }
        writeSetToPath(language, args[3]);
        writeSetToPath(underivables, args[4]);
      }
    } catch (NumberFormatException e) {
      System.out.println("ill-formatted number of steps for CNF derivation");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Generate all possible strings within the given number of derivation,
   * using leftmost derivation.
   * @param start, the start variable for CNF
   * @param stepNumber, the number of derivation
   * @return a set of possible strings derived
   */
  public HashSet<String> generate(String start, int stepNumber) {
    HashSet<LinkedList<String>> set = new HashSet<>(); {
      // To limit the scope of newly-created variable, list
      LinkedList<String> list = new LinkedList<>();
      list.add(start);
      set.add(list);
    }
    HashSet<String> language = new HashSet<>();
    for (int i = 0; i < stepNumber; ++i) {
      HashSet<LinkedList<String>> derivedSet = new HashSet<>();
      for (LinkedList<String> list : set) {
        Optional<String> symbol = list.stream()
          .filter(rules::containsKey).findFirst();
        if (symbol.isPresent()) {
          // there exist variables
          int index = list.indexOf(symbol.get());
          rules.get(symbol.get()).forEach(right -> {
              LinkedList<String> derivedList = new LinkedList<>(list);
              // replace the variable in place
              derivedList.set(index, right.get(0));
              if (right.size() > 1) {
                // insert another variable if there are more than one token
                derivedList.add(index + 1, right.get(1));
              }
              derivedSet.add(derivedList);
            });
        } else {
          // only terminals left, no variables
          language.add(list.stream().reduce("", (s1, s2) -> s1 + s2));
        }
      }
      set = derivedSet;
    }
    return language;
  }

  private static void writeSetToPath(HashSet<String> set, String filepath) {
    set.stream().reduce((s1, s2) -> s1 + "\n" + s2).ifPresent(chunk -> {
        try {
          Files.write(Paths.get(filepath), chunk.getBytes());
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
  }
}
