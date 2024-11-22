package cs107;

import static cs107.Helper.Image;

/**
 * "Quite Ok Image" Decoder
 * @apiNote Third task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder(){}

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header){
        assert header != null;
        assert header.length == QOISpecification.HEADER_SIZE;
        assert ArrayUtils.equals(ArrayUtils.extract(header,0,4),QOISpecification.QOI_MAGIC);
        assert (header[12] == QOISpecification.RGB)||(header[12] == QOISpecification.RGBA);
        assert (header[13] == QOISpecification.ALL)||(header[13] == QOISpecification.sRGB);

        int [] decodeHeader = new int[4];

        byte [] width = ArrayUtils.extract(header,4,4);
        byte [] height = ArrayUtils.extract(header,8,4);

        decodeHeader [0] = ArrayUtils.toInt(width);
        decodeHeader [1] = ArrayUtils.toInt(height);
        decodeHeader [2] = header[12];
        decodeHeader [3] = header[13];

        return decodeHeader;
    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param alpha (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx){
        assert (buffer != null) && (input != null);
        assert (position >= 0) && (position < buffer.length);
        assert (idx >= 0) && (idx <= (input.length-3));
        assert input.length >= 3;

        byte [] pixel = new byte[4];
        for (int i = 0 ; i < 3 ; ++i){
            pixel[i] = input[idx+i];
        }
        pixel [3] = alpha;

        for (int i = 0 ; i < 4 ; ++i){
            buffer[position][i] = pixel [i];
        }

        return 3;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx){
        assert (buffer != null) && (input != null);
        assert (position >= 0) && (position < buffer.length);
        assert (idx >= 0) && (idx <= (input.length-4));
        assert input.length >= 4;

        byte [] pixel = new byte[4];
        for (int i = 0 ; i < 4 ; ++i){
            pixel[i] = input[idx+i];
        }

        for (int i = 0 ; i < 4 ; ++i){
            buffer[position][i] = pixel [i];
        }

        return 4;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk){
        assert previousPixel != null;
        assert previousPixel.length == 4;
        assert (chunk & 0xC0) == QOISpecification.QOI_OP_DIFF_TAG;

        byte dr = (byte) (((chunk & 0x3F) >> 4)-2);
        byte dg = (byte) (((chunk & 0x0F) >> 2)-2);
        byte db = (byte) ((chunk & 0x03)-2);

        byte [] pixel = new byte[4];

        pixel [0] = (byte) (previousPixel[0]+dr);
        pixel [1] = (byte) (previousPixel[1]+dg);
        pixel [2] = (byte) (previousPixel[2]+db);
        pixel [3] = previousPixel[3];

        return pixel;
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     * @param previousPixel (byte[]) - The previous pixel
     * @param data (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data){
        assert (previousPixel != null) && (data != null);
        assert previousPixel.length == 4;
        assert ((byte) (data[0] & 0xC0)) == QOISpecification.QOI_OP_LUMA_TAG;

        byte dg = (byte) ((data[0] & 0x3F)-32);
        byte dr = (byte) ((((data[1] & 0xF0)>>4)-8)+dg);
        byte db = (byte) (((data[1] & 0x0F)-8)+dg);

        byte [] pixel = new byte[4];

        pixel [0] = (byte) (previousPixel[0]+dr);
        pixel [1] = (byte) (previousPixel[1]+dg);
        pixel [2] = (byte) (previousPixel[2]+db);
        pixel [3] = previousPixel[3];

        return pixel;
    }

    /**
     * Store the given pixel in the buffer multiple times
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param pixel (byte[]) - The pixel to store
     * @param chunk (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position){
        assert buffer != null;
        assert (position >= 0) && (position < buffer.length);
        assert pixel != null;
        assert pixel.length == 4;

        int count = chunk & 0x3F;
        assert ((count+1+position) <= buffer.length);

        for (int i = 0 ; i < count+1 ; ++i){
            for (int j = 0 ; j < 4 ; ++j){
                buffer [position+i][j] = pixel[j];
            }
        }

        return count;
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     * @param data (byte[]) - Data to decode
     * @param width (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height){
        assert data != null;
        assert (width > 0)&&(height > 0);

        byte [] precedent = new byte[4];
        ArrayUtils.newPrecedent(precedent,QOISpecification.START_PIXEL);
        byte [][] hashTable = new byte[64][4];
        byte [][] buffer = new byte[width*height][4];
        int position = 0;

        for (int i = 0 ; i < data.length ; ++i){

            if (((byte) (data[i] & 0xC0) == QOISpecification.QOI_OP_RUN_TAG) &&
                    ((data[i]) != QOISpecification.QOI_OP_RGB_TAG) &&
                    ((data[i]) != QOISpecification.QOI_OP_RGBA_TAG)){
                position += QOIDecoder.decodeQoiOpRun(buffer,precedent,data[i],position);
                ++position;
            } else {

                if ((byte) (data[i] & 0xC0) == QOISpecification.QOI_OP_INDEX_TAG){
                    int index = (data[i] & 0x3F);
                    for (int j = 0 ; j < 4 ; ++j){
                        buffer[position][j] = hashTable [index][j];
                    }
                    ArrayUtils.newPrecedent(precedent,hashTable[index]);
                    ++position;
                } else {

                    if ((byte) (data[i] & 0xC0) == QOISpecification.QOI_OP_DIFF_TAG){
                        byte [] pixel = QOIDecoder.decodeQoiOpDiff(precedent,data[i]);
                        int hashIdx = QOISpecification.hash(pixel);
                        for (int j = 0 ; j < 4 ; ++j){
                            buffer[position][j] = pixel [j];
                            hashTable[hashIdx][j] = pixel[j];
                        }
                        ArrayUtils.newPrecedent(precedent,pixel);
                        ++position;
                    } else {

                        if ((byte) (data[i] & 0xC0) == QOISpecification.QOI_OP_LUMA_TAG){
                            byte [] lumaChunk = ArrayUtils.extract(data,i,2);
                            byte [] pixel = QOIDecoder.decodeQoiOpLuma(precedent,lumaChunk);
                            int hashIdx = QOISpecification.hash(pixel);
                            for (int j = 0 ; j < 4 ; ++j){
                                buffer[position][j] = pixel [j];
                                hashTable[hashIdx][j] = pixel[j];
                            }
                            ArrayUtils.newPrecedent(precedent,pixel);
                            ++position;
                            ++i;
                        } else {

                            if (data[i] == QOISpecification.QOI_OP_RGB_TAG){
                                int idx = i+1;
                                i += QOIDecoder.decodeQoiOpRGB(buffer,data,precedent[3],position,idx);
                                ++position;
                                byte [] pixel = new byte[4];
                                for (int j = 0 ; j < 3 ; ++j){
                                    pixel[j] = data[idx+j];
                                }
                                pixel [3] = precedent[3];
                                int hashIdx = QOISpecification.hash(pixel);
                                for (int j = 0 ; j < 4 ; ++j){
                                    hashTable[hashIdx][j] = pixel[j];
                                }
                                ArrayUtils.newPrecedent(precedent,pixel);
                            } else {

                                if (data[i] == QOISpecification.QOI_OP_RGBA_TAG){
                                    int idx = i+1;
                                    i += QOIDecoder.decodeQoiOpRGBA(buffer,data,position,idx);
                                    ++position;
                                    byte [] pixel = new byte[4];
                                    for (int j = 0 ; j < 4 ; ++j){
                                        pixel[j] = data[idx+j];
                                    }
                                    int hashIdx = QOISpecification.hash(pixel);
                                    for (int j = 0 ; j < 4 ; ++j){
                                        hashTable[hashIdx][j] = pixel[j];
                                    }
                                    ArrayUtils.newPrecedent(precedent,pixel);
                                }
                            }
                        }
                    }
                }
            }
        }
        assert position == (height*width);
        return buffer;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content){
        assert content != null;
        byte [] header = ArrayUtils.extract(content,0,14);
        byte [] data = ArrayUtils.extract(content,14,(content.length-14-8));
        byte [] eOF = ArrayUtils.extract(content,(content.length-8),8);
        assert ArrayUtils.equals(eOF,QOISpecification.QOI_EOF);

        int width = QOIDecoder.decodeHeader(header)[0];
        int height = QOIDecoder.decodeHeader(header)[1];
        byte channels = (byte) QOIDecoder.decodeHeader(header)[2];
        byte colorSpace = (byte) QOIDecoder.decodeHeader(header)[3];

        byte [][] imageDecoded = QOIDecoder.decodeData(data,width,height);
        int [][] image = ArrayUtils.channelsToImage(imageDecoded,height,width);
        return Helper.generateImage(image,channels,colorSpace);
    }

}