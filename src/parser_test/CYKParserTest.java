package parser_test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import parser.CYKParser;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by zhang on 3/24/14.
 * Java Version: 1.8.0
 */
public class CYKParserTest {
  private final String filepath = "src/parser_test/english.txt";
  private final String trueCases = "src/parser_test/true_cases.txt";
  private final String falseCases = "src/parser_test/false_cases.txt";
  CYKParser parser;
  HashSet<String> language;
  HashSet<String> underivables;

  @Before
  public void setUp() throws Exception {
    parser = new CYKParser(filepath);
    language = new
      HashSet<>(Arrays.
                asList(new
                       String(Files
                              .readAllBytes(Paths
                                            .get(trueCases)))
                       .split("\n")));
    underivables = new
      HashSet<>(Arrays.
                asList(new
                        String(Files
                        .readAllBytes(Paths
                                .get(falseCases)))
                        .split("\n")));
  }

  @Test(timeout = 1000)
  public void testParse() throws Exception {
    language.forEach(ele -> Assert.assertTrue(parser.parse(ele)));
    underivables.forEach(ele -> Assert.assertFalse(parser.parse(ele)));
  }

  @Test
  public void testList() {

  }
}
