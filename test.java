import java.util.PriorityQueue;
import java.util.HashMap;

class HuffmanNode {
    char data;           
    int frequency;       
    HuffmanNode left, right; 
    HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        left = right = null;
    }
}

public class test {
    public static StringBuilder printCodes(HuffmanNode root, StringBuilder code, StringBuilder encodeddata, HashMap<Character, String> codemap) {
        if (root == null) return encodeddata;
        if (root.data != '$') {
            if(code.isEmpty()){
                code.append("0");
            }
            encodeddata.append(root.data + ":" + code + "\n");
            codemap.put(root.data, code.toString());
        }
        if (root.left != null) {
            encodeddata = printCodes(root.left, code.append('0'), encodeddata, codemap);
            code.deleteCharAt(code.length() - 1);
        }
        if (root.right != null) {
            encodeddata = printCodes(root.right, code.append('1'), encodeddata, codemap);
            code.deleteCharAt(code.length() - 1);
        }
        return encodeddata;
    }

    public static String encodeFile(String message) {
        if(message.length() == 0){
            return "";
        }
        HashMap<Character, Integer> frequencymap = new HashMap<>();
        PriorityQueue<HuffmanNode> priorityqueue = new PriorityQueue<>((a, b) -> a.frequency - b.frequency);
        HashMap<Character, String> codemap = new HashMap<>();
        for (char c : message.toCharArray()) {
            frequencymap.put(c, frequencymap.getOrDefault(c, 0) + 1);
        }
        for (char c : frequencymap.keySet()) {
            priorityqueue.add(new HuffmanNode(c, frequencymap.get(c)));
        }
        while (priorityqueue.size() > 1) {
            HuffmanNode left = priorityqueue.poll();
            HuffmanNode right = priorityqueue.poll();
            HuffmanNode newNode = new HuffmanNode('$', left.frequency + right.frequency);
            newNode.left = left;
            newNode.right = right;
            priorityqueue.add(newNode);
        }
        HuffmanNode root = priorityqueue.poll();
        StringBuilder encodeddata = printCodes(root, new StringBuilder(), new StringBuilder(), codemap);
        encodeddata.append(":\n");
        if (message == "") return encodeddata.toString();
        for (char c : message.toCharArray()) {
            encodeddata.append(codemap.get(c));
        }
        return encodeddata.toString();
    }
    
    private static String decodeFile(String message) {
        if(message.length() == 0){
            return "";
        }
        HashMap<String, Character> codemap = new HashMap<>();
        int lastindexofcode = message.indexOf(":\n") - 2;
        int lastindexofmessage = message.indexOf(":\n") + 2;
        StringBuilder subcode = new StringBuilder();
        subcode.append(message.substring(0, lastindexofcode + 1));
        String submessage = message.substring(lastindexofmessage, message.length()); 
        subcode.append("\n");
        char value;
        String key;
        int lineend;
        while(!subcode.isEmpty()) {
            value = subcode.charAt(0);
            subcode.delete(0, 2);
            lineend = subcode.indexOf("\n");
            key = subcode.substring(0, lineend);
            codemap.put(key, value);
            subcode.delete(0, lineend + 1);
        }
        int n = submessage.length();
        StringBuilder decodeddata = new StringBuilder();
        StringBuilder temp = new StringBuilder(); 
        for (int i = 0; i < n; i++) {
            temp.append(submessage.charAt(i));
            if(codemap.containsKey(temp.toString())){
                decodeddata.append(codemap.get(temp.toString()));
                temp.delete(0, temp.length());
            }
        }
        return decodeddata.toString();
    }

    public static void main(String[] args) {
        String message = "hel:lo wo\tr\nl:d";
        System.out.println("Encoded data:");
        String encodeddata = encodeFile(message);
        System.out.println(encodeddata);
        System.out.println("Decoded data:"); 
        String decodeddata = decodeFile(encodeddata);
        System.out.println(decodeddata);
    }
}
