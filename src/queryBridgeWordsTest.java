import static org.junit.jupiter.api.Assertions.*;

class queryBridgeWordsTest {

    private TextGraph graph;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // 使用题目中给定的文本初始化图
        String text = "The scientist carefully analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again.The scientist quickly analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again.";
        graph = new TextGraph(text);
    }

    @org.junit.jupiter.api.Test
    void testCase1_GraphNotInitialized() {
        // 测试用例1：图结构未初始化（等价类5）
        Main.graph = null;
        String result = Main.queryBridgeWords("any", "any");
        assertEquals("图结构未初始化", result);
    }

    @org.junit.jupiter.api.Test
    void testCase2_Word1NotInGraph() {
        // 测试用例2：word1不在图中（等价类1,6）
        Main.graph = graph;
        String result = Main.queryBridgeWords("nonexistent", "team");
        assertEquals("No word1 or word1 in the graph!", result);
    }

    @org.junit.jupiter.api.Test
    void testCase3_Word2NotInGraph() {
        // 测试用例3：word2不在图中（等价类1,7）
        Main.graph = graph;
        String result = Main.queryBridgeWords("scientist", "nonexistent");
        assertEquals("No word2 or word2 in the graph!", result);
    }

    @org.junit.jupiter.api.Test
    void testCase4_BothWordsNotInGraph() {
        // 测试用例4：word1和word2都不在图中（等价类1,8）
        Main.graph = graph;
        String result = Main.queryBridgeWords("nonexistent1", "nonexistent2");
        assertEquals("No word1 or word2 in the graph!", result);
    }

    @org.junit.jupiter.api.Test
    void testCase5_SingleBridgeWord() {
        // 测试用例5：存在一个桥接词（等价类1,2,3）
        Main.graph = graph;
        String result = Main.queryBridgeWords("more", "so");
        assertEquals("The bridge words from more to so are: data.", result);
    }

    @org.junit.jupiter.api.Test
    void testCase6_MultipleBridgeWords() {
        // 测试用例6：存在多个桥接词（等价类1,2,4）
        Main.graph = graph;
        String result = Main.queryBridgeWords("scientist", "analyzed");
        assertTrue(result.startsWith("The bridge words from scientist to analyzed are:"));
        // 由于桥接词可能有多个，且顺序不确定，所以只检查前缀
        assertTrue(result.contains("carefully"));
        assertTrue(result.contains("quickly"));
    }

    @org.junit.jupiter.api.Test
    void testCase7_NoBridgeWords() {
        // 测试用例7：不存在桥接词（等价类1,2,9）
        Main.graph = graph;
        String result = Main.queryBridgeWords("the", "scientist");
        assertEquals("No bridge words from the to scientist!", result);
    }
}