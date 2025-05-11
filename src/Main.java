import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Main {
    private static TextGraph graph;
    private static JFrame frame;
    private static JTextArea resultArea;
    private static JPanel graphPanel;
    
    public static void main(String[] args) {
        // 创建GUI界面
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
    
    private static void createAndShowGUI() {
        // 创建主窗口
        frame = new JFrame("文本图结构分析");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        
        // 创建面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton loadButton = new JButton("加载文本文件");
        JButton showGraphButton = new JButton("显示图结构");
        JButton queryBridgeButton = new JButton("查询桥接词");
        JButton generateTextButton = new JButton("生成新文本");
        JButton shortestPathButton = new JButton("计算最短路径");
        JButton pageRankButton = new JButton("计算PageRank");
        JButton randomWalkButton = new JButton("随机游走");
        
        buttonPanel.add(loadButton);
        buttonPanel.add(showGraphButton);
        buttonPanel.add(queryBridgeButton);
        buttonPanel.add(generateTextButton);
        buttonPanel.add(shortestPathButton);
        buttonPanel.add(pageRankButton);
        buttonPanel.add(randomWalkButton);
        
        // 创建结果显示区域
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        // 创建图形显示面板
        graphPanel = new JPanel();
        graphPanel.setPreferredSize(new Dimension(800, 400));
        graphPanel.setBorder(BorderFactory.createTitledBorder("图结构可视化"));
        
        // 添加组件到主面板
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(graphPanel, BorderLayout.SOUTH);
        
        // 设置按钮事件
        loadButton.addActionListener(e -> loadTextFile());
        showGraphButton.addActionListener(e -> {
            if (graph != null) {
                showDirectedGraph(graph);
            } else {
                resultArea.setText("请先加载文本文件");
            }
        });
        
        queryBridgeButton.addActionListener(e -> queryBridgeWordsGUI());
        generateTextButton.addActionListener(e -> generateNewTextGUI());
        shortestPathButton.addActionListener(e -> calcShortestPathGUI());
        pageRankButton.addActionListener(e -> calcPageRankGUI());
        randomWalkButton.addActionListener(e -> randomWalkGUI());
        
        // 显示窗口
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    private static void loadTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件", "txt"));
        int result = fileChooser.showOpenDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String text = readFile(selectedFile);
                // 处理文本并创建图
                graph = new TextGraph(text);
                resultArea.setText("文件加载成功：" + selectedFile.getName() + "\n");
                resultArea.append("图结构已创建，包含 " + graph.getVertexCount() + " 个单词节点和 " 
                                 + graph.getEdgeCount() + " 条边。");
            } catch (IOException e) {
                resultArea.setText("文件读取错误：" + e.getMessage());
            }
        }
    }
    
    private static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        return content.toString();
    }
    
    // 显示有向图
    public static void showDirectedGraph(TextGraph G) {
        // 在graphPanel上绘制图结构
        graphPanel.removeAll();
        
        // 创建自定义绘图面板
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 获取图的信息
                java.util.List<String> words = G.getAllWords();
                int[][] matrix = G.getAdjacencyMatrix();
                int nodeCount = words.size();
                
                if (nodeCount == 0) {
                    g2d.drawString("图中没有节点", 10, 20);
                    return;
                }
                
                // 计算节点位置（环形布局）
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = Math.min(centerX, centerY) - 50;
                
                // 存储节点位置
                Map<String, Point> nodePositions = new HashMap<>();
                
                // 绘制节点
                for (int i = 0; i < nodeCount; i++) {
                    double angle = 2 * Math.PI * i / nodeCount;
                    int x = (int) (centerX + radius * Math.cos(angle));
                    int y = (int) (centerY + radius * Math.sin(angle));
                    
                    // 存储节点位置
                    nodePositions.put(words.get(i), new Point(x, y));
                    
                    // 绘制节点
                    g2d.setColor(Color.BLUE);
                    g2d.fillOval(x - 15, y - 15, 30, 30);
                    
                    // 绘制节点标签
                    g2d.setColor(Color.BLACK);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(words.get(i));
                    g2d.drawString(words.get(i), x - textWidth / 2, y + 25);
                }
                
                // 绘制边
                g2d.setColor(Color.RED);
                for (int i = 0; i < nodeCount; i++) {
                    for (int j = 0; j < nodeCount; j++) {
                        if (matrix[i][j] > 0) {
                            String word1 = words.get(i);
                            String word2 = words.get(j);
                            Point p1 = nodePositions.get(word1);
                            Point p2 = nodePositions.get(word2);
                            
                            // 计算箭头
                            drawArrow(g2d, p1.x, p1.y, p2.x, p2.y);
                            
                            // 绘制权重
                            int weightX = (p1.x + p2.x) / 2;
                            int weightY = (p1.y + p2.y) / 2;
                            g2d.drawString(String.valueOf(matrix[i][j]), weightX, weightY);
                        }
                    }
                }
            }
            
            // 绘制箭头
            private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
                // 计算方向向量
                double dx = x2 - x1;
                double dy = y2 - y1;
                double length = Math.sqrt(dx * dx + dy * dy);
                
                // 单位向量
                double unitDx = dx / length;
                double unitDy = dy / length;
                
                // 调整起点和终点（避免箭头与节点重叠）
                int adjustedX1 = (int) (x1 + unitDx * 15);
                int adjustedY1 = (int) (y1 + unitDy * 15);
                int adjustedX2 = (int) (x2 - unitDx * 15);
                int adjustedY2 = (int) (y2 - unitDy * 15);
                
                // 绘制线段
                g2d.drawLine(adjustedX1, adjustedY1, adjustedX2, adjustedY2);
                
                // 绘制箭头
                int arrowSize = 8;
                double angle = Math.atan2(dy, dx);
                int x3 = (int) (adjustedX2 - arrowSize * Math.cos(angle - Math.PI / 6));
                int y3 = (int) (adjustedY2 - arrowSize * Math.sin(angle - Math.PI / 6));
                int x4 = (int) (adjustedX2 - arrowSize * Math.cos(angle + Math.PI / 6));
                int y4 = (int) (adjustedY2 - arrowSize * Math.sin(angle + Math.PI / 6));
                
                int[] xPoints = {adjustedX2, x3, x4};
                int[] yPoints = {adjustedY2, y3, y4};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }
        };
        
        drawingPanel.setPreferredSize(new Dimension(graphPanel.getWidth(), graphPanel.getHeight()));
        
        // 添加绘图面板到图形面板
        graphPanel.setLayout(new BorderLayout());
        graphPanel.add(drawingPanel, BorderLayout.CENTER);
        
        // 添加缩放控制
        JPanel controlPanel = new JPanel();
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        graphPanel.add(controlPanel, BorderLayout.SOUTH);
        
        graphPanel.revalidate();
        graphPanel.repaint();
        
        resultArea.setText("图结构已显示，共有 " + G.getVertexCount() + " 个节点和 " + G.getEdgeCount() + " 条边。");
    }
    
    // 查询桥接词的GUI实现
    private static void queryBridgeWordsGUI() {
        if (graph == null) {
            resultArea.setText("请先加载文本文件");
            return;
        }
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        JTextField word1Field = new JTextField();
        JTextField word2Field = new JTextField();
        inputPanel.add(new JLabel("单词1:"));
        inputPanel.add(word1Field);
        inputPanel.add(new JLabel("单词2:"));
        inputPanel.add(word2Field);
        
        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "查询桥接词", 
                                                  JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String word1 = word1Field.getText().trim().toLowerCase();
            String word2 = word2Field.getText().trim().toLowerCase();
            String bridgeWords = queryBridgeWords(word1, word2);
            resultArea.setText("桥接词查询结果：\n" + bridgeWords);
        }
    }
    
    // 查询桥接词
    public static String queryBridgeWords(String word1, String word2) {
        if (graph == null) {
            return "图结构未初始化";
        }
        
        // 检查单词是否在图中
        if (!graph.containsWord(word1) || !graph.containsWord(word2)) {
            return "No " + (!graph.containsWord(word1) ? "word1" : "word2") + " or " + 
                   (!graph.containsWord(word2) ? "word2" : "word1") + " in the graph!";
        }
        
        // 获取桥接词
        java.util.List<String> bridgeWords = graph.getBridgeWords(word1, word2);
        
        // 根据桥接词数量构建返回信息
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            StringBuilder result = new StringBuilder("The bridge words from " + word1 + " to " + word2 + " are: ");
            
            if (bridgeWords.size() == 1) {
                result.append(bridgeWords.get(0)).append(".");
            } else {
                for (int i = 0; i < bridgeWords.size() - 1; i++) {
                    result.append(bridgeWords.get(i)).append(", ");
                }
                result.append("and ").append(bridgeWords.get(bridgeWords.size() - 1)).append(".");
            }
            
            return result.toString();
        }
    }
    
    // 生成新文本的GUI实现
    private static void generateNewTextGUI() {
        if (graph == null) {
            resultArea.setText("请先加载文本文件");
            return;
        }
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(5, 30);
        inputPanel.add(new JLabel("输入文本:"), BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "生成新文本", 
                                                  JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String inputText = textArea.getText().trim();
            String newText = generateNewText(inputText);
            resultArea.setText("生成的新文本：\n" + newText);
        }
    }
    
    // 根据bridge word生成新文本
    public static String generateNewText(String inputText) {
        if (graph == null) {
            return "图结构未初始化";
        }
        
        // 处理输入文本，提取单词
        String processedText = inputText.toLowerCase();
        processedText = processedText.replaceAll("[\\p{Punct}]", " ");
        String[] words = processedText.split("\\s+");
        
        // 过滤非字母字符
        java.util.List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            word = word.replaceAll("[^a-z]", "");
            if (!word.isEmpty()) {
                filteredWords.add(word);
            }
        }
        
        // 如果单词数量少于2，无法生成新文本
        if (filteredWords.size() < 2) {
            return inputText;
        }
        
        // 生成新文本
        StringBuilder newText = new StringBuilder(filteredWords.get(0));
        
        for (int i = 0; i < filteredWords.size() - 1; i++) {
            String word1 = filteredWords.get(i);
            String word2 = filteredWords.get(i + 1);
            
            // 查找桥接词
            java.util.List<String> bridgeWords = graph.getBridgeWords(word1, word2);
            
            // 如果存在桥接词，随机选择一个插入
            if (!bridgeWords.isEmpty()) {
                // 随机选择一个桥接词
                Random random = new Random();
                String bridgeWord = bridgeWords.get(random.nextInt(bridgeWords.size()));
                
                // 添加桥接词和下一个单词
                newText.append(" ").append(bridgeWord).append(" ").append(word2);
            } else {
                // 如果不存在桥接词，直接添加下一个单词
                newText.append(" ").append(word2);
            }
        }
        
        return newText.toString();
    }
    
    // 计算最短路径的GUI实现
    private static void calcShortestPathGUI() {
        if (graph == null) {
            resultArea.setText("请先加载文本文件");
            return;
        }
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        JTextField word1Field = new JTextField();
        JTextField word2Field = new JTextField();
        inputPanel.add(new JLabel("起始单词:"));
        inputPanel.add(word1Field);
        inputPanel.add(new JLabel("目标单词:"));
        inputPanel.add(word2Field);
        
        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "计算最短路径", 
                                                  JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String word1 = word1Field.getText().trim().toLowerCase();
            String word2 = word2Field.getText().trim().toLowerCase();
            String path = calcShortestPath(word1, word2);
            resultArea.setText("最短路径结果：\n" + path);
        }
    }
    
    // 计算两个单词之间的最短路径
    public static String calcShortestPath(String word1, String word2) {
        if (graph == null) {
            return "图结构未初始化";
        }
        
        // 检查单词是否存在于图中
        if (!graph.containsWord(word1)) {
            return "起始单词 '" + word1 + "' 不存在于图中";
        }
        
        if (!graph.containsWord(word2)) {
            return "目标单词 '" + word2 + "' 不存在于图中";
        }
        
        // 计算最短路径
        java.util.List<String> path = GraphAlgorithms.shortestPath(graph, word1, word2);
        
        // 如果路径为空，表示不可达
        if (path.isEmpty()) {
            return "从 '" + word1 + "' 到 '" + word2 + "' 不存在路径";
        }
        
        // 计算路径长度（边权值之和）
        int pathLength = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            pathLength += graph.getEdgeWeight(path.get(i), path.get(i + 1));
        }
        
        // 在图上显示路径
        showPathInGraph((java.util.List<String>) path);
        
        // 构建路径字符串
        StringBuilder pathStr = new StringBuilder();
        for (int i = 0; i < path.size() - 1; i++) {
            pathStr.append(path.get(i)).append(" → ");
        }
        pathStr.append(path.get(path.size() - 1));
        
        return "从 '" + word1 + "' 到 '" + word2 + "' 的最短路径为：\n" + 
               pathStr.toString() + "\n" +
               "路径长度：" + pathLength;
    }
    
    // 在图上显示路径
    private static void showPathInGraph(java.util.List<String> path) {
        // 创建自定义绘图面板，高亮显示路径
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 获取图的信息
                java.util.List<String> words = graph.getAllWords();
                int[][] matrix = graph.getAdjacencyMatrix();
                int nodeCount = words.size();
                
                if (nodeCount == 0) {
                    g2d.drawString("图中没有节点", 10, 20);
                    return;
                }
                
                // 计算节点位置（环形布局）
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = Math.min(centerX, centerY) - 50;
                
                // 存储节点位置
                Map<String, Point> nodePositions = new HashMap<>();
                
                // 绘制节点
                for (int i = 0; i < nodeCount; i++) {
                    double angle = 2 * Math.PI * i / nodeCount;
                    int x = (int) (centerX + radius * Math.cos(angle));
                    int y = (int) (centerY + radius * Math.sin(angle));
                    
                    // 存储节点位置
                    nodePositions.put(words.get(i), new Point(x, y));
                    
                    // 检查节点是否在路径中
                    if (path.contains(words.get(i))) {
                        // 路径中的节点用绿色显示
                        g2d.setColor(Color.GREEN);
                    } else {
                        // 其他节点用蓝色显示
                        g2d.setColor(Color.BLUE);
                    }
                    
                    g2d.fillOval(x - 15, y - 15, 30, 30);
                    
                    // 绘制节点标签
                    g2d.setColor(Color.BLACK);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(words.get(i));
                    g2d.drawString(words.get(i), x - textWidth / 2, y + 25);
                }
                
                // 绘制边
                for (int i = 0; i < nodeCount; i++) {
                    for (int j = 0; j < nodeCount; j++) {
                        if (matrix[i][j] > 0) {
                            String word1 = words.get(i);
                            String word2 = words.get(j);
                            Point p1 = nodePositions.get(word1);
                            Point p2 = nodePositions.get(word2);
                            
                            // 检查边是否在路径中
                            boolean isInPath = false;
                            for (int k = 0; k < path.size() - 1; k++) {
                                if (path.get(k).equals(word1) && path.get(k + 1).equals(word2)) {
                                    isInPath = true;
                                    break;
                                }
                            }
                            
                            if (isInPath) {
                                // 路径中的边用绿色显示
                                g2d.setColor(Color.GREEN);
                                // 加粗显示
                                g2d.setStroke(new BasicStroke(2.5f));
                            } else {
                                // 其他边用红色显示
                                g2d.setColor(Color.RED);
                                g2d.setStroke(new BasicStroke(1.0f));
                            }
                            
                            // 计算箭头
                            drawArrow(g2d, p1.x, p1.y, p2.x, p2.y);
                            
                            // 绘制权重
                            int weightX = (p1.x + p2.x) / 2;
                            int weightY = (p1.y + p2.y) / 2;
                            g2d.drawString(String.valueOf(matrix[i][j]), weightX, weightY);
                        }
                    }
                }
            }
            
            // 绘制箭头
            private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
                // 计算方向向量
                double dx = x2 - x1;
                double dy = y2 - y1;
                double length = Math.sqrt(dx * dx + dy * dy);
                
                // 单位向量
                double unitDx = dx / length;
                double unitDy = dy / length;
                
                // 调整起点和终点（避免箭头与节点重叠）
                int adjustedX1 = (int) (x1 + unitDx * 15);
                int adjustedY1 = (int) (y1 + unitDy * 15);
                int adjustedX2 = (int) (x2 - unitDx * 15);
                int adjustedY2 = (int) (y2 - unitDy * 15);
                
                // 绘制线段
                g2d.drawLine(adjustedX1, adjustedY1, adjustedX2, adjustedY2);
                
                // 绘制箭头
                int arrowSize = 8;
                double angle = Math.atan2(dy, dx);
                int x3 = (int) (adjustedX2 - arrowSize * Math.cos(angle - Math.PI / 6));
                int y3 = (int) (adjustedY2 - arrowSize * Math.sin(angle - Math.PI / 6));
                int x4 = (int) (adjustedX2 - arrowSize * Math.cos(angle + Math.PI / 6));
                int y4 = (int) (adjustedY2 - arrowSize * Math.sin(angle + Math.PI / 6));
                
                int[] xPoints = {adjustedX2, x3, x4};
                int[] yPoints = {adjustedY2, y3, y4};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }
        };
        
        drawingPanel.setPreferredSize(new Dimension(graphPanel.getWidth(), graphPanel.getHeight()));
        
        // 添加绘图面板到图形面板
        graphPanel.removeAll();
        graphPanel.setLayout(new BorderLayout());
        graphPanel.add(drawingPanel, BorderLayout.CENTER);
        
        // 添加缩放控制
        JPanel controlPanel = new JPanel();
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        graphPanel.add(controlPanel, BorderLayout.SOUTH);
        
        graphPanel.revalidate();
        graphPanel.repaint();
    }
    
    // 计算PageRank的GUI实现
    private static void calcPageRankGUI() {
        if (graph == null) {
            resultArea.setText("请先加载文本文件");
            return;
        }
        
        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        JTextField wordField = new JTextField();
        inputPanel.add(new JLabel("单词:"));
        inputPanel.add(wordField);
        
        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "计算PageRank值", 
                                                  JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String word = wordField.getText().trim().toLowerCase();
            Double prValue = calcPageRank(word);
            resultArea.setText("单词 '" + word + "' 的PageRank值：" + prValue);
        }
    }
    
    // 计算单词的PR值
    public static Double calcPageRank(String word) {
        if (graph == null) {
            return -1.0;
        }
        
        if (!graph.containsWord(word)) {
            return -1.0; // 单词不存在于图中
        }
        
        // 使用阻尼系数0.85，迭代100次计算PageRank
        return GraphAlgorithms.pageRank(graph, word, 0.85, 100);
    }

    // 随机游走的GUI实现
    private static void randomWalkGUI() {
        if (graph == null) {
            resultArea.setText("请先加载文本文件");
            return;
        }
        
        // 调用随机游走函数
        String walkResult = randomWalk();
        resultArea.setText("随机游走结果：\n" + walkResult);
    }

    // 执行随机游走并返回结果
    public static String randomWalk() {
        if (graph == null) {
            return "图结构未初始化";
        }
        // 创建对话框
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        JTextField startWordField = new JTextField();
        JCheckBox randomStartCheckBox = new JCheckBox("随机选择起点", true);
        JCheckBox showProcessCheckBox = new JCheckBox("显示游走过程", true);
        
        inputPanel.add(new JLabel("起始单词:"));
        inputPanel.add(startWordField);
        inputPanel.add(randomStartCheckBox);
        inputPanel.add(new JLabel(""));
        inputPanel.add(showProcessCheckBox);
        inputPanel.add(new JLabel(""));
        
        // 当选择随机起始节点时，禁用起始单词输入框
        randomStartCheckBox.addActionListener(e -> {
            startWordField.setEnabled(!randomStartCheckBox.isSelected());
        });
        startWordField.setEnabled(!randomStartCheckBox.isSelected());
        
        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "随机游走", 
                                                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return "用户取消了操作";
        }
        
        // 获取参数
        final String startWord = randomStartCheckBox.isSelected() ? null : startWordField.getText().trim().toLowerCase();
        final boolean showProcess = showProcessCheckBox.isSelected();
        
        // 创建一个标志，用于停止游走
        final boolean[] stopWalk = {false};
        
        // 创建对话框和相关组件的引用
        final JDialog[] walkDialogRef = new JDialog[1];
        final JTextArea[] walkTextAreaRef = new JTextArea[1];
        final JButton[] stopButtonRef = new JButton[1];
        
        if (showProcess) {
            // 创建对话框
            JDialog walkDialog = new JDialog(frame, "随机游走进行中", false);
            walkDialog.setLayout(new BorderLayout());
            
            // 创建文本区域
            JTextArea walkTextArea = new JTextArea(20, 40);
            walkTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(walkTextArea);
            
            // 创建停止按钮
            JButton stopButton = new JButton("停止游走");
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(stopButton);
            
            walkDialog.add(scrollPane, BorderLayout.CENTER);
            walkDialog.add(buttonPanel, BorderLayout.SOUTH);
            walkDialog.setSize(500, 400);
            walkDialog.setLocationRelativeTo(frame);
            
            // 存储引用
            walkDialogRef[0] = walkDialog;
            walkTextAreaRef[0] = walkTextArea;
            stopButtonRef[0] = stopButton;
            
            // 停止按钮事件
            stopButton.addActionListener(e -> {
                stopWalk[0] = true;
                walkDialog.dispose();
            });
            
            walkDialog.setVisible(true);
        }
        
        // 创建一个单独的线程来执行随机游走，避免UI冻结
        Thread walkThread = new Thread(() -> {
            try {
                // 执行随机游走
                java.util.List<String> allWords = graph.getAllWords();
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
                java.util.List<String> path = new ArrayList<>();
                path.add(currentWord);
                
                if (walkTextAreaRef[0] != null) {
                    walkTextAreaRef[0].append("开始随机游走，起始节点: " + currentWord + "\n");
                }
                
                // 记录已经走过的边，格式为 "from-to"
                Set<String> visitedEdges = new HashSet<>();
                
                // 开始随机游走
                while (!stopWalk[0]) {
                    // 获取当前节点的索引
                    int currentIndex = allWords.indexOf(currentWord);
                    
                    // 查找当前节点的所有出边
                    java.util.List<Integer> neighbors = new ArrayList<>();
                    for (int i = 0; i < vertexCount; i++) {
                        if (matrix[currentIndex][i] > 0) {
                            neighbors.add(i);
                        }
                    }
                    
                    // 如果没有出边，结束游走
                    if (neighbors.isEmpty()) {
                        if (walkTextAreaRef[0] != null) {
                            walkTextAreaRef[0].append("节点 " + currentWord + " 没有出边，游走结束\n");
                        }
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
                        if (walkTextAreaRef[0] != null) {
                            walkTextAreaRef[0].append("边 " + edge + " 已经访问过，游走结束\n");
                        }
                        path.add(nextWord); // 添加最后一个节点
                        break;
                    }
                    
                    // 记录这条边
                    visitedEdges.add(edge);
                    
                    // 显示当前步骤
                    if (walkTextAreaRef[0] != null) {
                        walkTextAreaRef[0].append(currentWord + " → " + nextWord + "\n");
                    }
                    
                    // 移动到下一个节点
                    currentWord = nextWord;
                    path.add(currentWord);
                    
                    // 短暂暂停，使用户可以看到游走过程
                    if (showProcess) {
                        Thread.sleep(500);
                    }
                }
                
                // 关闭进度对话框
                if (walkDialogRef[0] != null && walkDialogRef[0].isVisible()) {
                    SwingUtilities.invokeLater(() -> walkDialogRef[0].dispose());
                }
                
                // 在图上显示路径
                SwingUtilities.invokeLater(() -> showPathInGraph(path));
                
                // 构建路径字符串
                StringBuilder pathStr = new StringBuilder("随机游走路径：\n");
                for (int i = 0; i < path.size() - 1; i++) {
                    pathStr.append(path.get(i)).append(" → ");
                }
                pathStr.append(path.get(path.size() - 1));
                
                // 添加路径长度信息
                pathStr.append("\n\n共经过 ").append(path.size()).append(" 个节点");
                
                // 更新UI显示结果
                final String finalResult = pathStr.toString();
                SwingUtilities.invokeLater(() -> {
                    resultArea.setText(finalResult);
                    
                    // 询问是否保存到文件
                    int saveResult = JOptionPane.showConfirmDialog(frame, 
                            "是否将游走路径保存到文件？", 
                            "保存路径", JOptionPane.YES_NO_OPTION);
                    
                    if (saveResult == JOptionPane.YES_OPTION) {
                        saveWalkPathToFile(path);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                if (walkDialogRef[0] != null && walkDialogRef[0].isVisible()) {
                    walkDialogRef[0].dispose();
                }
                SwingUtilities.invokeLater(() -> resultArea.setText("游走过程中发生错误: " + e.getMessage()));
            }
        });
        
        walkThread.start();
        
        // 返回初始提示信息
        return "正在执行随机游走，请稍候...";
    }

    // 保存随机游走路径到文件
    private static void saveWalkPathToFile(java.util.List<String> path) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存随机游走路径");
        fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件", "txt"));
            
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 确保文件有.txt扩展名
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("随机游走路径:");
                
                // 写入路径
                for (int i = 0; i < path.size(); i++) {
                    writer.print(path.get(i));
                    if (i < path.size() - 1) {
                        writer.print(" ");
                    }
                }
                
                writer.println("\n\n共经过 " + path.size() + " 个节点");
                resultArea.append("\n\n路径已保存到文件: " + file.getAbsolutePath());
            } catch (IOException e) {
                resultArea.append("\n\n保存文件时发生错误: " + e.getMessage());
            }
        }
    }
    
}