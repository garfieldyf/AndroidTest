package com.tencent.temp;

import android.ext.util.SectionList;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class TestSectionList {
    public static void testList() {
        final LogPrinter printer = new LogPrinter(Log.INFO, "abcd");
        SectionList<String> list = new SectionList<String>();
//        list.add("section_add");
        list.addSection(buildSection(0, 3));
        list.addSection(buildSection(1, 4));
        list.addSection(0, buildSection(3, 5));
        list.addSection(1, buildSection(2, 6));
//        list.add(5, "section_add_5");
//        list.add("section_add_2");
//        list.add(9, "section_add_9");
//        list.clear();
//        list.addSection(0, buildSection(2, 6));
//        list.addSection(0, buildSection(1, 4));
//        list.addSection(1, buildSection(3, 3));
//        list.addSection(2, buildSection(4, 2));
        forEach(printer, list);
        //printer.println("section = " + list.getSectionForPosition(17));
    }

    private static void removeAll(Printer printer, SectionList<String> list) {
        Iterator<String> itor = list.iterator();
        while (itor.hasNext()) {
            itor.next();
            itor.remove();
        }

        forEach(printer, list);
    }

    private static void removeAll(Printer printer, SectionList<String> list, int sectionIndex, int count) {
        final List<String> c = new ArrayList<String>();
        for (int i = 0; i < count; ++i) {
            final String value = "section_" + sectionIndex + "_" + i;
            c.add(value);
        }

//        c.add("section_0_0");
        list.removeAll(c);
//        list.retainAll(c);
        forEach(printer, list);
    }

    private static void removeSection(Printer printer, SectionList<String> list, int size) {
        for (int i = 0; i < size; ++i) {
            final String value = list.remove(0);
            printer.println("remove value = " + value);
            forEach(printer, list);
        }
    }

    private static void forEach(Printer printer, SectionList<String> list) {
        list.dump(printer);
        final int sectionCount = list.getSectionCount();
        for (int i = 0, index = 0; i < sectionCount; ++i) {
            final List<String> section = list.getSection(i);
            for (int j = 0, size = section.size(); j < size; ++j, ++index) {
                printer.println("index = " + index + ", section = " + i + ", sectionIndex = " + j + ", value = " + section.get(j));
            }
        }
    }

    private static List<String> buildSection(int sectionIndex, int count) {
        final ArrayList<String> data = new ArrayList<String>(count);
        for (int i = 0; i < count; ++i) {
            data.add("section_" + sectionIndex + "_" + i);
        }

        return data;
    }
}
