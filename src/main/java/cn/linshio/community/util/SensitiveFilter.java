package cn.linshio.community.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
//过滤敏感词汇
public class SensitiveFilter {

    //要进行替换的符号
    public static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //初始化这个树 ,并且在程序执行完自动调用
    @PostConstruct
    public void init(){
        //读取敏感词文件
        try (
            InputStream ras = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ras));
            ) {
            String keyword;
            while ((keyword = bufferedReader.readLine())!=null){
                this.addKeyword(keyword);
            }
        }catch (IOException e){
            log.error("加载敏感词文件失败"+e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中去
    private void addKeyword(String keyword){
        //创建一个临时节点
        TrieNode tempNode = rootNode;
        //遍历数据
        for (int i = 0; i < keyword.length(); i++) {
            char temp = keyword.charAt(i);
            TrieNode childNode = tempNode.getTrieNode(temp);
            //如果子节点为空就初始化子节点
            if (childNode==null){
                 childNode = new TrieNode();
                 tempNode.addTrieNode(temp,childNode);
            }
            //如果子节点存在就将临时节点指向子节点
            tempNode = childNode;
            //如果到了最后一个字符要进行标记
            if (i==(keyword.length()-1)){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     *
     * @param text 用户输入的文本
     * @return 经过处理屏蔽关键字后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()) {
                Character c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {
                    if (tempNode == rootNode) {
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getTrieNode(c);
                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;
                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                }
                // 检查下一个字符
                else {
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }
        return sb.toString();
    }

    //判断是否是符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //树形节点内部类 ，前缀树
    private class TrieNode{
        //关键词结束标志
        private boolean isKeywordEnd = false;
        //子节点（key为下级字符value为下级节点）
        Map<Character,TrieNode> trieNodeMap = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }
        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
        //添加子节点
        public void addTrieNode(Character character,TrieNode trieNode){
            trieNodeMap.put(character, trieNode);
        }
        //获取子节点
        public TrieNode getTrieNode(Character character){
            return trieNodeMap.get(character);
        }
    }
}
