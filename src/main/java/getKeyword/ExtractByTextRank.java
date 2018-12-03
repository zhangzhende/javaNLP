package getKeyword;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * text-rank----基于图排序的算法
 * <p>
 * 他利用投票的原理，让每一个单词给他的邻居（也就是窗口内的单词）投票，票的权重取决于自己的票数；
 * <p>
 * 当权值为1时，即为Google的PageRank算法
 * WS(Vi) = (1-d) + d * Σ（ (Wij/Σ（Wjk）)*WS(Vj) ）
 *
 * d-阻尼系数=0.85（一般）
 * 
 *
 *
 */
public class ExtractByTextRank {

    public void get() {
        String str = "hfjash fha h ahf ahha hfahf ah  h h hah";
        Map<String, Double> initialMap = new HashMap<String, Double>();
        double d = 0.85;
        int windowsLength = 5;
        Map<String, Set<String>> wordMap = new HashMap<String, Set<String>>();
        Map<String, Double> score = new HashMap<String, Double>();
        int min_diff = 0;

        String[] strArray = str.split(" ");
        for (int j = 0; j < strArray.length; j++) {
            initialMap.put(strArray[j], 1 - d);
            int lower = Math.max(0, j - windowsLength);
            int upper = Math.min(strArray.length, j + windowsLength);
            for (int l = 0; l < j; l++) {
                putMap(wordMap, strArray, j, l);
            }
            if (j < strArray.length - 1) {
                for (int l = j + 1; l < upper; l++) {
                    putMap(wordMap, strArray, j, l);
                }
            }
        }

        int max_iter = 10;//迭代次数
        for (int i = 0; i < max_iter; i++) {
            double max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : wordMap.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                for (String out : value) {
                    //计算key的连出节点的个数
                    int size = wordMap.get(out).size();//
                    if (key.equals(out) || size == 0) {
                        continue;//如果key节点本身或者没有连出节点，则不更新
                    }
                    initialMap.put(key, initialMap.get(key) + d / size * (score.get(out) == null ? 0 : score.get(out)));
                    //计算迭代两次的变化
                    max_diff = Math.max(max_diff, Math.abs(initialMap.get(key) - (score.get(key) == null ? 0 : score.get(key))));
                }
                score = initialMap;
                if (max_diff <= min_diff) {
                    break;
                }
            }
        }
    }

    public void putMap(Map<String, Set<String>> wordMap, String[] strArray, int j, int l) {
        Set<String> set = wordMap.get(strArray[j]);
        if (set == null) {
            set = new HashSet<String>();
        }
        set.add(strArray[l]);
        wordMap.put(strArray[j], set);
    }
}
