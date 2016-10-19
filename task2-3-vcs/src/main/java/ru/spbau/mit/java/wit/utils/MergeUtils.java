package ru.spbau.mit.java.wit.utils;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 10/1/16
 * Email: egor-mailbox@ya.com
 */
public class MergeUtils {

    /**
     * Creates git-like merge of two files
     */
    public static List<String> merge(List<String> linesFrom, List<String> linesTo,
                                     String nameFrom, String nameTo) {

        Patch diff = DiffUtils.diff(linesFrom, linesTo);
        int last = 0;
        ArrayList<String> merge = new ArrayList<>();
        for (Delta d : diff.getDeltas()) {
            List<String> original = (List<String>) d.getOriginal().getLines();
            List<String> revised = (List<String>) d.getRevised().getLines();
            int pos = d.getOriginal().getPosition();
            while (last < pos) {
                merge.add(linesFrom.get(last++));
            }
            switch (d.getType()) {
                case CHANGE:
                    merge.add(">>>>>>>>>>>>>> " + nameFrom);
                    merge.addAll(original);
                    merge.add("================================");
                    merge.addAll(revised);
                    merge.add("<<<<<<<<<<<<<< " + nameTo);
                    last += original.size();
                    break;
                case DELETE:
                    merge.add(">>>>>>>>>>>>>> " + nameFrom);
                    merge.addAll(original);
                    merge.add("<<<<<<<<<<<<<< DELETE");
                    last += original.size();
                    break;
                case INSERT:
                    merge.add(">>>>>>>>>>>>>> " + nameTo);
                    merge.addAll(revised);
                    merge.add("<<<<<<<<<<<<<< INSERT");
                    break;
            }
            d.getOriginal().getLines().size();
        }

        return merge;
    }
}
