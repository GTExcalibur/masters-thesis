package gturner.crossword.substance;

import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.colorscheme.BaseColorScheme;
import org.pushingpixels.substance.internal.utils.SubstanceColorUtilities;

import java.awt.*;

/**
 * Alpha channel color scheme. A alpha channel color scheme is a color scheme that is
 * set to use a particular alpha channel value.
 *
 * @author Kirill Grouchnikov
 * @author Danno Ferrin
 * @see org.pushingpixels.substance.internal.colorscheme.ShiftColorScheme
 */
public class AlphaColorScheme extends BaseColorScheme {
	/**
	 * Alpha channel.
	 */
	private int alphaBlend;

	/**
	 * The main ultra-light color.
	 */
	private Color mainUltraLightColor;

	/**
	 * The main extra-light color.
	 */
	private Color mainExtraLightColor;

	/**
	 * The main light color.
	 */
	private Color mainLightColor;

	/**
	 * The main medium color.
	 */
	private Color mainMidColor;

	/**
	 * The main dark color.
	 */
	private Color mainDarkColor;

	/**
	 * The main ultra-dark color.
	 */
	private Color mainUltraDarkColor;

	/**
	 * The foreground color.
	 */
	private Color foregroundColor;

	/**
	 * The original color scheme.
	 */
	private SubstanceColorScheme origScheme;

	/**
	 * Creates a new saturated color scheme.
	 *
	 * @param origScheme
	 *            The original color scheme.
	 * @param saturationFactor
	 *            Saturation factor. Should be in -1.0..1.0 range.
	 */
	public AlphaColorScheme(SubstanceColorScheme origScheme,
			double alphaBlend) {
		super("Alpha Channeled (" + (int) (100 * alphaBlend) + "%) "
				+ origScheme.getDisplayName(), origScheme.isDark());
		this.alphaBlend = (int)(alphaBlend*255);
		this.origScheme = origScheme;
		this.foregroundColor = origScheme.getForegroundColor();
		this.mainUltraDarkColor = SubstanceColorUtilities.getAlphaColor(
                origScheme.getUltraDarkColor(), this.alphaBlend);
		this.mainDarkColor = SubstanceColorUtilities.getAlphaColor(
				origScheme.getDarkColor(), this.alphaBlend);
		this.mainMidColor = SubstanceColorUtilities.getAlphaColor(
				origScheme.getMidColor(), this.alphaBlend);
		this.mainLightColor = SubstanceColorUtilities.getAlphaColor(
				origScheme.getLightColor(), this.alphaBlend);
		this.mainExtraLightColor = SubstanceColorUtilities.getAlphaColor(
				origScheme.getExtraLightColor(), this.alphaBlend);
		this.mainUltraLightColor = SubstanceColorUtilities.getAlphaColor(
				origScheme.getUltraLightColor(), this.alphaBlend);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.pushingpixels.substance.color.ColorScheme#getForegroundColor()
	 */
	@Override
	public Color getForegroundColor() {
		return this.foregroundColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.pushingpixels.substance.color.ColorScheme#getUltraLightColor()
	 */
	@Override
	public Color getUltraLightColor() {
		return this.mainUltraLightColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.pushingpixels.substance.color.ColorScheme#getExtraLightColor()
	 */
	@Override
	public Color getExtraLightColor() {
		return this.mainExtraLightColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.pushingpixels.substance.color.ColorScheme#getLightColor()
	 */
	@Override
	public Color getLightColor() {
		return this.mainLightColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.pushingpixels.substance.color.ColorScheme#getMidColor()
	 */
	@Override
	public Color getMidColor() {
		return this.mainMidColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.pushingpixels.substance.color.ColorScheme#getDarkColor()
	 */
	@Override
	public Color getDarkColor() {
		return this.mainDarkColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.pushingpixels.substance.color.ColorScheme#getUltraDarkColor()
	 */
	@Override
	public Color getUltraDarkColor() {
		return this.mainUltraDarkColor;
	}

	/**
	 * Returns the original color scheme.
	 *
	 * @return The original color scheme.
	 */
	public SubstanceColorScheme getOrigScheme() {
		return this.origScheme;
	}

	/**
	 * Returns the Alpha Blend.
	 *
	 * @return Alpha Blend.
	 */
	public double getAlphaBlend() {
		return this.alphaBlend / 255.0D;
	}
}
