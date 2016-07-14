package com.xabber.android.utils.alpha;

import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;

public class AlphaUtil {

	/**
	 * 
	 * @param v
	 *            Componente al que poner el alpha
	 * @param alpha
	 *            grado de alpha que aplicar
	 */
	public static void setCustomAlpha(View v, Integer alpha) {

		if (v != null) {

			if (alpha == null || alpha <= 0) {

				alpha = 100;
			}

			Float fAlpha = alpha.floatValue() / 100F;
			if (fAlpha != null) {
				if (Build.VERSION.SDK_INT < 11) {
					final AlphaAnimation animation = new AlphaAnimation(
							fAlpha.floatValue(), fAlpha.floatValue());
					animation.setDuration(0);
					animation.setFillAfter(true);
					v.startAnimation(animation);
				} else {
					v.setAlpha(fAlpha.floatValue());
				}
			}
		}

	}

}
