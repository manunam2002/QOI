package cs107;

import java.util.ArrayList;

/**
 * "Quite Ok Image" Encoder
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder(){}

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *  (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image){
        assert (image.color_space() == QOISpecification.ALL) || (image.color_space() == QOISpecification.sRGB);
        assert ((image.channels() == QOISpecification.RGB) || (image.channels() == QOISpecification.RGBA));
        assert image != null;

        return ArrayUtils.concat(
                QOISpecification.QOI_MAGIC,
                ArrayUtils.fromInt(image.data()[0].length),
                ArrayUtils.fromInt(image.data().length),
                ArrayUtils.wrap(image.channels()),
                ArrayUtils.wrap(image.color_space()));
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel){
        assert pixel.length == 4;

        return ArrayUtils.concat(
                QOISpecification.QOI_OP_RGB_TAG,
                pixel[0],
                pixel[1],
                pixel[2]);
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel){
        assert pixel.length == 4;

        return ArrayUtils.concat(
                ArrayUtils.wrap(QOISpecification.QOI_OP_RGBA_TAG),
                pixel);
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index){
        assert (index > -1) || (index < 64);

        return ArrayUtils.wrap(index);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff){
        assert diff != null;
        assert diff.length == 3;
        for (int i = 0 ; i < 3 ; ++i){
            assert (diff [i] > -3) && (diff [i] < 2);
        }

        int qoiOpDiff = (QOISpecification.QOI_OP_DIFF_TAG |
                (diff[0]+2) << 4 |
                (diff[1]+2) << 2 |
                (diff[2]+2));

        return ArrayUtils.wrap((byte) qoiOpDiff);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     * or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff){
        assert diff != null;
        assert diff.length == 3;
        assert (diff [1] < 32) && (diff [1] > -33);
        assert ((diff[0]-diff[1]) < 8) && ((diff[0]-diff[1]) > -9);
        assert ((diff[2]-diff[1]) < 8) && ((diff[2]-diff[1]) > -9);

        int qoiOpLuma1 = (QOISpecification.QOI_OP_LUMA_TAG |
                (diff[1]+32));
        int qoiOpLuma2 = ((diff[0]-diff[1]+8) << 4 |
                (diff[2]-diff[1]+8));

        return ArrayUtils.concat((byte) qoiOpLuma1,(byte) qoiOpLuma2);
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count){
        assert (count > 0) && (count < 63);

        int qoiOpRun = QOISpecification.QOI_OP_RUN_TAG |
                (count-1);

        return ArrayUtils.wrap((byte) qoiOpRun);
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image){
        assert image.length != 0;
        for (int i = 0 ; i < image.length ; ++i){
            assert image [i] != null;
            assert image[i].length == 4;
        }

        ArrayList<Byte> QoiImage = new ArrayList<>();

        byte [] precedent = new byte[4];
        ArrayUtils.newPrecedent(precedent,QOISpecification.START_PIXEL);
        byte [][] hashTable = new byte[64][4];


        for (int i = 0 ; i < image.length ; ++i){

            int count = 0;
            int count2 = 0;
            if (ArrayUtils.equals(image[i],precedent)){
                while (ArrayUtils.equals(image[i],precedent)){
                    ++i;
                    count +=1;
                    if (i == image.length){
                        break;
                    }
                }
                i -=1;
                ArrayUtils.newPrecedent(precedent,image[i]);
                if (count > 62){
                    while (count > 62){
                        count -= 62;
                        count2 += 1;
                    }
                }
                if (count2 > 0){
                    for (int k = 0 ; k < count2 ; ++k){
                        byte [] run2 = QOIEncoder.qoiOpRun((byte) 62);
                        ArrayUtils.arrayAdd(run2, QoiImage);
                    }
                }
                byte [] run = QOIEncoder.qoiOpRun((byte) count);
                ArrayUtils.arrayAdd(run, QoiImage);
            } else {

                byte hash = QOISpecification.hash(image[i]);
                if (ArrayUtils.equals(image[i],hashTable[hash])) {
                    byte[] index = QOIEncoder.qoiOpIndex(hash);
                    ArrayUtils.arrayAdd(index, QoiImage);
                    ArrayUtils.newPrecedent(precedent, image[i]);
                } else {

                    for (int j = 0 ; j < 4 ; ++j){
                        hashTable[hash][j] = image[i][j];
                    }

                    if (image[i][3] == precedent[3]){
                        boolean testDiff = true;
                        byte [] diff = new byte[3];
                        for (int j = 0 ; j < 3 ; ++j){
                            byte diff0 = (byte) (image[i][j]-precedent[j]);
                            diff [j] = diff0;
                        }
                        for (int j = 0 ; j < 3 ; ++j){
                            if ((diff[j] <= -3)||(diff[j] >= 2)){
                                testDiff = false;
                            }
                        }
                        if (testDiff){
                            byte [] qoiDiff = QOIEncoder.qoiOpDiff(diff);
                            ArrayUtils.arrayAdd(qoiDiff, QoiImage);
                            ArrayUtils.newPrecedent(precedent,image[i]);
                        } else {

                            if (image[i][3] == precedent[3]){
                                boolean testLuma = false;
                                byte [] luma = new byte[3];
                                for (int j = 0 ; j < 3 ; ++j){
                                    byte diff0 = (byte) (image[i][j]-precedent[j]);
                                    luma [j] = diff0;
                                }
                                if ((luma [1] < 32) && (luma [1] > -33)&&
                                        ((luma[0]-luma[1]) < 8) && ((luma[0]-luma[1]) > -9)&&
                                        ((luma[2]-luma[1]) < 8) && ((luma[2]-luma[1]) > -9)){
                                    testLuma = true;
                                }
                                if (testLuma){
                                    byte [] qoiLuma = QOIEncoder.qoiOpLuma(luma);
                                    ArrayUtils.arrayAdd(qoiLuma, QoiImage);
                                    ArrayUtils.newPrecedent(precedent,image[i]);
                                } else {

                                    byte [] rgb = QOIEncoder.qoiOpRGB(image[i]);
                                    ArrayUtils.arrayAdd(rgb, QoiImage);
                                    ArrayUtils.newPrecedent(precedent,image[i]);
                                }
                            }
                        }
                    } else {

                        byte [] rgba = QOIEncoder.qoiOpRGBA(image[i]);
                        ArrayUtils.arrayAdd(rgba, QoiImage);
                        ArrayUtils.newPrecedent(precedent,image[i]);
                    }
                }
            }
        }
        byte [] qoiImage = new byte[QoiImage.size()];
        for (int i = 0 ; i < QoiImage.size() ; ++i){
            qoiImage[i] = QoiImage.get(i);
        }
        return qoiImage;
    }

    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     */
    public static byte[] qoiFile(Helper.Image image){
        assert image != null;

        return ArrayUtils.concat(QOIEncoder.qoiHeader(image),
                QOIEncoder.encodeData(ArrayUtils.imageToChannels(image.data())),
                QOISpecification.QOI_EOF);
    }

}