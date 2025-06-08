import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class calcShortestPathTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // 使用实验手册中给定的文本初始化图
        String text = "The scientist carefully analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again.";
        Main.graph = new TextGraph(text);
    }

    @Test
    void testCase2_Word1NotInGraph() {
        String result = Main.calcShortestPath("nonexistent", "scientist");
        assertEquals("起始单词 'nonexistent' 不存在于图中", result);
    }

    @Test
    void testCase3_Word2NotInGraph() {
        String result = Main.calcShortestPath("scientist", "nonexistent");
        assertEquals("目标单词 'nonexistent' 不存在于图中", result);
    }

    @Test
    void testCase4_NoBridgeWords() {
        String result = Main.calcShortestPath("again", "it");
        assertEquals("从 'again' 到 'it' 不存在路径", result);
    }

    @Test
    void testCase5_NormalCase() {
        String result = Main.calcShortestPath("the", "it");
        assertTrue(result.contains("the"));
        assertTrue(result.contains("scientist"));
        assertTrue(result.contains("analyzed"));
        assertTrue(result.contains("it"));
        assertTrue(result.contains("路径长度："));
    }
    @Test
    void testCase6_SelfLoop() {
        String result = Main.calcShortestPath("scientist", "scientist");
        assertTrue(result.contains("scientist") && result.contains("路径长度：0"));
    }

    @Test
    void testCase1_GraphNotInitialized() {
        Main.graph = null;
        String result = Main.calcShortestPath("any", "any");
        assertEquals("图结构未初始化", result);
    }
}