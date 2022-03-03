package com.uwallet.pay.main.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author: liming
 * @Date: 2020/8/10 09:24
 * @Description: 赫夫曼编码
 */

public class HuffmanCode {

    private Map<Byte, String> huffmanCodeMap;
    private long length;

    public Map<Byte, String> getHuffmanCodeMap() {
        return huffmanCodeMap;
    }

    public long getLength() {
        return length;
    }

    /**
     * 赫夫曼编码
     *
     * @param content 待编译数据
     * @return 编译后数据
     */
    public byte[] huffmanBuild(String content) {

        byte[] bytes = content.getBytes();

        // 获取树节点
        List<Node> nodes = getNodes(bytes);

        // 初始化赫夫曼编码表
        initHuffmanCodeMap(nodes.size());

        // 获取根节点
        Node root = getRoot(nodes);

        // 获取赫夫曼编码表
        getHuffmanCode(root);

        // 数据编译
        return getHuffmanCodeBytes(bytes, huffmanCodeMap);

    }

    /**
     * 初始化赫夫曼编码表
     *
     * @param size
     */
    private void initHuffmanCodeMap(int size) {
        size = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(0.75), 0, BigDecimal.ROUND_UP).intValue();
        if (size % 2 != 0) {
            size++;
        }
        huffmanCodeMap = new HashMap<>(size);
    }

    /**
     * 赫夫曼解码
     *
     * @param bytes          待解码数据
     * @param huffmanCodeMap 赫夫曼编码表
     * @return 解码后数据
     */
    public static byte[] huffmanDecode(byte[] bytes, long length, Map<Byte, String> huffmanCodeMap) {

        // 获取二进制字符
        String huffmanCode = huffmanCodeDealingWithStress(bytes, length);

        // todo 解码
//        System.out.println(huffmanCode);

        List<Byte> byteList = new ArrayList<>();

        Set<Map.Entry<Byte, String>> entries = huffmanCodeMap.entrySet();
        // 初始化容器 size
        int size = BigDecimal.valueOf(huffmanCodeMap.size()).divide(BigDecimal.valueOf(0.75), 0, BigDecimal.ROUND_UP).intValue();
        if (size % 2 != 0) {
            size++;
        }
        // 反转赫夫曼编码表, 将 key, value 交替位置
        Map<String, Byte> map = new HashMap<>(size);
        for (Map.Entry<Byte, String> entry : entries) {
            map.put(entry.getValue(), entry.getKey());
        }

        // 扫描二进制字符
        for (int i = 0; i < huffmanCode.length(); ) {

            // 辅助指针, 记录扫描字符的长度
            int point = 1;
            // 假定还未匹配到
            boolean flag = true;

            while (flag) {
                // 截取扫描到的二进制字符串
                String s = huffmanCode.substring(i, i + point);
                // 进行赫夫曼编码表匹配
                if (map.get(s) != null) {
                    // 匹配到则, 存储赫夫曼编码表中的 byte 数据, 修改标识位, 退出该循环
                    byteList.add(map.get(s));
                    flag = false;
                } else {
                    // 未匹配到, 则辅助指针后移
                    point++;
                    // 防止指针越界
                    if (i + point > huffmanCode.length()) {
                        break;
                    }
                }
            }

            // 退出循环后, i 指针移到 point + i 的位置上
            i += point;
        }

        byte[] bytes1 = new byte[byteList.size()];

        // 将集合数据转换成 byte[]
        for (int i = 0; i < byteList.size(); i++) {
            bytes1[i] = byteList.get(i);
        }

        return bytes1;
    }

    /**
     * 数据解压
     *
     * @param bytes 带解压数据
     * @return 二进制字符
     */
    private static String huffmanCodeDealingWithStress(byte[] bytes, long length) {

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {

            if (i == bytes.length - 1) {
                String byteToBinarySystem = byteToBinarySystem(bytes[i], false);
                long len = length - stringBuilder.length();
                if (len - byteToBinarySystem.length() <= 0) {
                    stringBuilder.append(byteToBinarySystem);
                } else {
                    stringBuilder.append(StringUtils.leftPad(byteToBinarySystem, (int) len, '0'));
                }
            } else {
                stringBuilder.append(byteToBinarySystem(bytes[i], true));
            }
        }

        return stringBuilder.toString();
    }

    /**
     * 将 byte 进行转码, 转为二进制字符
     *
     * @param b    byte 数据
     * @param flag 是否需要补码
     * @return 二进制字符
     */
    private static String byteToBinarySystem(byte b, boolean flag) {

        int temp = b;

        if (flag) {
            // 数据补位 256 二进制 => 1 0000 0000
            temp |= 256;
        }

        String binaryString = Integer.toBinaryString(temp);

        if (flag) {
            binaryString = binaryString.substring(binaryString.length() - 8);
        }

        return binaryString;
    }

    /**
     * 获取压缩后的 byte 数组
     *
     * @param bytes          原始 byte 数组
     * @param huffmanCodeMap 赫夫曼编码表
     * @return 经过赫夫曼编码后得到的 byte 数组
     */
    private byte[] getHuffmanCodeBytes(byte[] bytes, Map<Byte, String> huffmanCodeMap) {

        StringBuilder stringBuilder = new StringBuilder();


        // 赫夫曼编码
        for (byte b : bytes) {
            stringBuilder.append(huffmanCodeMap.get(b));
        }

        length = stringBuilder.length();

        // todo 编码
//        System.out.println(stringBuilder.toString());

        byte[] bytes1 = new byte[BigDecimal.valueOf(stringBuilder.length()).divide(BigDecimal.valueOf(8), 0, BigDecimal.ROUND_UP).intValue()];

        // 将其二进制字符进行每 8 位截取, 转换成一个 byte, (注: 最后一个二进制字符可能不足 8 位)
        for (int i = 0; i < bytes1.length; i++) {
            bytes1[i] =  Integer.valueOf(stringBuilder.substring(i * 8, Math.min((i + 1) * 8, stringBuilder.length())), 2).byteValue();
        }

        return bytes1;
    }

    /**
     * 获取树节点
     *
     * @param bytes 原始 byte 数组
     * @return 树节点集合
     */
    private List<Node> getNodes(byte[] bytes) {

        // 初始化容器
        int size = BigDecimal.valueOf(bytes.length).divide(BigDecimal.valueOf(0.75), 0, BigDecimal.ROUND_UP).intValue();
        if (size % 2 != 0) {
            size++;
        }

        // 统计字节出现的次数
        Map<Byte, Integer> map = new HashMap<>(size);
        // 树节点
        List<Node> nodes = new ArrayList<>(size);

        // 循环遍历每个字节
        for (byte b : bytes) {
            map.merge(b, 1, Integer::sum);
        }

        // 将统计出的数据, 构建成树节点集合
        for (Map.Entry<Byte, Integer> entry : map.entrySet()) {
            nodes.add(new Node(entry.getKey(), entry.getValue()));
        }

        return nodes;

    }

    /**
     * 构建赫夫曼树
     *
     * @param nodes
     * @return
     */
    private Node getRoot(List<Node> nodes) {

        while (nodes.size() > 1) {

            // 排序
            Collections.sort(nodes);

            // 取出权重最小的两个节点
            Node leftNode = nodes.get(0);
            Node rightNode = nodes.get(1);

            // 构建父节点, 父节点权重为左右子节点权重之和
            Node parent = new Node(null, leftNode.weight + rightNode.weight);

            // 关联左右节点
            parent.left = leftNode;
            parent.right = rightNode;

            // 从原集合中删除取出的节点
            nodes.remove(leftNode);
            nodes.remove(rightNode);

            // 将构建的新节点放入到集合中
            nodes.add(parent);
        }

        // 当循环退出, 则集合中只剩下根节点
        return nodes.get(0);
    }

    /**
     * 获取 赫夫曼编码表 (封装)
     *
     * @param root 根节点
     */
    private void getHuffmanCode(Node root) {

        if (root == null) {
            return;
        }

        StringBuilder cache = new StringBuilder();

        if (root.left != null) {
            getHuffmanCode(root.left, "0", cache);
        }
        if (root.right != null) {
            getHuffmanCode(root.right, "1", cache);
        }


    }

    /**
     * 获取赫夫曼编码表
     *
     * @param node  节点
     * @param code  路径标识: 0.左 1.右
     * @param cache 路径缓存
     */
    private void getHuffmanCode(Node node, String code, StringBuilder cache) {

        // 临时存储
        StringBuilder stringBuilder = new StringBuilder(cache);
        stringBuilder.append(code);

        // 如果左节点不为空
        if (node.left != null) {
            getHuffmanCode(node.left, "0", stringBuilder);
        }
        // 如果右节点不为空
        if (node.right != null) {
            getHuffmanCode(node.right, "1", stringBuilder);
        }

        // 如果当前节点为叶子节点, 则存储
        if (node.value != null) {
            huffmanCodeMap.put(node.value, stringBuilder.toString());
        }

    }


    // 如果想用 Collections.sort 方法进行排序, 则必须实现 Comparable<T> 接口, 并重写 compareTo() 方法
    class Node implements Comparable<Node> {
        private Byte value;
        private Integer weight;
        private Node left;
        private Node right;

        public Node(Byte value, Integer weight) {
            this.value = value;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "value=" + value +
                    ", weight=" + weight +
                    '}';
        }

        @Override
        public int compareTo(Node o) {
            // 从小到大, 如果加上 -(), 则从大到小
            return this.weight - o.weight;
        }

        public void preOrder() {

            System.out.println(this);
            if (this.left != null) {
                this.left.preOrder();
            }
            if (this.right != null) {
                this.right.preOrder();
            }


        }
    }
}
