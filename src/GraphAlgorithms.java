import java.util.*;

/**
 * 图算法类
 * 实现各种图算法操作
 */
public class GraphAlgorithms {
    
    /**
     * 计算两个单词之间的最短路径（Dijkstra算法）
     * @param graph 图结构
     * @param startWord 起始单词
     * @param endWord 目标单词
     * @return 最短路径，如果不存在则返回空列表
     */
    public static List<String> shortestPath(TextGraph graph, String startWord, String endWord) {
        // 检查单词是否存在于图中
        if (!graph.containsWord(startWord) || !graph.containsWord(endWord)) {
            return new ArrayList<>();
        }
        
        // 获取图的信息
        List<String> allWords = graph.getAllWords();
        int[][] matrix = graph.getAdjacencyMatrix();
        int vertexCount = allWords.size();
        
        // 获取起始和目标单词的索引
        int startIndex = allWords.indexOf(startWord);
        int endIndex = allWords.indexOf(endWord);
        
        // 初始化距离数组和前驱节点数组
        int[] distance = new int[vertexCount];
        int[] predecessor = new int[vertexCount];
        boolean[] visited = new boolean[vertexCount];
        
        // 初始化距离为无穷大，前驱为-1
        for (int i = 0; i < vertexCount; i++) {
            distance[i] = Integer.MAX_VALUE;
            predecessor[i] = -1;
            visited[i] = false;
        }
        
        // 起始节点距离为0
        distance[startIndex] = 0;
        
        // Dijkstra算法主循环
        for (int count = 0; count < vertexCount - 1; count++) {
            // 找到未访问的最小距离节点
            int minIndex = -1;
            int minDist = Integer.MAX_VALUE;
            for (int i = 0; i < vertexCount; i++) {
                if (!visited[i] && distance[i] < minDist) {
                    minDist = distance[i];
                    minIndex = i;
                }
            }
            
            // 如果没有可达节点，退出循环
            if (minIndex == -1) break;
            
            // 标记为已访问
            visited[minIndex] = true;
            
            // 更新相邻节点的距离
            for (int i = 0; i < vertexCount; i++) {
                // 如果有边且未访问
                if (matrix[minIndex][i] > 0 && !visited[i]) {
                    int newDist = distance[minIndex] + matrix[minIndex][i];
                    // 如果找到更短的路径
                    if (newDist < distance[i]) {
                        distance[i] = newDist;
                        predecessor[i] = minIndex;
                    }
                }
            }
        }
        
        // 如果目标节点不可达
        if (distance[endIndex] == Integer.MAX_VALUE) {
            return new ArrayList<>();
        }
        
        // 重建路径
        List<String> path = new ArrayList<>();
        int current = endIndex;
        while (current != -1) {
            path.add(allWords.get(current));
            current = predecessor[current];
        }
        
        // 反转路径（从起点到终点）
        Collections.reverse(path);
        
        return path;
    }
    
    /**
     * 计算单词的PageRank值
     * @param graph 图结构
     * @param word 单词
     * @param dampingFactor 阻尼系数（默认0.85）
     * @param iterations 迭代次数
     * @return PageRank值
     */
    public static double pageRank(TextGraph graph, String word, double dampingFactor, int iterations) {
        // 检查单词是否存在于图中
        if (!graph.containsWord(word)) {
            return 0.0;
        }
        
        // 获取图的信息
        List<String> allWords = graph.getAllWords();
        int[][] matrix = graph.getAdjacencyMatrix();
        int vertexCount = allWords.size();
        
        // 获取单词的索引
        int wordIndex = allWords.indexOf(word);
        
        // 初始化PageRank值
        double[] pr = new double[vertexCount];
        Arrays.fill(pr, 1.0 / vertexCount);
        
        // 迭代计算PageRank
        for (int iter = 0; iter < iterations; iter++) {
            double[] newPr = new double[vertexCount];
            
            // 处理出度为0的节点，将其PR值均分给所有节点
            double sinkPR = 0;
            for (int i = 0; i < vertexCount; i++) {
                boolean isSink = true;
                for (int j = 0; j < vertexCount; j++) {
                    if (matrix[i][j] > 0) {
                        isSink = false;
                        break;
                    }
                }
                if (isSink) {
                    sinkPR += pr[i] / vertexCount;
                }
            }
            
            // 计算新的PageRank值
            for (int i = 0; i < vertexCount; i++) {
                // 随机跳转部分
                newPr[i] = (1.0 - dampingFactor) / vertexCount;
                
                // 加上出度为0的节点贡献
                newPr[i] += dampingFactor * sinkPR;
                
                // 加上其他节点的贡献
                for (int j = 0; j < vertexCount; j++) {
                    if (matrix[j][i] > 0) {
                        // 计算j节点的出度
                        int outDegree = 0;
                        for (int k = 0; k < vertexCount; k++) {
                            outDegree += matrix[j][k];
                        }
                        
                        // 根据边权重计算贡献
                        newPr[i] += dampingFactor * pr[j] * matrix[j][i] / outDegree;
                    }
                }
            }
            
            // 更新PageRank值
            pr = newPr;
        }
        
        // 返回指定单词的PageRank值
        return pr[wordIndex];
    }
    
    /**
     * 随机游走算法
     * @param graph 图结构
     * @param startWord 起始单词，如果为null则随机选择
     * @param maxSteps 最大步数，如果为负数则无限制直到无法继续
     * @return 游走路径
     */
    public static List<String> randomWalk(TextGraph graph, String startWord, int maxSteps) {
        // 检查图是否为空
        if (graph == null || graph.getVertexCount() == 0) {
            return new ArrayList<>();
        }
        
        List<String> allWords = graph.getAllWords();
        int[][] matrix = graph.getAdjacencyMatrix();
        int vertexCount = allWords.size();
        
        // 确定起始节点
        String currentWord;
        if (startWord != null && graph.containsWord(startWord)) {
            currentWord = startWord;
        } else {
            // 随机选择一个起始节点
            Random random = new Random();
            currentWord = allWords.get(random.nextInt(vertexCount));
        }
        
        // 记录游走路径
        List<String> path = new ArrayList<>();
        path.add(currentWord);
        
        // 记录已经走过的边，格式为 "from-to"
        Set<String> visitedEdges = new HashSet<>();
        
        // 开始随机游走
        int steps = 0;
        while ((maxSteps < 0 || steps < maxSteps)) {
            // 获取当前节点的索引
            int currentIndex = allWords.indexOf(currentWord);
            
            // 查找当前节点的所有出边
            List<Integer> neighbors = new ArrayList<>();
            for (int i = 0; i < vertexCount; i++) {
                if (matrix[currentIndex][i] > 0) {
                    neighbors.add(i);
                }
            }
            
            // 如果没有出边，结束游走
            if (neighbors.isEmpty()) {
                break;
            }
            
            // 随机选择一个邻居
            Random random = new Random();
            int nextIndex = neighbors.get(random.nextInt(neighbors.size()));
            String nextWord = allWords.get(nextIndex);
            
            // 构造边的标识
            String edge = currentWord + "-" + nextWord;
            
            // 如果边已经访问过，结束游走
            if (visitedEdges.contains(edge)) {
                path.add(nextWord); // 添加最后一个节点
                break;
            }
            
            // 记录这条边
            visitedEdges.add(edge);
            
            // 移动到下一个节点
            currentWord = nextWord;
            path.add(currentWord);
            
            steps++;
        }
        
        return path;
    }
    
}