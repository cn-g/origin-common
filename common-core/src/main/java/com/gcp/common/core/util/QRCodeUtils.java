package com.gcp.common.core.util;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 生成二维码
 * @author Admin
 */
public class QRCodeUtils extends LuminanceSource {

  private static final Logger logger = LoggerFactory.getLogger(QRCodeUtils.class);

  // 二维码颜色
  private static final int BLACK = 0xFF000000;
  // 二维码颜色
  private static final int WHITE = 0xFFFFFFFF;

  private final BufferedImage image;
  private final int left;
  private final int top;

  public QRCodeUtils(BufferedImage image) {
    this(image, 0, 0, image.getWidth(), image.getHeight());
  }

  public QRCodeUtils(BufferedImage image, int left, int top, int width, int height) {
    super(width, height);
    int sourceWidth = image.getWidth();
    int sourceHeight = image.getHeight();
    if (left + width > sourceWidth || top + height > sourceHeight) {
      throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
    }
    for (int y = top; y < top + height; y++) {
      for (int x = left; x < left + width; x++) {
        if ((image.getRGB(x, y) & 0xFF000000) == 0) {
          image.setRGB(x, y, 0xFFFFFFFF);
        }
      }
    }
    this.image = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_BYTE_GRAY);
    this.image.getGraphics().drawImage(image, 0, 0, null);
    this.left = left;
    this.top = top;
  }

  /**
   * @param matrix
   * @return
   */
  private static BufferedImage toBufferedImage(BitMatrix matrix) {
    int width = matrix.getWidth();
    int height = matrix.getHeight();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
      }
    }
    return image;
  }

  /**
   * 生成二维码图片
   *
   * @param matrix
   * @param format
   * @param file
   * @throws IOException
   */
  private static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
    BufferedImage image = toBufferedImage(matrix);
    if (!ImageIO.write(image, format, file)) {
      throw new IOException("Could not write an image of format " + format + " to " + file);
    }
  }


  /**
   * 根据内容，生成指定宽高、指定格式的二维码图片
   *
   * @param text   内容
   * @param width  宽
   * @param height 高
   * @param format 图片格式
   * @return 生成的二维码图片路径
   * @throws Exception
   */
  private static String generateQRCode(String text, int width, int height, String format, String pathName)
      throws Exception {
    HashMap<EncodeHintType, Object> hints = new HashMap<>();
    // 指定编码格式
    hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
    // 指定纠错等级
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
    // 白边大小，取值范围0~4
    hints.put(EncodeHintType.MARGIN, 1);
    BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
    File outputFile = new File(pathName);
    writeToFile(bitMatrix, format, outputFile);
    return pathName;
  }


  /**
   * 解析指定路径下的二维码图片
   *
   * @param filePath 二维码图片路径
   * @return
   */
  public static String parseQRCode(String filePath) {
    String content = "";
    try {
      File file = new File(filePath);
      BufferedImage image = ImageIO.read(file);
      LuminanceSource source = new QRCodeUtils(image);
      Binarizer binarizer = new HybridBinarizer(source);
      BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
      Map<DecodeHintType, Object> hints = new HashMap<>();
      hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
      MultiFormatReader formatReader = new MultiFormatReader();
      Result result = formatReader.decode(binaryBitmap, hints);
      // 设置返回值
      content = result.getText();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return content;
  }

  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = getWidth();
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    image.getRaster().getDataElements(left, top + y, width, 1, row);
    return row;
  }

  @Override
  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();
    int area = width * height;
    byte[] matrix = new byte[area];
    image.getRaster().getDataElements(left, top, width, height, matrix);
    return matrix;
  }

  @Override
  public boolean isCropSupported() {
    return true;
  }

  @Override
  public LuminanceSource crop(int left, int top, int width, int height) {
    return new QRCodeUtils(image, this.left + left, this.top + top, width, height);
  }

  @Override
  public boolean isRotateSupported() {
    return true;
  }

  @Override
  public LuminanceSource rotateCounterClockwise() {
    int sourceWidth = image.getWidth();
    int sourceHeight = image.getHeight();
    AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, sourceWidth);
    BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D g = rotatedImage.createGraphics();
    g.drawImage(image, transform, null);
    g.dispose();
    int width = getWidth();
    return new QRCodeUtils(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
  }

  /**
   * 生成二维码
   * @param text 二维码的内容
   * @param filePath 保存路径(不需要写到文件名称)
   * @param name 保存的图片名称
   */
  public static void createQRCode(String text,String filePath,String name){
    logger.info("下载二维码，生成二维码URL：{}", text);
    String qrCodeName = filePath + "/" + name+".png";
    File file = new File(qrCodeName);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    // 二维码图片的宽
    final int width = 300;
    // 二维码图片的高
    final int height = 300;
    // 二维码图片的格式
    String format = "png";
    try {
      // 生成二维码图片，并返回图片路径
      String pathName = QRCodeUtils.generateQRCode(text, width, height, format, qrCodeName);
      logger.info("生成二维码的图片路径：{}", pathName);
    } catch (Exception e) {
      logger.error("生成二维码失败：{}", e);
    }
  }

}
