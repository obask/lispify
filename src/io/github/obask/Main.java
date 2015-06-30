package io.github.obask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    static final Path path = Paths.get("/Users/obaskakov/IdeaProjects/lisbon/prog.scm");

    static final String L_PAREN = "(";
    static final String R_PAREN = ")";


    abstract class ATree {

    }

    class ABranch extends ATree {
        final List<ATree> value;
        ABranch(List<ATree> val) {
            value = val;
        }

        @Override
        public String toString() {
            final StringBuilder xx = new StringBuilder();
            xx.append("[");
            for (ATree x : value) {
                xx.append(x);
                xx.append(" ");
            }
            xx.append("]");
            return xx.toString();
        }
    }


    abstract class ALeaf extends ATree {

    }


    class AString extends ALeaf {
        final String value;
        AString(String val) {
            value = val;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    class ANumber extends ALeaf {
        final double value;
        ANumber(double val) {
            value = val;
        }
    }


    public ATree reduceTree(Iterator<String> tokens, final List<ATree> state) {
        if (tokens.hasNext()) {
            final String curr = tokens.next();
            switch (curr) {
                case L_PAREN:
//                    System.out.println("L_PAREN");
                    final List<ATree> tmp = new ArrayList<>();
                    final ATree aTree = reduceTree(tokens, tmp);
                    state.add(aTree);
                    return reduceTree(tokens, state);
                case R_PAREN:
//                    System.out.println("R_PAREN");
                    return new ABranch(state);
                default:
//                    System.out.println("L_ATOM");
//                    System.out.println(state.getClass());
                    state.add(new AString(curr));
                    return reduceTree(tokens, state);
            }
        } else {
            return new ABranch(state);
        }
    }


    // TODO replace to splitAsStream method
    // Pattern.compile("\\W").splitAsStream("Some sentence");

    public static Stream<String> splitMeAsStream(String input, final char ch) {
        final char value[] = input.toCharArray();

        final String sep = String.valueOf(ch);
        int off = 0;
        int next = 0;
        boolean limited = false;
        ArrayList<String> list = new ArrayList<>();
        while ((next = input.indexOf(ch, off)) != -1) {
            list.add(input.substring(off, next));
            list.add(sep);
            off = next + 1;
        }
        // If no match was found, return this
        if (off == 0) {
            return Collections.singletonList(input).stream();
        }
        // Add remaining segment
        list.add(input.substring(off, value.length));

        return list.stream();
    }

    
    public static void main(String[] args) {
	// write your code here
        try {
            final Stream<String> stream0 = Files.lines(path);
            Stream<String> stream1 = stream0.map(ss -> ss.split(" ")).flatMap(Arrays::stream);
            Stream<String> stream2 = stream1.flatMap(ss -> splitMeAsStream(ss, L_PAREN.charAt(0)));
            Stream<String> stream3 = stream2.flatMap(ss -> splitMeAsStream(ss, R_PAREN.charAt(0)));
//            String stream4 = stream3.filter(ss -> !ss.isEmpty()).collect(Collectors.joining("\n"));
//            String[] stmp = stream4.toArray(String[]::new);
//            System.out.println(stream4);

            final Main app = new Main();

            final List<String> objects = stream3.filter(ss -> !ss.isEmpty()).collect(Collectors.toList());
            final Iterator<String> stringIterator = objects.iterator();

            while (stringIterator.hasNext()) {
                final String next = stringIterator.next();
                assert(next.equals(L_PAREN));
                final ATree aTree = app.reduceTree(stringIterator, new ArrayList<>());
                System.out.println(aTree);
                System.out.println("----");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
