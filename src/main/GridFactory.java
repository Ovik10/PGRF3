package main;

import lwjglutils.OGLBuffers;

public class GridFactory {

    /**
     * @param a počet vrcholů na řádku
     * @param b počet vrcholů ve sloupci
     * @return OGLBuffers
     */
    public static OGLBuffers createGrid(int a, int b) {

        float[] vb = new float[a * b * 2];
        int index = 0;
        for (int j = 0; j < b; j++) {
            float y = j / (float) (b - 1);
            for (int i = 0; i < a; i++) {
                float x = i / (float) (a - 1);
                vb[index++] = x;
                vb[index++] = y;
//                System.out.println(x + ", " + y);
            }
        }

        int[] ib = new int[(a - 1) * (b - 1) * 2 * 3];
        int index2 = 0;

        for (int j = 0; j < b - 1; j++) {
            int offset = j * a;
            for (int i = 0; i < a - 1; i++) {
                ib[index2++] = offset + i;
                ib[index2++] = offset + i + 1;
                ib[index2++] = offset + i + a;
                ib[index2++] = offset + i + a;
                ib[index2++] = offset + i + 1;
                ib[index2++] = offset + i + a + 1;
//                System.out.println(offset + i);
//                System.out.println(offset + i + 1);
//                System.out.println(offset + i + a);
//                System.out.println(offset + i + a);
//                System.out.println(offset + i + 1);
//                System.out.println(offset + i + a + 1);
//                System.out.println("---");
            }
        }

        OGLBuffers.Attrib[] attributes = {
            new OGLBuffers.Attrib("inPosition", 2) // 2 floats per vertex
        };
        return new OGLBuffers(vb, attributes, ib);
    }

//    public static void main(String[] args) {
//        createGrid(4, 3);
//    }

}
