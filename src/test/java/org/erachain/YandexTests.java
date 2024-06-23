package org.erachain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

;

@Slf4j
public class YandexTests {


    /*
 ====================== шаблон для задач - работа с потоками
 ========= A. A+B 1
import java.io.*;

public class WooHoo {
    public static void main(String[] args) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        String[] s = r.readLine().split(" ");

        System.out.println(Integer.parseInt(s[0]) + Integer.parseInt(s[1]));
    }
}

============== - работа с файлами в Яндекс задачах

import java.io.*;

public class Program {

    public static void main(String[] args) {

        String[] vals = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
            vals = reader.readLine().split(" ");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int a = Integer.parseInt(vals[0]);
        int b = Integer.parseInt(vals[1]);

        try(FileWriter writer = new FileWriter("output.txt", false))
        {
            writer.write("" + (a+b));
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}

==================== работа со стандартными каналами
import java.util.Scanner;

public class Program {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        int a = in.nextInt();
        int b = in.nextInt();
        in.close();

        System.out.println(a + b);
    }
}

=========================== Камни и украшения

import java.util.Scanner;

public class Program {

    public static void main(String[] args) {

		String a = null, b = null;
        try {
            Scanner in = new Scanner(System.in);
            a = in.next();
            b = in.next();
            in.close();
        } catch (Exception e) {
            System.out.println(0);
            return;
        }

        int c = 0;
        for(int i=0; i<b.length(); i++) {
        	if (a.contains("" + b.charAt(i)))
            	c++;
        }

        System.out.println(c);
    }
}
     */

    public static void mainFile(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("file.txt"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mainSys(String[] args) {

        Scanner in = new Scanner(System.in);
        System.out.print("Input a number: ");
        int num = in.nextInt();
        String[] str = in.next().split(" ");
        in.close();

        System.out.println(Integer.parseInt(str[0]) * Integer.parseInt(str[1]));
    }

    // Yandex test
    private void gen(String res, int open, int closed, int n) {
        if (res.length() == 2 * n) {
            if (open == closed) {
                System.out.println(res);
            }
            return;
        }
        gen(res + "(", open + 1, closed, n);

        if (closed < open)
            gen(res + ")", open, closed + 1, n);

    }

    @Ignore
    @Test
    public void testParens() {

        gen("", 0, 0, 3);

    }

    public boolean isValid_1(String s) {
        if ((s.length() >> 1) << 1 != s.length())
            return false;

        Stack<String> stack = new Stack<>();

        for (int i = 0; i < s.length(); i++) {
            char v = s.charAt(i);

            if (v == '(' || v == '{' || v == '[') {
                stack.push("" + v);
            } else if (v == ')') {
                if (!stack.pop().equals("("))
                    return false;
            } else if (v == '}') {
                if (!stack.pop().equals("{"))
                    return false;
            } else if (v == ']') {
                if (!stack.pop().equals("["))
                    return false;
            }
        }

        return stack.isEmpty();
    }

    @Ignore
    @Test
    public void testSS() {

        isValid_1("{}");

    }


    public class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode fast = head, slow = head;
        for (int i = 0; i < n; i++) fast = fast.next;
        if (fast == null) return head.next;
        while (fast.next != null) {
            fast = fast.next;
            slow = slow.next;
        }
        slow.next = slow.next.next;
        return head;
    }

    @Ignore
    @Test
    public void testLN_2() {
        removeNthFromEnd(new ListNode(1, new ListNode(2, new ListNode(3, new ListNode(4, new ListNode(5))))),
                1);

    }

}