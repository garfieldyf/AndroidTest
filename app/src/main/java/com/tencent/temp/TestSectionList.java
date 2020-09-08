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
        list.addSection(buildSection(0, 3));
        list.addSection(buildSection(1, 4));
        list.addSection(0, buildSection(3, 5));
        list.addSection(1, buildSection(2, 6));
        list.clear();
        list.addSection(0, buildSection(2, 6));
        list.addSection(0, buildSection(1, 4));
        list.addSection(1, buildSection(3, 3));
        list.addSection(2, buildSection(4, 2));
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
    }

    private static List<String> buildSection(int sectionIndex, int count) {
        final ArrayList<String> data = new ArrayList<String>(count);
        for (int i = 0; i < count; ++i) {
            data.add("section_" + sectionIndex + "_" + i);
        }

        return data;
    }
}
