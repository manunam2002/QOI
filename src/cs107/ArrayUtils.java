package cs107;

import java.util.ArrayList;

/**
 * Utility class to manipulate arrays.
 * @apiNote First Task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils(){}

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2){
        assert (a1 != null && a2 != null) || (a1 == null && a2 == null);
        assert a1.length == a2.length;

        for (int i = 0 ; i < a1.length ; ++i){
            if (a1[i] != a2[i]){
                return false;
            }
        } return true;
    }

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2){
        assert (a1 != null && a2 != null) || (a1 == null && a2 == null);
        assert a1.length == a2.length;

        for (int i = 0 ; i < a1.length ; ++i){
            if (!ArrayUtils.equals(a1[i],a2[i])){
                return false;
            }
        } return true;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value){
        byte [] tab = {value};
        return tab;
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes){
        assert (bytes.length == 4)||(bytes == null);

        int negative = 0;
        for (int i = 1 ; i < 4 ; ++i){
            if (bytes[i]<0){
                bytes [i] = (byte) (bytes[i]&0x7F);
                negative += 128*Math.pow(256,(3-i));
            }
        }
        int value = ((bytes[1] << 16)|
                (bytes[2] << 8)|
                (bytes[3])|
                (bytes[0] << 24));
        value += negative;

        return value;
    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value){
        byte [] bytes = new byte[4];
        bytes [0] = (byte) (value >>> 24);
        bytes [1] = (byte) ((value >>> 16)&0xFF);
        bytes [2] = (byte) ((value >>> 8)&0xFFFF);
        bytes [3] = (byte) (value&0xFFFFFF);

        return bytes;
    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte ... bytes){
        assert bytes !=null;

        byte [] tab = new byte[bytes.length];
        for (int i = 0 ; i < bytes.length ; ++i){
            tab [i] = bytes[i];
        }

        return tab;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[] ... tabs){
        assert tabs != null;
        for (int i = 0 ; i < tabs.length ; ++i){
            assert tabs [i] != null;
        }

        int totalLenght = 0;
        for (int i = 0; i < tabs.length ; ++i){
            totalLenght += tabs[i].length;
        }
        byte[] tabConcat = new byte[totalLenght];
        ArrayList<Byte> Concat = new ArrayList<Byte>();
        for (int i = 0 ; i < tabs.length ; ++i){
            for (int j = 0 ; j < tabs[i].length ; ++j){
                Concat.add(tabs[i][j]);
            }
        }
        for (int i = 0 ; i < totalLenght ; ++i){
            tabConcat [i] = Concat.get(i);
        }

        return tabConcat;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     * @param input (byte[]) - Array to extract from
     * @param start (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     * start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length){
        assert (input != null)
                && (start < input.length)
                && (start > -1)
                && (length > 0)
                && ((start + length) <= input.length);

        byte [] tab = new byte[length];
        for (int i = 0 ; i < length ; ++i){
            tab [i] = input[start+i];
        }

        return tab;
    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     * or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int ... sizes) {
        assert input != null;

        int sumSizes = 0;
        for (int i = 0 ; i < sizes.length ; ++i){
            sumSizes += sizes[i];
        }
        assert sumSizes == input.length;

        byte [][] tab = new byte[sizes.length][];

        int start = 0;
        for (int i = 0 ; i < sizes.length ; ++i){
            tab[i] = new byte[sizes[i]];
            for (int j = 0 ; j < sizes [i] ; ++j){
                tab[i][j] = input [start + j];
            }
            start += sizes[i];
        }

        return tab;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input){
        assert input != null;
        for (int i = 0 ; i < input.length ; ++i){
            assert input [i] != null;
        }

        int tabSize = input.length * input[0].length;
        byte[][] tab = new byte[tabSize][4];
        ArrayList<Integer> line = new ArrayList<Integer>(tabSize);
        for (int i = 0 ; i < input.length ; ++i){
            for (int j = 0 ; j < input[i].length ; ++j){
                line.add(input[i][j]);
            }
        }
        int [] line1 = new int[tabSize];
        for (int i = 0 ; i < tabSize ; ++i){
            line1 [i] = line.get(i);
        }
        for (int i = 0 ; i < tabSize ; ++i){
            for (int j = 0 ; j < 4 ; ++j){
                int index = j+1;
                if (index == 4){
                    index = 0;
                }
                tab [i][j] = ArrayUtils.fromInt(line1[i])[index];
            }
        }

        return tab;
    }

    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     * @param input (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     * or input's length differs from width * height
     * or height is invalid
     * or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width){
        assert input != null;
        for (int i = 0 ; i < input.length ; ++i){
            assert input[i] != null;
        }
        assert input.length == height*width;
        assert (height > 0)&&(width > 0);

        int [][] tab = new int[height][width];
        int [] line = new int[input.length];
        for (int i = 0 ; i < input.length ; ++i){
            byte[] aRGB = new byte[4];
            aRGB[0] = input[i][3];
            aRGB[1] = input[i][0];
            aRGB[2] = input[i][1];
            aRGB[3] = input[i][2];
            input[i] = aRGB;
        }
        for (int i = 0 ; i < input.length ; ++i){
            line[i] = ArrayUtils.toInt(input[i]);
        }
        for (int i = 0 ; i < height ; ++i){
            for (int j = 0 ; j < width ; ++j){
                tab [i][j] = line[(i*width)+j];
            }
        }

        return tab;
    }

    /**
     * Add a byte array to an Arraylist of bytes.
     * @param tab (byte[]) - byte array
     * @param qoiImage (Arraylist<Byte>) - byte Arraylist
     * @throws AssertionError if the byte array is null
     */
    public static void arrayAdd(byte[] tab , ArrayList<Byte> qoiImage){
        assert tab != null;

        for (int i = 0 ; i < tab.length ; ++i){
            qoiImage.add(tab[i]);
        }
    }

    /**
     * Update the previous pixel in the encoder/decoder algorithm.
     * @param precedent (byte[]) - previous pixel
     * @param newPrecedent (byte[]) - new previous pixel
     * @throws AssertionError if one of the byte arrays is null
     */
    public static void newPrecedent (byte[] precedent, byte[] newPrecedent){
        assert precedent != null;
        assert newPrecedent != null;

        for (int i = 0 ; i < precedent.length ; ++i){
            precedent[i] = newPrecedent [i];
        }
    }

}