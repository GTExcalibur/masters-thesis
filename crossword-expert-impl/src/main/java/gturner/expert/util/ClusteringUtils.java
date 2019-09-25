package gturner.expert.util;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/24/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusteringUtils {


    public static <K> List<Map.Entry<K, Long>> getHighClusterValues(Map<K, Long> entries) {
        if(entries.isEmpty()) return null;

        ArrayList<Map.Entry<K, Long>> entryList = new ArrayList<Map.Entry<K, Long>>(entries.entrySet());
        if(entryList.size() == 1) {
            return entryList;
        }

        Collections.sort(entryList, new Comparator<Map.Entry<K, Long>>() {
            @Override
            public int compare(Map.Entry<K, Long> o1, Map.Entry<K, Long> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        if(entryList.size() == 2) {
            Long high = entryList.get(0).getValue();
            Long low = entryList.get(0).getValue();
            if(0.9 * high < low) {
                return Collections.emptyList();
            }
            return Collections.singletonList(entryList.get(0));
        }

        ArrayList<Map.Entry<K,Long>> highList = new ArrayList<Map.Entry<K, Long>>();
        ArrayList<Map.Entry<K,Long>> lowList = new ArrayList<Map.Entry<K, Long>>();

        highList.add(entryList.get(0));
        lowList.add(entryList.get(entryList.size()-1));

        for(int i = 1; i < entryList.size() - 1; i++) {
            Map.Entry<K, Long> current = entryList.get(i);
            if(Math.abs(highList.get(0).getValue() - current.getValue()) < Math.abs(current.getValue() - lowList.get(0).getValue())) {
                highList.add(0, current);
            } else {
                lowList.add(0, current);
            }
        }

        Collections.reverse(highList);

        return highList;
    }
}
