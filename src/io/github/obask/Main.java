package io.github.obask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    static public Path path = Paths.get("/Users/obaskakov/IdeaProjects/lissabon/prog.scm");


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
            Stream<String> stream2 = stream1.flatMap(ss -> splitMeAsStream(ss, ')'));
            Stream<String> stream3 = stream2.flatMap(ss -> splitMeAsStream(ss, '('));
            String stream4 = stream3.filter(ss -> !ss.isEmpty()).collect(Collectors.joining("\n"));
//            String[] stmp = stream4.toArray(String[]::new);
            System.out.println(stream4);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
