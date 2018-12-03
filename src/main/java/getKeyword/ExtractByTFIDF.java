package getKeyword;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import util.MyConstants;

import java.io.*;
import java.util.*;

/**
 * TF-IDF
 * TF=该词出现的次数/文档的总词数
 * <p>
 * IDF=log10(总文档数/包含该词的文档数)
 * <p>
 * tfidf=TF*IDF
 * <p>
 * tf-idf算法会倾向于选出某一特定文档内的高频词语，同时该词在整个文档集合中分布是比较集中（即包含该词的文档较少）。
 * 所以有一个缺陷如下：在文档分类时，如果一个词在这一类文档中频繁出现（即包含这个词的文档较多），但是这就导致他的idf较小从而可能导致该
 * 词条类别区分能力不强，但是实际上，如果一个词条在某一类文档中频繁出现，则说明该词条能很好的代表这个类的文本特征的。
 * <p>
 * <p>
 * 改进算法一：
 * tf-iwf
 * iwf=log(所有词语总数/该词出现的词数)
 * <p>
 * <p>
 * 改进方法二---tfdfidf0
 * https://max.book118.com/html/2018/0910/5113131144001313.shtm
 */
public class ExtractByTFIDF {


    /**
     * 计算tfIdf
     *
     * @param topk
     * @return
     */
    public static Map<String, Double> getTfidf(int topk) {
        Map<String, Double> tfidfMap = new HashMap<String, Double>();
        Map<String, Double> tfMap = new HashMap<String, Double>();
        Map<String, Double> idfMap = new HashMap<String, Double>();

        Iterator<String> tfit = tfMap.keySet().iterator();
        Iterator<String> idfit = idfMap.keySet().iterator();

        while (tfit.hasNext()) {
            String key1 = tfit.next();
            String key2 = idfit.next();
//            Double v = tfMap.get(key1) * idfMap.get(key2);
            //顺序不一致不就乱了吗，取同样的key对应的tf,idf
            Double v = tfMap.get(key1) * idfMap.get(key1);
            tfidfMap.put(key1, v);
        }

        //排序
        List<Map.Entry<String, Double>> infoids = new ArrayList<Map.Entry<String, Double>>(tfidfMap.entrySet());
        Collections.sort(infoids, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                if (o2.getValue() - o1.getValue() > 0) {
                    return 1;
                }
                return -1;
            }
        });
        if (infoids.size() < topk) {
            topk = infoids.size();
        }
        //获取topK
        for (int i = 0; i < topk; i++) {
            String id = infoids.get(i).toString();
            int equalIndex = id.indexOf('=');
            char[] strChar = id.toCharArray();
            String fea = new String();
            for (int j = 0; j < equalIndex; j++) {
                fea = fea + strChar[j];
            }
            System.out.println(fea);
        }
        return tfidfMap;
    }

    /**
     * 获取idf(Inverse Document Frequency)逆文档频率
     * idf=log10(总文档数/包含该词的文档数)----文档数与文档数的关系
     *
     * @return
     * @throws IOException
     */
    public static Map<String, Double> getIdfMAp() throws IOException {
        //词频数
        Map<String, Integer> wordMap = new HashMap<String, Integer>();
        //总类别数
        int totalClassNum = 10;
        //各文档所在的开始行数，这里应该是多个文档分词在一个文件，不同的类别在不同行里面，这里存储的是文件行数
        int[] a = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};


        Map<String, Double> idfMap = new HashMap<String, Double>();
        Iterator<String> it = wordMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();//获取当前Word
            int otherClassNum = 1;
            for (int m = 0; m < totalClassNum - 1; m++) {//遍历所有类别
                int beginning = a[m];//第m文档所在行数
                int end = a[m + 1];
                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream("a.txt"), "utf-8");//读取文件
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String txt = bufferedReader.readLine();//读一行
                int next = 1;
                while (txt != null) {
                    if (next >= beginning - 1 && next < end) {//如果是这个文档，需在这个文档的词表里面出现，下面是循环词表，查看是否出现
                        String[] str2Array = txt.split("");
                        int l;
                        for (l = 0; l < str2Array.length; l++) {
                            if (str2Array[l].equals(key)) {//找到了
                                otherClassNum++;//在这个文档出现过一次
                                break;
                            }
                        }
                        if (l < str2Array.length) {
                            break;//小于说明找到了，该文档包含，该文档的循环结束
                        }
                    }
                    //到这里说明这一行没找到，读取下一行，继续找
                    txt = bufferedReader.readLine();
                    next++;
                }
            }
            idfMap.put(key, Math.log(1.0 * (totalClassNum) / otherClassNum));//idf=log(总文档数/包含关键词的文档数)
        }
        return idfMap;
    }

    /**
     * 获取tf（term frequency）,指一个文档中给定的词语在指定文档中的占比
     * 归一化为小数
     * 计算为：tf=该词出现的次数/文档的总词数
     * <p>
     * 词数与词数的关系
     *
     * @param wordMap
     * @param tfMap
     * @param list
     * @return
     */
    public static Map<String, Double> getTfMaps(Map<String, Integer> wordMap, Map<String, Double> tfMap, List<List<Term>> list) {
        //总词数
        long totalCount = 0;
        for (List<Term> termList : list) {
            totalCount += termList.size();
            //遍历统计词数
            for (Term term : termList) {
                if (!wordMap.containsKey(term.word)) {
                    wordMap.put(term.word, 1);
                } else {
                    int v = wordMap.get(term.word);
                    wordMap.put(term.word, v + 1);
                }
            }
        }
        Iterator<String> it = wordMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            int v = wordMap.get(key);
            //统计词频，归一化totalCount
            tfMap.put(key, 1.0 * v / totalCount);
        }

        return tfMap;

    }


    /**
     * 获取停用词
     *
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getStopWord() throws IOException {
        File file1 = new File("E:\\资料\\数据\\totalstop.txt");//停用词
        ArrayList<String> stopword = new ArrayList();
        String string1 = null;
        BufferedReader br1 = new BufferedReader(new FileReader(file1));//构造一个BufferedReader类来读取totalstop文件
        while ((string1 = br1.readLine()) != null) {//使用readLine方法，一次读一行 读取停用词
            stopword.add(string1);
        }
        br1.close();
        return stopword;

    }

    /**
     * 测试tf
     *
     * @throws IOException
     */
    public static void test1() throws IOException {
        ArrayList<String> stopword = getStopWord();
        List<Term> termList = HanLP.segment(MyConstants.CONTENT);
        //去掉停用词
        termList.removeAll(stopword);
        List<List<Term>> list = new ArrayList<List<Term>>();
        list.add(termList);
        Map<String, Integer> wordMap = new HashMap<String, Integer>();
        Map<String, Double> tfMap = new HashMap<String, Double>();
        tfMap = getTfMaps(wordMap, tfMap, list);
        System.out.println(tfMap);
    }

    public static void main(String[] args) throws IOException {
        test1();
    }
}
