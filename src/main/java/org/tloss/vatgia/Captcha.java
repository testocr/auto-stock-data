package org.tloss.vatgia;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.sourceforge.vietocr.OCR;
import net.sourceforge.vietocr.utilities.ImageIOHelper;
import net.sourceforge.vietocr.utilities.Utilities;

public class Captcha {
	public void antiNoise() throws Exception {
		BufferedImage img = ImageIO.read(new File("security_code1.png"));
		BufferedImage image = new BufferedImage(img.getWidth(),
				img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(new Color(0xFFFFFF));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		int[] colors = new int[] { 0xfe5afd, 0xfe4c64, 0xfe3e06, 0x3b0808,
				0xfe3575, 0xfe4c7b, 0xfe3e61, 0x6c0a08, 0xfe3f46, 0xfe58e6,
				0xfe48fe, 0xfe32e5 };
		graphics.drawImage(
				toBufferedImage(transformGrayToTransparency(img, colors)), 0,
				0, new Color(0xFFFFFF), null);
		ImageIO.write(image, "jpg", new File("test.jpg"));

	}

	public String recognizeText(String fileName) throws Exception {
		String tessPath;
		String curLangCode = "eng"; // default language
		String psm = "3"; // Fully automatic page segmentation, but no OSD
							// (default)
		File baseDir = Utilities.getBaseDir(this);
		tessPath = new File(baseDir, "tesseract").getPath();
		OCR ocrEngine = new OCR(tessPath);
		ocrEngine.setPSM(psm);
		File imageFile = new File(fileName);
		List<IIOImage> iioImageList = ImageIOHelper.getIIOImageList(imageFile);
		List<File> tempTiffFiles = null;
		tempTiffFiles = ImageIOHelper.createTiffFiles(iioImageList, -1);
		String result = ocrEngine.recognizeText(tempTiffFiles, curLangCode);
		return result;
	}

	private Image transformGrayToTransparency(BufferedImage image,
			final int[] colors) {
		ImageFilter filter = new RGBImageFilter() {
			public final int filterRGB(int x, int y, int rgb) {
				boolean cleanColor = false;
				for (int i = 0; i < colors.length && !cleanColor; i++) {
					if ((colors[i]) == (rgb & 0x00FFFFFF))
						cleanColor = true;
				}
				if (cleanColor) {
					return 0xFFFFFFFF;
				}
				if ((rgb & 0x00FFFFFF) == 0)
					return 0xFFFFFFFF;
				return rgb;

			}
		};

		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);

		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	// This method returns true if the specified image has transparent pixels
	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	// This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	public boolean validate(String text) {
		return Pattern.matches("\\d{4}", text);
	}

	public static void main(String[] args) throws Exception {
		Captcha captcha = new Captcha();
		captcha.antiNoise();
		String result = captcha.recognizeText("test.jpg");
		System.out.println(result);
		System.out.println(captcha.validate(result.trim()));
	}
}
