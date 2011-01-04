/*
 *  BFI
 *  Copyright (C) 2003 Thomas Cort
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package hellobrainfuck;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *  BFI - Compliant with The ENSI BrainFuck Language Specification v1.3, and
 *  also compliant with The Portable Brainfuck Specification.
 *  @author Thomas Cort (<A HREF="mailto:tom@tomcort.com">tom@tomcort.com</A>)
 *  @version 1.1 2003-03-16
 *  @since   1.0
 */
public class BFI implements Runnable {

    protected final int MAX_DATA_SIZE = 10000;
    protected byte[] x;
    public char[] c;
    public int p, pc, l;
    public String res;
    boolean error;

    /**
     *  Constructor
     *  @param String - Brainfuck program to interpret.
     *  @since 1.0
     */
    public BFI(String s) {
        x = new byte[MAX_DATA_SIZE + 1];
        c = s.toCharArray();
        for (int i = 0; i < x.length; i++) {
            x[i] = 0;
        }
        p = l = pc = 0;
        error = false;
        res = "";
    }

    @Override
    public void run() {
        try {
            try {
                this.checkSyntax();
                this.interpret();
            } catch (Error e) {
                this.error = true;
                pc=10000;
            }
        } catch (Exception a) {
            this.error = true;
            pc=10000;
        }
    }

    public void stop() {
        p = 100000;
        pc = 100000;
        l = 100000;
        error = true;
    }

    /**
     *  Evaluates the current character in the Brainfuck program
     *  @since 1.0
     */
    public void interpret() {
        for (pc = 0; pc < c.length; pc++) {
            if (c[pc] == '>') {
                incrementPointer();
            } else if (c[pc] == '<') {
                decrementPointer();
            } else if (c[pc] == '+') {
                incrementByteAtPointer();
            } else if (c[pc] == '-') {
                decrementByteAtPointer();
            } else if (c[pc] == '.') {
                output();
            } else if (c[pc] == ',') {
                input();
            } else if (c[pc] == '[') {
                startJump();
            } else if (c[pc] == ']') {
                endJump();
            }
        }
    }

    /**
     *  Checks sytanx. Must be called explicitly before you call interpret() if
     *  you want to check the syntax.
     *  @since 1.0
     */
    public void checkSyntax() {
        if(c.length==0)errBF("");

        int lt = 0, gt = 0, ob = 0, cb = 0, ptrAt = 0;

        for (int t = 0; t < c.length; t++) {
            if (c[t] == '[') {
                ob++;
            } else if (c[t] == ']') {
                cb++;
            }
            if (ob < cb) {
                errBF("");
            }
        }

        if (ob != cb) {
            errBF(ob > cb ? "Missing closing bracket(s)" : "Missing opening bracket(s)");
        }
    }

    /**
     *  '>' Increment pointer.
     *  @since 1.0
     */
    public void incrementPointer() {
        if (++p > MAX_DATA_SIZE) {
            errBF("Pointer moved beyond MAX_DATA_SIZE: " + MAX_DATA_SIZE);
        }
    }

    /**
     *  '<' Decrement pointer.
     *  @since 1.0
     */
    public void decrementPointer() {
        if (--p < 0) {
            errBF("Pointer moved to the left of the starting position");
        }
    }

    /**
     *  '+' Increments the value at the pointer.
     *  @since 1.0
     */
    public void incrementByteAtPointer() {
        if (x[p] == 127) {
            errBF("Byte incremented beyond capacity");
        }
        ++x[p];
    }

    /**
     *  '-' Dencrements the value at the pointer.
     *  @since 1.0
     */
    public void decrementByteAtPointer() {
        if (x[p] == -128) {
            errBF("Byte decremented below capacity");
        }
        --x[p];
    }

    /**
     *  '[' Start of loop code block.
     *  @since 1.0
     */
    public void startJump() {
        if (x[p] == 0) {
            pc++;
            while (l > 0 || c[pc] != ']') {
                if (c[pc] == '[') {
                    l++;
                }
                if (c[pc] == ']') {
                    l--;
                }
                pc++;
            }
        }
    }

    /**
     *  ']' End of loop code block.
     *  @since 1.0
     */
    public void endJump() {
        pc--;
        while (l > 0 || c[pc] != '[') {
            if (c[pc] == ']') {
                l++;
            }
            if (c[pc] == '[') {
                l--;
            }
            pc--;
        }
        pc--;
    }

    /**
     *  '.' converts current byte to ascii & prints it to STDOUT
     *  @since 1.0
     */
    public void output() {
        byte[] out = {x[p]};
        String s = new String(out);
 //       System.out.print(s);
        res += s;
    }

    /**
     *  ',' reads a character from STDIN, and stores it at the current location
     *  @since 1.0
     */
    public void input() {
        BufferedReader Stream =
                new BufferedReader(new InputStreamReader(System.in));

        try {
            String str = Stream.readLine();
            byte[] in = str.getBytes();
            x[p] = in[0];
        } catch (Exception e) {
            errBF("Input Parse Error");
        }
    }

    /**
     *  Error Handler
     *  @param String - error message
     *  @since 1.0
     */
    public void errBF(String str) {
        error = true;
        pc=10000;
        throw new Error(str);
    }
/*
    public static void main(String[] args) throws Exception {
        System.out.println("From main");
        BFI_old b = new BFI_old("");
        BufferedReader reader;
        String line = "", input = "", filename = "";

        // Read each file and interpret.
        for (int z = 0; z < args.length; z++) {
            filename = args[z];
            input = "";

            try {
                reader = new BufferedReader(new FileReader(filename));
                while ((line = reader.readLine()) != null) {
                    input += line;
                }
            } catch (Exception e) {
                b.errBF("Cannot read input file");
            }

            b = new BFI_old(input);
            b.checkSyntax();
            b.interpret();
        }
    }*/
}