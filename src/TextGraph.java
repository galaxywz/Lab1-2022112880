import java.util.*;

/**
 * 文本图结构类
 * 用于表示单词之间的关系图
 */
public class TextGraph {
    private Map<String, Integer> wordToIndex; // 单词到索引的映射
    private List<String> indexToWord; // 索引到单词的映射
    private int[][] adjacencyMatrix; // 邻接矩阵
    private int vertexCount; // 顶点数量
    private int edgeCount; // 边数量
    
    /**
     * 构造函数，从文本创建图结构
     * @param text 输入文本
     */
    public TextGraph(String text) {
        // 初始化数据结构
        wordToIndex = new HashMap<>();
        indexToWord = new ArrayList<>();
        
        // 处理文本
        List<String> words = processText(text);
        
        // 构建图
        buildGraph(words);
    }
    
    /**
     * 处理文本，提取单词
     * @param text 输入文本
     * @return 处理后的单词列表
     */
    private List<String> processText(String text) {
        List<String> words = new ArrayList<>();
        
        // 将文本转换为小写
        text = text.toLowerCase();
        
        // 替换标点符号为空格
        text = text.replaceAll("[\\p{Punct}]", " ");
        
        // 分割文本为单词
        String[] wordsArray = text.split("\\s+");
        
        // 过滤非字母字符
        for (String word : wordsArray) {
            word = word.replaceAll("[^a-z]", "");
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        
        return words;
    }
    
    /**
     * 构建图结构
     * @param words 单词列表
     */
    private void buildGraph(List<String> words) {
        // 创建顶点
        for (String word : words) {
            if (!wordToIndex.containsKey(word)) {
                int index = indexToWord.size();
                wordToIndex.put(word, index);
                indexToWord.add(word);
            }
        }
        
        vertexCount = indexToWord.size();
        adjacencyMatrix = new int[vertexCount][vertexCount];
        
        // 创建边
        for (int i = 0; i < words.size() - 1; i++) {
            String word1 = words.get(i);
            String word2 = words.get(i + 1);
            
            int index1 = wordToIndex.get(word1);
            int index2 = wordToIndex.get(word2);
            
            adjacencyMatrix[index1][index2]++;
            edgeCount++;
        }
    }
    
    /**
     * 获取顶点数量
     * @return 顶点数量
     */
    public int getVertexCount() {
        return vertexCount;
    }
    
    /**
     * 获取边数量
     * @return 边数量
     */
    public int getEdgeCount() {
        return edgeCount;
    }
    
    /**
     * 获取单词的所有后继单词
     * @param word 单词
     * @return 后继单词列表
     */
    public List<String> getSuccessors(String word) {
        List<String> successors = new ArrayList<>();
        
        if (!wordToIndex.containsKey(word)) {
            return successors;
        }
        
        int index = wordToIndex.get(word);
        for (int i = 0; i < vertexCount; i++) {
            if (adjacencyMatrix[index][i] > 0) {
                successors.add(indexToWord.get(i));
            }
        }
        
        return successors;
    }
    
    /**
     * 获取单词的所有前驱单词
     * @param word 单词
     * @return 前驱单词列表
     */
    public List<String> getPredecessors(String word) {
        List<String> predecessors = new ArrayList<>();
        
        if (!wordToIndex.containsKey(word)) {
            return predecessors;
        }
        
        int index = wordToIndex.get(word);
        for (int i = 0; i < vertexCount; i++) {
            if (adjacencyMatrix[i][index] > 0) {
                predecessors.add(indexToWord.get(i));
            }
        }
        
        return predecessors;
    }
    
    /**
     * 查找两个单词之间的桥接词
     * @param word1 第一个单词
     * @param word2 第二个单词
     * @return 桥接词列表
     */
    public List<String> getBridgeWords(String word1, String word2) {
        List<String> bridgeWords = new ArrayList<>();
        
        if (!wordToIndex.containsKey(word1) || !wordToIndex.containsKey(word2)) {
            return bridgeWords;
        }
        
        int index1 = wordToIndex.get(word1);
        int index2 = wordToIndex.get(word2);
        
        for (int i = 0; i < vertexCount; i++) {
            if (adjacencyMatrix[index1][i] > 0 && adjacencyMatrix[i][index2] > 0) {
                bridgeWords.add(indexToWord.get(i));
            }
        }
        
        return bridgeWords;
    }
    
    /**
     * 获取邻接矩阵
     * @return 邻接矩阵
     */
    public int[][] getAdjacencyMatrix() {
        return adjacencyMatrix;
    }
    
    /**
     * 获取所有单词
     * @return 单词列表
     */
    public List<String> getAllWords() {
        return new ArrayList<>(indexToWord);
    }
    
    /**
     * 检查单词是否存在于图中
     * @param word 单词
     * @return 是否存在
     */
    public boolean containsWord(String word) {
        return wordToIndex.containsKey(word);
    }
    
    /**
     * 获取两个单词之间的边权重
     * @param word1 第一个单词
     * @param word2 第二个单词
     * @return 边权重，如果不存在则返回0
     */
    public int getEdgeWeight(String word1, String word2) {
        if (!wordToIndex.containsKey(word1) || !wordToIndex.containsKey(word2)) {
            return 0;
        }
        
        int index1 = wordToIndex.get(word1);
        int index2 = wordToIndex.get(word2);
        
        return adjacencyMatrix[index1][index2];
    }
}

